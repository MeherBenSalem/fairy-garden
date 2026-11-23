#!/usr/bin/env bash
# Boots a real Forge 1.20.1 dedicated server with one built Fairy Garden jar (and TerraBlender, its one required
# dependency) in its mods folder, on a normal generated world, and checks that it runs:
#   - places every Fairy Garden block with /setblock from a datapack (an unknown block id makes the function fail to load),
#   - finds each of the five biomes and four structures with /locate and force-loads the chunks there, so the server
#     generates them in full: terrain, trees, flowers, features and structures,
#   - summons a butterfly and a moon wisp,
#   - lets the world run, then stops.
# Fails if the server crashes, the mod does not load, a biome, structure or chunk is not found or generated, or the log
# has an error that names the mod.
#   usage: scripts/server-boot-test.sh <path to the mod jar>
set -u
JAR="$(cd "$(dirname "$1")" && pwd)/$(basename "$1")"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
prop() { grep "^$1=" "$ROOT/gradle.properties" | cut -d= -f2; }
MC=$(prop minecraft_version)
FORGE=$(prop forge_version)
TB=$(prop terrablender_version)
SERVER="$ROOT/build/boot-test-server"
rm -rf "$SERVER"; mkdir -p "$SERVER/mods"
cd "$SERVER"

curl -fsSL -o installer.jar "https://maven.minecraftforge.net/net/minecraftforge/forge/$MC-$FORGE/forge-$MC-$FORGE-installer.jar"
java -jar installer.jar --installServer > install.log 2>&1 || { tail -40 install.log; exit 1; }
curl -fsSL -o "mods/TerraBlender-forge-$MC-$TB.jar" \
  "https://maven.minecraftforge.net/com/github/glitchfiend/TerraBlender-forge/$MC-$TB/TerraBlender-forge-$MC-$TB.jar"
cp "$JAR" mods/
ls -la mods

echo "eula=true" > eula.txt
cat > server.properties <<PROPS
online-mode=false
function-permission-level=4
spawn-protection=0
view-distance=4
simulation-distance=4
level-seed=fairygarden
initial-enabled-packs=vanilla,file/fairy_boot
PROPS

# Every block id the jar has a blockstate for.
BLOCKS=$(unzip -Z1 "$JAR" 'assets/fairygarden/blockstates/*.json' | sed 's|.*/||; s|\.json$||' | sort)
NBLOCKS=$(echo "$BLOCKS" | grep -c .)
echo "$NBLOCKS blocks in the jar"
PACK="world/datapacks/fairy_boot"
mkdir -p "$PACK/data/fairy_boot/functions" "$PACK/data/minecraft/tags/functions"
echo '{"pack":{"pack_format":15,"description":"Fairy Garden boot test"}}' > "$PACK/pack.mcmeta"
echo '{"values":["fairy_boot:start"]}' > "$PACK/data/minecraft/tags/functions/load.json"
printf 'forceload add 0 0 63 63\nschedule function fairy_boot:place 100t\n' > "$PACK/data/fairy_boot/functions/start.mcfunction"
{
  i=0
  for id in $BLOCKS; do
    echo "setblock $(( (i % 20) * 3 )) 250 $(( (i / 20) * 3 )) fairygarden:$id"
    i=$((i + 1))
  done
  echo "say FAIRY_BOOT_PLACED $i"
} > "$PACK/data/fairy_boot/functions/place.mcfunction"

LOG="$SERVER/boot.log"
mkfifo console
bash run.sh nogui < console > "$LOG" 2>&1 &
PID=$!
exec 3> console
send() { echo "$*" >&3; }
fail_seen() { grep -q -E "This crash report has been saved|Encountered an unexpected exception|Failed to start the minecraft server|Failed to load function fairy_boot" "$LOG"; }
# Waits up to $2 seconds for a log line matching $1.
wait_for() {
  for _ in $(seq 1 "$2"); do
    grep -q -E "$1" "$LOG" && return 0
    fail_seen && return 1
    kill -0 $PID 2>/dev/null || return 1
    sleep 1
  done
  return 1
}

