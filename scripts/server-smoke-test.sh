#!/usr/bin/env bash
# Starts a dev server for one Minecraft version and loader (./gradlew :<loader>:runServer in that workspace, with
# TerraBlender on the dev runtime) on a normal generated world, and checks from a datapack that the mod runs:
#   - places every Fairy Garden block with /setblock (an unknown block id makes the function fail to load),
#   - summons a butterfly and a moon wisp (an unknown entity id makes the function fail to load),
#   - finds each of the five biomes (through the TerraBlender region) and the four structures with /locate,
#   - lets the world run, then stops.
# Fails if the server crashes, never gets there, the datapack does not load, a block, biome or structure is missing,
# or the log has an error that names the mod.
#   usage: scripts/server-smoke-test.sh <minecraft version folder> <fabric|forge|neoforge>
set -u
VERSION="$1"; LOADER="$2"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/$VERSION"
RUN="$LOADER/runs/server"
rm -rf "$RUN/world" "$RUN/crash-reports" "$RUN/logs"
NS=fairy_smoke
PACK="$RUN/world/datapacks/$NS"
mkdir -p "$PACK"
echo "eula=true" > "$RUN/eula.txt"
if [ "$LOADER" = forge ]; then
  # In the Forge 1.20.1 dev environment the LAN advertiser loads a client-only class and the dedicated server stops.
  mkdir -p "$RUN/defaultconfigs"
  printf '[server]\n\tadvertiseDedicatedServerToLan = false\n' > "$RUN/defaultconfigs/forge-server.toml"
fi
# max-tick-time=-1: a /locate can take longer than the watchdog's minute on a slow runner.
cat > "$RUN/server.properties" <<PROPS
online-mode=false
function-permission-level=4
spawn-protection=0
view-distance=4
simulation-distance=4
max-tick-time=-1
level-seed=fairygarden
initial-enabled-packs=vanilla,file/$NS
PROPS
# The mod's own pack object (pack_format / supported_formats / min_format+max_format) fits this Minecraft version.
python3 - common/src/main/resources/pack.mcmeta "$PACK/pack.mcmeta" <<'PY'
import json, sys
pack = json.load(open(sys.argv[1]))["pack"]
pack["description"] = "Fairy Garden smoke test"
json.dump({"pack": pack}, open(sys.argv[2], "w"))
PY

# Every block the workspace has a blockstate for.
BLOCKS=$(find common/src/main/resources/assets/fairygarden/blockstates common/src/generated/resources/assets/fairygarden/blockstates \
  -name '*.json' 2>/dev/null | sed 's|.*/||; s|\.json$||' | sort -u)
COUNT=$(echo "$BLOCKS" | grep -c .)
echo "$COUNT blocks in $VERSION"
TARGETS="biome:blossom_valley biome:lavender_fields biome:strawberry_meadow biome:moonlight_forest biome:fairy_glade
structure:flower_shrine structure:hidden_fairy_garden structure:moonstone_circle structure:mushroom_cottage"
NTARGETS=$(echo $TARGETS | wc -w)

