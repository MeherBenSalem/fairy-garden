package com.sugarplumstudios.fairygarden.client.dev;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.worldgen.ModBiomes;
import com.sugarplumstudios.fairygarden.worldgen.ModStructures;
import net.minecraft.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Guided showcase of the mod. Only active when the client is started with {@code -Dfairygarden.showcase=true}
 * ({@code gradlew runClient -Pshowcase}); it does nothing in normal play and is left out of the jar.
 * <p>
 * From the title screen it creates a new world, then flies a spectator camera in slow circles around every Fairy
 * Garden biome (again by night where the glow matters) and every structure, with a caption and a few notes for
 * each stop. Transitions fade through dusk so chunk loading stays hidden. At the end the player lands beside the
 * last structure in Creative mode, and the world is theirs. Sneak skips the current stop, any open screen (Esc,
 * chat) pauses the tour, and leaving the world ends it. One screenshot per stop goes to
 * {@code run/screenshots/showcase_*.png}.
 */
@Mod.EventBusSubscriber(modid = FairyGarden.MOD_ID, value = Dist.CLIENT)
public final class ShowcaseTour {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean ENABLED = Boolean.getBoolean("fairygarden.showcase") && !Boolean.getBoolean("fairygarden.devshots");
    /** A seed known to have every biome and structure within ~2000 blocks of spawn. */
    private static final long SEED = 20261103L;
    private static final int DAY = 5000;
    private static final int NIGHT = 18000;
    private static final int FADE_TICKS = 12;
    private static final int TITLE_IN = 10, TITLE_STAY = 90, TITLE_OUT = 20;
    private static final int DUSK = 0x241B36;
    private static final int CREAM = 0xFFF3E6;
    private static final int PINK = 0xF9A8CB;

    private enum State { WAITING, TOURING, DONE }

    /** One step of the script. Ticked once per unpaused client tick with its own tick count until it returns true. */
    private interface Phase {
        boolean tick(int t);
    }

    /** Skippable entries are dropped when the player skips their stop; the others (travel, state changes) always run. */
    private record Entry(int stop, boolean skippable, Phase phase) {
    }

    /**
     * A circling camera: it stays at height {@code y} on a circle of {@code radius} around {@code focus} and keeps
     * looking at it. {@code spot} is the column the orbit was planned around and {@code box} the structure, if any.
     */
    private record Orbit(BlockPos spot, Vec3 focus, double radius, double y, double startAngle, double speed, BoundingBox box) {
        Vec3 eye(double ticks) {
            double a = Math.toRadians(startAngle + speed * ticks);
            return new Vec3(focus.x + Math.cos(a) * radius, y, focus.z + Math.sin(a) * radius);
        }
    }

    /** Plans the next orbit on the server thread, given the current one (may be null). Returns null if nothing was found. */
    private interface Plan {
        Orbit plan(ServerLevel level, Orbit current);
    }

    private static final List<Entry> script = new ArrayList<>();
    private static int building;
    private static int stopCount;

    private static State state = State.WAITING;
    private static int index, phaseTicks, ticks;
    private static int skipStop = -1;
    private static int stopNumber;
    private static CompletableFuture<?> pending;

    private static Orbit orbit;
    private static Orbit planned;
    private static BoundingBox lastBox;
    private static boolean orbiting;
    private static int orbitTicks;
    private static float yaw;

    private static float fade = 1, fadeO = 1, fadeTarget = 1;
    private static Component fadeCaption = Component.empty();
    private static Component title, subtitle;
    private static int titleColour, titleStart = -1000;
    private static boolean hidingGuiForShot, guiWasHidden;

    private ShowcaseTour() {
    }

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    // ---------------------------------------------------------------- script building

    private static void add(boolean skippable, Phase phase) {
        script.add(new Entry(building, skippable, phase));
    }

    private static void once(boolean skippable, Runnable action) {
        add(skippable, t -> {
            action.run();
            return true;
        });
    }

    private static void hold(boolean skippable, int ticks) {
        add(skippable, t -> t >= ticks);
    }

    private static void fadeOut(boolean skippable, String caption) {
        once(skippable, () -> {
            fadeCaption = Component.literal(caption);
            fadeTarget = 1;
        });
        add(skippable, t -> fade >= 1 || t > FADE_TICKS * 3);
    }

