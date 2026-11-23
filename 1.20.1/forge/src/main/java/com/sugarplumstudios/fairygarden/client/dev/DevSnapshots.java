package com.sugarplumstudios.fairygarden.client.dev;

import com.mojang.logging.LogUtils;
import com.sugarplumstudios.fairygarden.FairyGarden;
import com.sugarplumstudios.fairygarden.worldgen.ModBiomes;
import com.sugarplumstudios.fairygarden.worldgen.ModStructures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import com.sugarplumstudios.fairygarden.platform.RegistryEntry;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Developer tool for visual checks. Only active when the client is started with {@code -Dfairygarden.devshots=true}
 * (the {@code runSnapshots} Gradle task): it creates a throwaway world, flies to every Fairy Garden biome and
 * structure, builds a gallery of every block, fills the inventory with every item, saves screenshots into
 * {@code run/screenshots/} and quits. It does nothing in normal play.
 */
@Mod.EventBusSubscriber(modid = FairyGarden.MOD_ID, value = Dist.CLIENT)
public final class DevSnapshots {
    private static final Logger LOGGER = LogUtils.getLogger();

    private record Step(int delay, BooleanSupplier ready, Runnable action) {
    }

    private static final List<Step> steps = new ArrayList<>();
    private static int index;
    private static int wait;
    private static BlockPos target;
    private static BoundingBox structureBox;

    private DevSnapshots() {
    }

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    private static void step(int delay, Runnable action) {
        steps.add(new Step(delay, () -> true, action));
    }

    private static void waitFor(BooleanSupplier ready, Runnable action) {
        steps.add(new Step(20, ready, action));
    }

    private static void shot(String name) {
        step(10, () -> Screenshot.grab(mc().gameDirectory, "fairygarden_" + name + ".png", mc().getMainRenderTarget(),
                msg -> LOGGER.info("[devshots] {}", msg.getString())));
    }

    private static void command(String cmd) {
        if (mc().player != null) {
            mc().player.connection.sendCommand(cmd);
        }
    }

    private static <T> T onServer(java.util.function.Function<ServerLevel, T> task) {
        IntegratedServer server = mc().getSingleplayerServer();
        return server.submit(() -> task.apply(server.overworld())).join();
    }

    /** Generates the column on the server and returns the y of its surface. */
    private static int surfaceY(int x, int z) {
        return onServer(level -> DevWorld.surfaceY(level, x, z, Heightmap.Types.MOTION_BLOCKING));
    }

    /** Teleports the (flying) player and hides toasts and chat so the shot is clean. */
    private static void fly(double x, double y, double z, float yaw, float pitch) {
        command(String.format(java.util.Locale.ROOT, "tp @s %.1f %.1f %.1f %.1f %.1f", x, y, z, yaw, pitch));
        mc().player.getAbilities().flying = true;
        mc().player.onUpdateAbilities();
        mc().getToasts().clear();
        mc().getTutorial().setStep(TutorialSteps.NONE);
        mc().gui.getChat().clearMessages(false);
    }

    private static void visitBiome(ResourceKey<Biome> biome, boolean night) {
        String name = biome.location().getPath();
        step(5, () -> {
            BlockPos origin = mc().player.blockPosition();
            target = onServer(level -> DevWorld.locateBiome(level, biome, origin));
            LOGGER.info("[devshots] {} at {}", name, target);
            command("time set 6000");
            if (target != null) {
                fly(target.getX() + 0.5, surfaceY(target.getX(), target.getZ()) + 14, target.getZ() + 0.5, 145, 35);
            }
        });
        step(200, () -> mc().getToasts().clear());
        shot(name + "_day");
        if (night) {
            step(5, () -> command("time set 18000"));
            step(80, () -> fly(mc().player.getX(), mc().player.getY() - 8, mc().player.getZ(), 325, 20));
            step(40, () -> mc().getToasts().clear());
            shot(name + "_night");
        }
    }

    private static void visitStructure(ResourceKey<Structure> key) {
        String name = key.location().getPath();
        step(5, () -> {
            BlockPos origin = mc().player.blockPosition();
            target = onServer(level -> DevWorld.locateStructure(level, key, origin));
            LOGGER.info("[devshots] {} near {}", name, target);
            command("time set 6000");
            if (target != null) {
                fly(target.getX() + 0.5, surfaceY(target.getX(), target.getZ()) + 20, target.getZ() + 0.5, 0, 60);
            }
        });
        step(120, () -> {
            if (target == null) {
                return;
            }
            structureBox = onServer(level -> DevWorld.structureBox(level, key, target));
            if (structureBox != null) {
                BlockPos c = structureBox.getCenter();
                // The moonstone circle is a clearing roofed by forest canopy all around, so its camera stays inside
                // the footprint, and it is shown by lantern light.
                boolean clearing = key == ModStructures.MOONSTONE_CIRCLE;
                if (clearing) {
                    command("time set 18000");
                }
                BoundingBox box = structureBox;
                net.minecraft.world.phys.Vec3 eye = onServer(level -> DevWorld.viewpoint(level, box, clearing));
                double dx = c.getX() + 0.5 - eye.x;
                double dy = structureBox.minY() + 1.5 - eye.y;
                double dz = c.getZ() + 0.5 - eye.z;
                float yaw = (float) (Math.atan2(-dx, dz) * 180.0 / Math.PI);
                float pitch = (float) (-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * 180.0 / Math.PI);
                fly(eye.x, eye.y - mc().player.getEyeHeight(), eye.z, yaw, pitch);
            }
        });
        step(140, () -> mc().getToasts().clear());
        shot(name);
    }