# 1.20.1 reads functions/ and tags/functions/, 1.21+ reads function/ and tags/function/.
for dir in function functions; do
  F="$PACK/data/$NS/$dir"
  mkdir -p "$F" "$PACK/data/minecraft/tags/$dir"
  echo "{\"values\":[\"$NS:start\"]}" > "$PACK/data/minecraft/tags/$dir/load.json"
  printf 'scoreboard objectives add fairy_smoke dummy\nforceload add 0 0 63 63\nschedule function %s:place 100t\n' "$NS" > "$F/start.mcfunction"
  {
    i=0
    for id in $BLOCKS; do
      echo "setblock $(( (i % 20) * 3 )) 250 $(( (i / 20) * 3 )) fairygarden:$id"
      i=$((i + 1))
    done
    echo "say FAIRY_SMOKE_PLACED $i"
    echo "summon fairygarden:butterfly 4 252 4"
    echo "summon fairygarden:moon_wisp 8 252 8"
    echo "say FAIRY_SMOKE_SUMMONED"
    echo "schedule function $NS:locate_0 40t"
  } > "$F/place.mcfunction"
  # One /locate per tick-separated function, so a slow search never stacks up with the next one.
  n=0
  for target in $TARGETS; do
    kind=${target%%:*}; name=${target#*:}
    {
      echo "scoreboard players set #found fairy_smoke 0"
      echo "execute store success score #found fairy_smoke run locate $kind fairygarden:$name"
      echo "execute if score #found fairy_smoke matches 1.. run say FAIRY_SMOKE_FOUND $kind $name"
      echo "execute unless score #found fairy_smoke matches 1.. run say FAIRY_SMOKE_MISSING $kind $name"
      if [ $((n + 1)) -lt "$NTARGETS" ]; then echo "schedule function $NS:locate_$((n + 1)) 20t"; else echo "schedule function $NS:finish 200t"; fi
    } > "$F/locate_$n.mcfunction"
    n=$((n + 1))
  done
  printf 'say FAIRY_SMOKE_OK\nstop\n' > "$F/finish.mcfunction"
done

LOG="$ROOT/server-smoke-$VERSION-$LOADER.log"
# Lets the workspace's toolchain lookup find the JDKs setup-java installs (Java 17 for 1.20.1 bytecode).
./gradlew ":$LOADER:runServer" --stacktrace \
  -Porg.gradle.java.installations.fromEnv=JAVA_HOME_17_X64,JAVA_HOME_21_X64,JAVA_HOME_25_X64 \
  < /dev/null > "$LOG" 2>&1 &
PID=$!
result=timeout
for _ in $(seq 1 300); do
  sleep 5
  if grep -q "FAIRY_SMOKE_OK" "$LOG"; then result=ok; break; fi
  if grep -q -E "This crash report has been saved|Encountered an unexpected exception|Failed to start the minecraft server|Failed to load function $NS|Failed to execute function $NS|Feature order cycle found|Mixin apply.*failed|InvalidInjectionException" "$LOG"; then result=crash; break; fi
  if ! kill -0 $PID 2>/dev/null; then result=exited; break; fi
done
sleep 10
pkill -P $PID 2>/dev/null; kill $PID 2>/dev/null; wait $PID 2>/dev/null

grep -n -i -E "FAIRY_SMOKE|fairygarden|Fairy Garden|terrablender|Done \(" "$LOG" | grep -v -E "Download|Task :|setblock" | head -60
for f in "$RUN"/crash-reports/*.txt; do [ -f "$f" ] && { echo "=== $f"; head -80 "$f"; }; done
grep -n -E "ERROR|Exception|Caused by|[Mm]ixin.*(fail|error)" "$LOG" | head -60
echo "Result: $result"
if [ "$result" != ok ]; then
  # The Gradle output (compile errors included) only lives in $LOG, so show the useful part here.
  grep -n -E ": error:|What went wrong|FAILURE:" -A6 "$LOG" | head -150
  echo "=== last lines of $LOG"; tail -60 "$LOG"
fi
if [ "$result" != ok ] || grep -q -E "This crash report has been saved|InvalidInjectionException" "$LOG"; then exit 1; fi
grep -q "FAIRY_SMOKE_PLACED $COUNT" "$LOG" || { echo "Not every block was placed"; exit 1; }
grep -q "FAIRY_SMOKE_SUMMONED" "$LOG" || { echo "The entities were not summoned"; exit 1; }
if grep -q "FAIRY_SMOKE_MISSING" "$LOG"; then grep "FAIRY_SMOKE_MISSING" "$LOG"; echo "A biome or structure was not found"; exit 1; fi
found=$(grep -c "FAIRY_SMOKE_FOUND" "$LOG")
[ "$found" -ge "$NTARGETS" ] || { echo "Only $found of $NTARGETS biomes and structures were located"; exit 1; }
# "No data fixer registered for <entity>" is vanilla's development-only notice for every modded entity type.
if grep -E "ERROR|Exception|Caused by" "$LOG" | grep -v "No data fixer registered" | grep -q -E 'com.sugarplumstudios|fairygarden([^-.a-z]|$)'; then echo "The log has errors naming Fairy Garden"; exit 1; fi
echo "Server smoke test passed ($VERSION $LOADER): $COUNT blocks, 2 entities, 5 biomes and 4 structures"