    /** Plans an orbit on the server (without blocking the client), then jumps the camera to it. */
    private static void travel(boolean skippable, String what, Plan plan) {
        add(skippable, t -> {
            if (t == 0) {
                IntegratedServer server = mc().getSingleplayerServer();
                Orbit current = orbit;
                pending = server.submit(() -> plan.plan(server.overworld(), current));
            }
            return pending.isDone();
        });
        once(skippable, () -> {
            planned = null;
            try {
                planned = (Orbit) pending.join();
            } catch (RuntimeException e) {
                LOGGER.error("[showcase] planning {} failed", what, e);
            }
            pending = null;
            if (planned == null) {
                LOGGER.warn("[showcase] {} not found in this world, skipping it", what);
                skipStop = currentStop();
                return;
            }
            LOGGER.info("[showcase] {} at {}", what, planned.spot());
            setOrbit(planned);
        });
    }

    /** Waits for the terrain around the orbit to be drawn, then fades in with a caption and notes. */
    private static void arrive(boolean skippable, String name, String tagline, int colour, Runnable extra, String... notes) {
        add(skippable, t -> t >= 300 || (t >= 10 && chunksReady()));
        hold(skippable, 10);
        once(skippable, () -> {
            fadeTarget = 0;
            orbiting = true;
            showTitle(name, tagline, colour);
            notes(colour, notes);
            if (extra != null) {
                extra.run();
            }
        });
    }

    /** Holds the orbit, saving one clean screenshot (GUI hidden for a moment) part-way through. */
    private static void show(boolean skippable, int ticks, String shotName) {
        int shotAt = Math.min(ticks - 5, TITLE_IN + TITLE_STAY + TITLE_OUT + 10);
        add(skippable, t -> {
            if (t == shotAt) {
                guiWasHidden = mc().options.hideGui;
                hidingGuiForShot = true;
                mc().options.hideGui = true;
            } else if (t == shotAt + 2) {
                Screenshot.grab(mc().gameDirectory, "showcase_" + shotName + ".png", mc().getMainRenderTarget(),
                        msg -> LOGGER.info("[showcase] {}", msg.getString()));
                restoreGui();
            }
            return t >= ticks;
        });
    }

    private static void newStop() {
        building++;
    }

    private static void biomeStop(ResourceKey<Biome> biome, String name, String tagline, int colour, double angle,
                                  String creature, String[] dayNotes, String[] nightNotes) {
        newStop();
        int number = ++stopCount;
        String id = biome.location().getPath();
        boolean forest = biome == ModBiomes.MOONLIGHT_FOREST;
        fadeOut(false, "Flying to " + name + "…");
        once(false, () -> {
            stopNumber = number;
            command("time set " + DAY);
        });
        travel(false, name, (level, current) -> {
            BlockPos spot = DevWorld.locateBiome(level, biome, level.getSharedSpawnPos());
            return spot == null ? null : orbitAround(level, spot,
                    forest ? Heightmap.Types.MOTION_BLOCKING : Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 28, 9, angle, 0.45);
        });
        arrive(true, name, tagline, colour, creature == null ? null : () -> summonAround(creature, 4), dayNotes);
        show(true, 230, id + "_day");
        if (nightNotes != null) {
            fadeOut(true, "Night falls…");
            once(true, () -> command("time set " + NIGHT));
            travel(true, name + " by night", (level, current) -> current == null ? null
                    : orbitAround(level, current.spot(), Heightmap.Types.MOTION_BLOCKING, forest ? 20 : 22, forest ? 3 : 4,
                    current.startAngle() + 150, 0.5));
            arrive(true, name, "…by night", colour, () -> summonAround("moon_wisp", 3), nightNotes);
            show(true, 210, id + "_night");
        }
    }

    private static void structureStop(ResourceKey<Structure> key, String name, String tagline, int colour, double angle,
                                      boolean night, String... notes) {
        newStop();
        int number = ++stopCount;
        boolean clearing = key == ModStructures.MOONSTONE_CIRCLE;
        fadeOut(false, "Flying to the " + name + "…");
        once(false, () -> {
            stopNumber = number;
            command("time set " + (night ? NIGHT : DAY));
        });
        travel(false, name, (level, current) -> {
            BlockPos located = DevWorld.locateStructure(level, key, level.getSharedSpawnPos());
            BoundingBox box = located == null ? null : DevWorld.structureBox(level, key, located);
            if (box == null) {
                return null;
            }
            BlockPos c = box.getCenter();
            Vec3 focus = new Vec3(c.getX() + 0.5, box.minY() + 1.5, c.getZ() + 0.5);
            double half = Math.max(box.getXSpan(), box.getZSpan()) / 2.0;
            if (clearing) {
                // Forest clearings are roofed by the canopy around them: circle low, inside the clearing, on the
                // ring with the clearest views (canopy can hang into some clearings).
                double[] ring = DevWorld.clearingOrbit(level, box);
                return new Orbit(c, focus, ring[0], ring[1], angle, 0.6, box);
            }
            double radius = half + 9;
            int top = Math.max(box.maxY(), DevWorld.ringTop(level, c.getX(), c.getZ(), radius));
            return new Orbit(c, focus, radius, top + 5, angle, 0.45, box);
        });
        arrive(true, name, tagline, colour, null, notes);
        show(true, 220, key.location().getPath());
    }