result=ok
wait_for 'Done \([0-9.]+s\)!' 600 || result=nostart
if [ "$result" = ok ]; then
  wait_for "FAIRY_BOOT_PLACED" 60 || result=noplace
fi

# Locate every biome and structure, then force-load the chunks there.
declare -A FOUND
if [ "$result" = ok ]; then
  for target in "biome blossom_valley" "biome lavender_fields" "biome strawberry_meadow" "biome moonlight_forest" "biome fairy_glade" \
                "structure flower_shrine" "structure hidden_fairy_garden" "structure moonstone_circle" "structure mushroom_cottage"; do
    set -- $target
    send "locate $1 fairygarden:$2"
    if wait_for "The nearest fairygarden:$2 is at" 240; then
      line=$(grep -E "The nearest fairygarden:$2 is at" "$LOG" | tail -1)
      x=$(echo "$line" | sed -E 's/.*is at \[(-?[0-9]+), [^,]+, (-?[0-9]+)\].*/\1/')
      z=$(echo "$line" | sed -E 's/.*is at \[(-?[0-9]+), [^,]+, (-?[0-9]+)\].*/\2/')
      FOUND[$2]="$x $z"
      echo "Located $1 $2 at $x $z"
      send "forceload add $((x - 16)) $((z - 16)) $((x + 16)) $((z + 16))"
    else
      echo "Could not locate $1 $2"; result=nolocate
    fi
  done
fi

# Wait until every force-loaded spot is a fully generated, loaded chunk.
if [ "$result" = ok ]; then
  for _ in $(seq 1 60); do
    pending=0
    for name in "${!FOUND[@]}"; do
      grep -q "FAIRY_LOADED_$name\$" "$LOG" && continue
      pending=$((pending + 1))
      set -- ${FOUND[$name]}
      send "execute if loaded $1 64 $2 run say FAIRY_LOADED_$name"
    done
    [ $pending = 0 ] && break
    fail_seen && break
    sleep 5
  done
  for name in "${!FOUND[@]}"; do
    grep -q "FAIRY_LOADED_$name\$" "$LOG" || { echo "The chunks at $name never finished generating"; result=nogen; }
  done
fi

if [ "$result" = ok ]; then
  set -- ${FOUND[moonlight_forest]}
  send "summon fairygarden:moon_wisp $1 200 $2"
  set -- ${FOUND[blossom_valley]}
  send "summon fairygarden:butterfly $1 200 $2"
  send "time set 18000"
  sleep 20
  send "time set 1000"
  sleep 10
  send "say FAIRY_BOOT_OK"
  wait_for "FAIRY_BOOT_OK" 30 || result=nofinish
fi
send "stop"
exec 3>&-
for _ in $(seq 1 60); do kill -0 $PID 2>/dev/null || break; sleep 1; done
pkill -P $PID 2>/dev/null; kill $PID 2>/dev/null; wait $PID 2>/dev/null

grep -n -i -E "FAIRY_|fairygarden|Fairy Garden|terrablender|The nearest|Done \(" "$LOG" | grep -v -E "setblock|DEBUG" | head -80
for f in crash-reports/*.txt; do [ -f "$f" ] && { echo "=== $f"; head -80 "$f"; }; done
grep -n -E "ERROR|Exception|Caused by" "$LOG" | head -60
echo "Result: $result"
[ "$result" = ok ] || exit 1
grep -q "This crash report has been saved" "$LOG" && exit 1
grep -q "FAIRY_BOOT_PLACED $NBLOCKS" "$LOG" || { echo "Not every block was placed"; exit 1; }
if grep -E "ERROR|Exception|Caused by" "$LOG" | grep -q -E 'com.sugarplumstudios|fairygarden'; then echo "The log has errors naming Fairy Garden"; exit 1; fi
echo "Server boot test passed: $NBLOCKS blocks placed, 5 biomes and 4 structures located and generated"
