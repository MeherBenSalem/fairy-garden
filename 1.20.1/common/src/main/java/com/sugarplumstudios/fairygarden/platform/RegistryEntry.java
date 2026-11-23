package com.sugarplumstudios.fairygarden.platform;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A registered object, filled in by the loader's registry (immediately on Fabric, during the registry events on
 * Forge and NeoForge). Read it with {@link #get()} once the registries are populated.
 */
public final class RegistryEntry<T> implements Supplier<T> {
    private final ResourceLocation id;
    private final Supplier<? extends T> source;

    public RegistryEntry(ResourceLocation id, Supplier<? extends T> source) {
        this.id = id;
        this.source = source;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public T get() {
        return Objects.requireNonNull(source.get(), () -> "Registry entry not yet available: " + id);
    }
}