    static {
        // Title screen → new world.
        add(false, t -> mc().getOverlay() == null && mc().screen instanceof TitleScreen);
        once(false, () -> {
            String folder;
            try {
                folder = FileUtil.findAvailableName(mc().getLevelSource().getBaseDir(), "Fairy Garden Showcase", "");
            } catch (IOException e) {
                folder = "fairygarden_showcase_" + System.currentTimeMillis();
            }
            LOGGER.info("[showcase] creating world '{}'", folder);
            LevelSettings settings = new LevelSettings("Fairy Garden Showcase", GameType.CREATIVE, false, Difficulty.PEACEFUL,
                    true, new GameRules(), WorldDataConfiguration.DEFAULT);
            mc().createWorldOpenFlows().createFreshLevel(folder, settings, new WorldOptions(SEED, true, false),
                    WorldPresets::createNormalWorldDimensions);
        });
        add(false, t -> mc().level != null && mc().player != null && mc().screen == null);
        once(false, () -> {
            state = State.TOURING;
            fade = fadeO = fadeTarget = 1;
            fadeCaption = Component.literal("Preparing the tour…");
            while (mc().options.keyShift.consumeClick()) {
                // drop presses from before the tour
            }
            onServerPlayer((server, player) -> player.setGameMode(GameType.SPECTATOR));
            command("gamerule doDaylightCycle false");
            command("gamerule doWeatherCycle false");
            command("gamerule announceAdvancements false");
            command("weather clear");
            command("time set " + DAY);
        });

        // Intro: circle the spawn.
        newStop();
        travel(false, "spawn", (level, current) -> orbitAround(level, level.getSharedSpawnPos(),
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 30, 12, 210, 0.4));
        arrive(true, "Fairy Garden", "A guided tour · Sugarplum Studios", PINK, null,
                "This tour flies through the five Fairy Garden biomes and four hidden structures (about 3 minutes).",
                "Press Sneak (Shift) to skip a stop, Esc to pause.",
                "When it ends, the world is yours to play in.");
        hold(true, 150);

        biomeStop(ModBiomes.BLOSSOM_VALLEY, "Blossom Valley", "Blossom trees on flowery hills", PINK, 200, "butterfly",
                new String[]{
                        "Pink and snow-white blossom trees, some with bee nests",
                        "Petals drift down and carpet the ground; little streams wind through",
                        "Butterflies flutter between the flowers by day"}, null);
        biomeStop(ModBiomes.LAVENDER_FIELDS, "Lavender Fields", "Rows of lavender under a lavender sky", 0xC9A7F0, 30, "butterfly",
                new String[]{
                        "Lavender and tall lavender, starflowers and bluebells",
                        "Listen for the warm breeze and passing bees",
                        "Lavender makes tea, bales and giant petals"}, null);
        biomeStop(ModBiomes.STRAWBERRY_MEADOW, "Strawberry Meadow", "Strawberries, buttercups and fairy rings", 0xFF8FA3, 120, "butterfly",
                new String[]{
                        "Pick strawberries from the bushes for shortcake",
                        "Fairy mushrooms, flower carpets and giant spotted mushrooms",
                        "Mushroom cottages and flower shrines hide here"}, null);
        biomeStop(ModBiomes.MOONLIGHT_FOREST, "Moonlight Forest", "A moonwood forest that wakes after dark", 0xA9B4FF, 300, null,
                new String[]{
                        "Periwinkle moonwood trees with glowing bark veins",
                        "Moonflowers stay closed by day; wait for nightfall"},
                new String[]{
                        "Moonbells, glowcaps and moonwood bark glow",
                        "Moonflowers open one by one; an open one gives a Moon Petal",
                        "Rare Moon Wisps drift between the trees"});
        biomeStop(ModBiomes.FAIRY_GLADE, "Fairy Glade", "Giant flowers, crystals and enchanted ponds", 0x9EE6C8, 60, "butterfly",
                new String[]{
                        "Crystal formations sparkle and chime when hit",
                        "Glimmer lilies float on the enchanted ponds",
                        "Giant petals soften falls; try jumping onto one"},
                new String[]{
                        "Crystal flowers and glowcaps light up the glade",
                        "Fairy sparkles float in the air",
                        "Moon Wisps visit the glade too"});

        structureStop(ModStructures.FLOWER_SHRINE, "Flower Shrine", "A crystal-flower altar in the meadows", 0xF9C0D8, 225, false,
                "Found in Blossom Valley, Lavender Fields and Strawberry Meadow",
                "The treasure chest sits behind the altar");
        structureStop(ModStructures.HIDDEN_FAIRY_GARDEN, "Hidden Fairy Garden", "A walled hedge garden with a lily pond", 0xB5E8A8, 45, false,
                "Found in the Fairy Glade and Blossom Valley",
                "Look behind the hydrangeas for the chest");
        structureStop(ModStructures.MOONSTONE_CIRCLE, "Moonstone Circle", "Standing stones around a quartz altar", 0xC8D0FF, 135, true,
                "Found deep in the Moonlight Forest, lit by moon lanterns",
                "The chest is buried beneath the altar");
        structureStop(ModStructures.MUSHROOM_COTTAGE, "Mushroom Cottage", "A fairy's house in a giant mushroom", 0xFFB3A7, 315, false,
                "Found in the Strawberry Meadow and Fairy Glade",
                "Step inside: the pantry has a chest");

        // Finale: land beside the last structure and hand over control.
        newStop();
        fadeOut(false, "Landing…");
        add(false, t -> {
            if (t == 0) {
                orbit = null;
                stopNumber = 0;
                BoundingBox box = lastBox;
                UUID id = mc().player.getUUID();
                IntegratedServer server = mc().getSingleplayerServer();
                pending = server.submit(() -> {
                    ServerPlayer player = server.getPlayerList().getPlayer(id);
                    if (player != null) {
                        land(server.overworld(), player, box);
                    }
                    return null;
                });
            }
            return pending.isDone();
        });
        commandStep("gamerule doDaylightCycle true");
        commandStep("gamerule doWeatherCycle true");
        commandStep("gamerule announceAdvancements true");
        commandStep("time set 1000");
        // The camera "visited" every biome; give the advancements back so they can be earned in play.
        commandStep("advancement revoke @s everything");
        hold(false, 20);
        once(false, () -> {
            fadeTarget = 0;
            showTitle("Welcome to Fairy Garden", "The world is yours", PINK);
            notes(PINK,
                    "The tour is over. You're in Creative: the Fairy Garden tabs hold every block and item.",
                    "/gamemode survival to play normally, /difficulty normal for monsters",
                    "/locate biome fairygarden:fairy_glade (or any biome) to find your way back");
        });
        hold(false, FADE_TICKS + 5);
        once(false, () -> {
            state = State.DONE;
            LOGGER.info("[showcase] tour finished, handing over the world");
        });
    }

