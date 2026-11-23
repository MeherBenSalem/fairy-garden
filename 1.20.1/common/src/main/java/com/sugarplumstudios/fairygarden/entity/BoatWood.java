package com.sugarplumstudios.fairygarden.entity;

import com.sugarplumstudios.fairygarden.registry.ModBlocks;
import com.sugarplumstudios.fairygarden.registry.WoodSet;

import java.util.Locale;

/** The wood a Fairy Garden boat is made from. Stored by name so saves survive reordering. */
public enum BoatWood {
    BLOSSOM,
    MOONWOOD;

    private final String id = name().toLowerCase(Locale.ROOT);

    public String id() {
        return id;
    }

    public WoodSet woodSet() {
        return this == BLOSSOM ? ModBlocks.BLOSSOM : ModBlocks.MOONWOOD;
    }

    public static BoatWood byId(String id) {
        for (BoatWood wood : values()) {
            if (wood.id.equals(id)) {
                return wood;
            }
        }
        return BLOSSOM;
    }
}