    static {
        waitFor(() -> mc().getOverlay() == null && mc().screen != null && mc().level == null, () -> {
            mc().options.onboardAccessibility = false;
            mc().options.pauseOnLostFocus = false;
            mc().setScreen(new TitleScreen());
        });
        step(5, () -> {
            LevelSettings settings = new LevelSettings("Fairy Garden Snapshots", GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                    new GameRules(), WorldDataConfiguration.DEFAULT);
            mc().createWorldOpenFlows().createFreshLevel("fairygarden_snapshots", settings, new WorldOptions(20261103L, true, false),
                    WorldPresets::createNormalWorldDimensions);
        });
        waitFor(() -> mc().level != null && mc().player != null && mc().screen == null, () -> {
            command("gamerule doDaylightCycle false");
            command("gamerule doWeatherCycle false");
            command("gamerule announceAdvancements false");
            command("weather clear");
            mc().options.hideGui = true;
        });
        for (ResourceKey<Biome> biome : ModBiomes.ALL) {
            boolean night = biome == ModBiomes.MOONLIGHT_FOREST || biome == ModBiomes.FAIRY_GLADE;
            visitBiome(biome, night);
        }
        for (ResourceKey<Structure> structure : ModStructures.ALL) {
            visitStructure(structure);
        }

        // Block gallery on a sky platform, by day and night.
        step(5, () -> {
            command("time set 6000");
            fly(0.5, 205, -6, 0, 30);
        });
        step(40, () -> {
            command("fill -12 200 -2 12 200 14 minecraft:grass_block");
            command("fill -12 200 15 12 200 15 minecraft:farmland");
            command("fill -12 200 16 12 200 16 minecraft:water");
            List<Block> blocks = new ArrayList<>();
            for (RegistryEntry<? extends Block> entry : com.sugarplumstudios.fairygarden.registry.ModBlocks.ALL) {
                Block block = entry.get();
                String path = entry.getId().getPath();
                if (path.contains("wall_") || path.equals("starbloom") || path.equals("glimmer_lily")) {
                    continue;
                }
                blocks.add(block);
            }
            int i = 0;
            for (Block block : blocks) {
                int x = -12 + (i % 13) * 2;
                int z = (i / 13) * 2;
                command("setblock " + x + " 201 " + z + " " + BuiltInRegistries.BLOCK.getKey(block));
                i++;
            }
            for (int age = 0; age < 4; age++) {
                command("setblock " + (-12 + age * 2) + " 201 15 fairygarden:starbloom[age=" + age + "]");
            }
            command("setblock 0 201 16 fairygarden:glimmer_lily");
            command("setblock 2 201 16 fairygarden:glimmer_lily");
        });
        step(80, () -> fly(0.5, 207, -7, 0, 40));
        shot("gallery_day");
        step(5, () -> command("time set 18000"));
        step(60, () -> {
        });
        shot("gallery_night");
        step(5, () -> fly(-6.5, 204, -2, 0, 45));
        step(40, () -> {
        });
        shot("gallery_closeup_night");
        step(5, () -> command("time set 6000"));
        step(40, () -> {
        });
        shot("gallery_closeup_day");

        // Inventory pages showing every item icon.
        step(5, () -> {
            mc().options.hideGui = false;
            command("gamemode survival");
        });
        List<List<Item>> pages = new ArrayList<>();
        step(5, () -> {
            List<Item> all = new ArrayList<>();
            for (RegistryEntry<? extends Item> entry : com.sugarplumstudios.fairygarden.registry.ModItems.ALL) {
                all.add(entry.get());
            }
            for (int start = 0; start < all.size(); start += 27) {
                pages.add(all.subList(start, Math.min(all.size(), start + 27)));
            }
            mc().getToasts().clear();
        });
        for (int page = 0; page < 5; page++) {
            int p = page;
            step(10, () -> {
                command("clear @s");
                if (p < pages.size()) {
                    for (Item item : pages.get(p)) {
                        command("give @s " + ForgeRegistries.ITEMS.getKey(item));
                    }
                }
            });
            step(30, () -> {
                if (p < pages.size()) {
                    mc().setScreen(new InventoryScreen(mc().player));
                }
            });
            step(10, () -> {
                if (p < pages.size()) {
                    Screenshot.grab(mc().gameDirectory, "fairygarden_items_" + p + ".png", mc().getMainRenderTarget(),
                            msg -> LOGGER.info("[devshots] {}", msg.getString()));
                }
            });
            step(5, () -> mc().setScreen(null));
        }
        step(5, () -> {
            LOGGER.info("[devshots] finished, quitting");
            mc().stop();
        });
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!Boolean.getBoolean("fairygarden.devshots") || event.phase != TickEvent.Phase.END || index >= steps.size()) {
            return;
        }
        // The client drops flight whenever it thinks it touched the ground, e.g. the tick before a teleport lands.
        if (mc().player != null && mc().player.getAbilities().mayfly && !mc().player.getAbilities().flying) {
            mc().player.getAbilities().flying = true;
            mc().player.onUpdateAbilities();
        }
        Step s = steps.get(index);
        if (!s.ready().getAsBoolean()) {
            return;
        }
        if (wait++ < s.delay()) {
            return;
        }
        wait = 0;
        index++;
        try {
            s.action().run();
        } catch (RuntimeException e) {
            LOGGER.error("[devshots] step {} failed", index - 1, e);
        }
    }
}