    private static void commandStep(String cmd) {
        once(false, () -> command(cmd));
    }

    // ---------------------------------------------------------------- runtime helpers

    private static void restoreGui() {
        if (hidingGuiForShot) {
            hidingGuiForShot = false;
            mc().options.hideGui = guiWasHidden;
        }
    }

    private static int currentStop() {
        return index < script.size() ? script.get(index).stop() : -1;
    }

    /** Runs a command as the player on the integrated server, without any chat feedback. */
    private static void command(String cmd) {
        onServerPlayer((server, player) -> {
            CommandSourceStack source = player.createCommandSourceStack().withSuppressedOutput().withPermission(4);
            server.getCommands().performPrefixedCommand(source, cmd);
        });
    }

    private static void onServerPlayer(java.util.function.BiConsumer<IntegratedServer, ServerPlayer> task) {
        IntegratedServer server = mc().getSingleplayerServer();
        LocalPlayer local = mc().player;
        if (server == null || local == null) {
            return;
        }
        UUID id = local.getUUID();
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                task.accept(server, player);
            }
        });
    }

    private static void summonAround(String entity, int count) {
        if (orbit == null) {
            return;
        }
        Vec3 f = orbit.focus();
        for (int i = 0; i < count; i++) {
            double a = i * Math.PI * 2 / count;
            command(String.format(Locale.ROOT, "summon %s:%s %.1f %.1f %.1f", FairyGarden.MOD_ID, entity,
                    f.x + Math.cos(a) * 4, f.y + 2.5, f.z + Math.sin(a) * 4));
        }
    }

    private static void showTitle(String name, String tagline, int colour) {
        title = Component.literal(name);
        subtitle = Component.literal(tagline);
        titleColour = colour;
        titleStart = ticks;
    }

    private static void notes(int colour, String... lines) {
        mc().gui.getChat().clearMessages(false);
        for (String line : lines) {
            mc().gui.getChat().addMessage(Component.literal("✿ ").withStyle(s -> s.withColor(colour))
                    .append(Component.literal(line).withStyle(s -> s.withColor(CREAM))));
        }
    }

    private static void setOrbit(Orbit next) {
        orbit = next;
        orbitTicks = 0;
        orbiting = false;
        if (next.box() != null) {
            lastBox = next.box();
        }
        LocalPlayer player = mc().player;
        Vec3 eye = next.eye(0);
        float[] rot = lookAt(eye, next.focus());
        yaw = rot[0];
        player.moveTo(eye.x, eye.y - player.getEyeHeight(), eye.z, rot[0], rot[1]);
        player.setDeltaMovement(Vec3.ZERO);
    }

    /** Puts the camera on its orbit. Runs at the end of every client tick, so rendering interpolates smoothly. */
    private static void applyCamera() {
        LocalPlayer player = mc().player;
        if (orbit == null || player == null) {
            return;
        }
        if (orbiting) {
            orbitTicks++;
        }
        Vec3 eye = orbit.eye(orbitTicks);
        float[] rot = lookAt(eye, orbit.focus());
        yaw += Mth.wrapDegrees(rot[0] - yaw);
        player.setDeltaMovement(Vec3.ZERO);
        player.setPos(eye.x, eye.y - player.getEyeHeight(), eye.z);
        player.setYRot(yaw);
        player.setXRot(rot[1]);
        player.setYHeadRot(yaw);
    }

    private static float[] lookAt(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        float pitch = (float) -(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    private static boolean chunksReady() {
        if (orbit == null) {
            return true;
        }
        Vec3 f = orbit.focus();
        for (int k = 0; k < 5; k++) {
            double a = k * Math.PI / 2;
            double d = k == 4 ? 0 : orbit.radius() * 0.6;
            if (!mc().levelRenderer.isChunkCompiled(BlockPos.containing(f.x + Math.cos(a) * d, f.y - 1, f.z + Math.sin(a) * d))) {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------------- server-side planning (server thread)

    private static Orbit orbitAround(ServerLevel level, BlockPos spot, Heightmap.Types focusType, double radius,
                                     double height, double angle, double speed) {
        int ground = DevWorld.surfaceY(level, spot.getX(), spot.getZ(), focusType);
        int top = Math.max(ground, DevWorld.ringTop(level, spot.getX(), spot.getZ(), radius));
        BlockPos column = new BlockPos(spot.getX(), ground, spot.getZ());
        return new Orbit(column, new Vec3(spot.getX() + 0.5, ground + 1.0, spot.getZ() + 0.5), radius, top + height, angle, speed, null);
    }

    /** Switches the player to Creative and stands them on dry, open ground beside the structure (or at spawn). */
    private static void land(ServerLevel level, ServerPlayer player, BoundingBox box) {
        BlockPos target = null;
        Vec3 lookAt = null;
        if (box != null) {
            BlockPos c = box.getCenter();
            lookAt = new Vec3(c.getX() + 0.5, box.minY() + 1.5, c.getZ() + 0.5);
            int[][] sides = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            search:
            for (int d = 4; d <= 10; d++) {
                for (int[] side : sides) {
                    int x = side[0] > 0 ? box.maxX() + d : side[0] < 0 ? box.minX() - d : c.getX();
                    int z = side[1] > 0 ? box.maxZ() + d : side[1] < 0 ? box.minZ() - d : c.getZ();
                    BlockPos feet = new BlockPos(x, DevWorld.surfaceY(level, x, z, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES), z);
                    if (level.getFluidState(feet.below()).isEmpty()
                            && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                            && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()) {
                        target = feet;
                        break search;
                    }
                }
            }
        }
        if (target == null) {
            BlockPos spawn = level.getSharedSpawnPos();
            target = new BlockPos(spawn.getX(), DevWorld.surfaceY(level, spawn.getX(), spawn.getZ(),
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES), spawn.getZ());
        }
        Vec3 feet = Vec3.atBottomCenterOf(target);
        float[] rot = lookAt == null ? new float[]{player.getYRot(), 0} : lookAt(feet.add(0, player.getEyeHeight(), 0), lookAt);
        player.setGameMode(GameType.CREATIVE);
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
        player.setDeltaMovement(Vec3.ZERO);
        player.teleportTo(level, feet.x, feet.y, feet.z, rot[0], Mth.clamp(rot[1], -10, 30));
        LOGGER.info("[showcase] landed at {}", target);
    }

    // ---------------------------------------------------------------- events

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!ENABLED || event.phase != TickEvent.Phase.END || state == State.DONE && title == null) {
            return;
        }
        if (state == State.DONE) {
            ticks++;
            return;
        }
        if (state == State.TOURING) {
            if (mc().level == null || mc().player == null) {
                LOGGER.info("[showcase] left the world, tour ended");
                state = State.DONE;
                title = null;
                return;
            }
            if (mc().screen != null) {
                return; // paused while any screen is open
            }
            ticks++;
            fadeO = fade;
            fade = fade < fadeTarget ? Math.min(fadeTarget, fade + 1F / FADE_TICKS) : Math.max(fadeTarget, fade - 1F / FADE_TICKS);
            while (mc().options.keyShift.consumeClick()) {
                skipStop = currentStop();
            }
            mc().getToasts().clear();
            applyCamera();
        }
        while (index < script.size()) {
            Entry entry = script.get(index);
            if (entry.skippable() && entry.stop() == skipStop) {
                restoreGui();
                index++;
                phaseTicks = 0;
                continue;
            }
            boolean done;
            try {
                done = entry.phase().tick(phaseTicks++);
            } catch (RuntimeException e) {
                LOGGER.error("[showcase] step {} failed", index, e);
                done = true;
            }
            if (done) {
                index++;
                phaseTicks = 0;
            }
            break;
        }
    }

    @SubscribeEvent
    public static void onRenderGuiPre(RenderGuiEvent.Pre event) {
        if (!ENABLED || state != State.TOURING) {
            return;
        }
        float alpha = Mth.lerp(event.getPartialTick(), fadeO, fade);
        if (alpha <= 0.01F) {
            return;
        }
        GuiGraphics g = event.getGuiGraphics();
        int w = g.guiWidth();
        int h = g.guiHeight();
        g.fill(0, 0, w, h, (int) (alpha * 255) << 24 | DUSK);
        int textAlpha = (int) (Mth.clamp((alpha - 0.5F) * 2F, 0F, 1F) * 255);
        if (textAlpha > 8) {
            g.drawCenteredString(mc().font, fadeCaption, w / 2, h / 2 - 4, textAlpha << 24 | CREAM);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        if (!ENABLED || state == State.WAITING || mc().level == null) {
            return;
        }
        GuiGraphics g = event.getGuiGraphics();
        Font font = mc().font;
        int w = g.guiWidth();
        int h = g.guiHeight();
        if (title != null) {
            float age = ticks - titleStart + event.getPartialTick();
            float alpha = age < TITLE_IN ? age / TITLE_IN
                    : age < TITLE_IN + TITLE_STAY ? 1F : 1F - (age - TITLE_IN - TITLE_STAY) / TITLE_OUT;
            if (age > TITLE_IN + TITLE_STAY + TITLE_OUT) {
                if (state == State.DONE) {
                    title = null;
                }
            } else if (alpha > 0.04F) {
                int a = (int) (Mth.clamp(alpha, 0F, 1F) * 255) << 24;
                float scale = Math.min(3F, (w - 24F) / Math.max(1, font.width(title)));
                PoseStack pose = g.pose();
                pose.pushPose();
                pose.translate(w / 2F, h / 3F, 0);
                pose.scale(scale, scale, 1F);
                g.drawString(font, title, -font.width(title) / 2, -font.lineHeight, titleColour | a, true);
                pose.popPose();
                g.drawCenteredString(font, subtitle, w / 2, (int) (h / 3F) + 6, CREAM | a);
            }
        }
        if (state == State.TOURING) {
            String hint = stopNumber > 0
                    ? "Stop " + stopNumber + " of " + stopCount + "  ·  Sneak: skip  ·  Esc: pause"
                    : "Sneak: skip  ·  Esc: pause";
            g.drawCenteredString(font, hint, w / 2, h - 14, 0xB0000000 | CREAM);
        }
    }
}
