package com.sugarplumstudios.fairygarden.datagen.structure;

import com.google.common.hash.Hashing;
import com.sugarplumstudios.fairygarden.FairyGarden;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.NbtIo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/** Writes every {@link StructureBlueprints blueprint} to {@code data/fairygarden/structures/<name>.nbt}. */
public class StructureTemplateProvider implements DataProvider {
    private final PackOutput output;

    public StructureTemplateProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path root = output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(FairyGarden.MOD_ID).resolve("structures");
        try {
            for (Blueprint blueprint : StructureBlueprints.all()) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                NbtIo.writeCompressed(blueprint.toNbt(), bytes);
                byte[] data = bytes.toByteArray();
                cache.writeIfNeeded(root.resolve(blueprint.name + ".nbt"), data, Hashing.sha1().hashBytes(data));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "Fairy Garden structure templates";
    }
}
