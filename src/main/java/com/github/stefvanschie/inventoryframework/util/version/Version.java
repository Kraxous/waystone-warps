package com.github.stefvanschie.inventoryframework.util.version;

import com.github.stefvanschie.inventoryframework.exception.UnsupportedVersionException;
import org.bukkit.Bukkit;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;

/**
 * Compatibility override for InventoryFramework 0.11.6.
 *
 * <p>IF 0.11.6 only knows server versions up to 1.21.11. Paper 26.x still works
 * with the same IF implementation path for the GUI types this plugin uses, but
 * IF rejects it before constructing any GUI. Keep this class aligned with IF's
 * enum values and map newer Paper versions to the latest known implementation.
 */
public enum Version {
    V1_16_1,
    V1_16_2_3,
    V1_16_4_5,
    V1_17_0,
    V1_17_1,
    V1_18_0,
    V1_18_1,
    V1_18_2,
    V1_19_0,
    V1_19_1,
    V1_19_2,
    V1_19_3,
    V1_19_4,
    V1_20_0,
    V1_20_1,
    V1_20_2,
    V1_20_3_4,
    V1_20_5,
    V1_20_6,
    V1_21_0,
    V1_21_1,
    V1_21_2_3,
    V1_21_4,
    V1_21_5,
    V1_21_6_8,
    V1_21_9_10,
    V1_21_11;

    private static final Collection<Version> MODERN_SMITHING_TABLE_VERSIONS = EnumSet.of(
            V1_19_4,
            V1_20_0, V1_20_1, V1_20_2, V1_20_3_4, V1_20_5, V1_20_6,
            V1_21_0, V1_21_1, V1_21_2_3, V1_21_4, V1_21_5, V1_21_6_8, V1_21_9_10, V1_21_11
    );

    @NotNull
    private static final Collection<@NotNull Version> LEGACY_SMITHING_TABLE_VERSIONS = EnumSet.of(
            V1_16_1, V1_16_2_3, V1_16_4_5,
            V1_17_0, V1_17_1,
            V1_18_0, V1_18_1, V1_18_2,
            V1_19_0, V1_19_1, V1_19_2, V1_19_3, V1_19_4
    );

    @NotNull
    private static final Collection<@NotNull Version> INTERFACE_INVENTORY_VIEW = EnumSet.of(
            V1_21_0, V1_21_1, V1_21_2_3, V1_21_4, V1_21_5, V1_21_6_8, V1_21_9_10, V1_21_11
    );

    @Contract(pure = true)
    public boolean isInventoryViewInterface() {
        return INTERFACE_INVENTORY_VIEW.contains(this);
    }

    public boolean isOlderThan(@NotNull Version version) {
        return ordinal() < version.ordinal();
    }

    boolean existsModernSmithingTable() {
        return MODERN_SMITHING_TABLE_VERSIONS.contains(this);
    }

    @Contract(pure = true)
    boolean existsLegacySmithingTable() {
        return LEGACY_SMITHING_TABLE_VERSIONS.contains(this);
    }

    @NotNull
    @Contract(pure = true)
    public static Version getVersion() {
        String version = Bukkit.getBukkitVersion().split("-")[0];

        switch (version) {
            case "1.16.1":
                return V1_16_1;
            case "1.16.2":
            case "1.16.3":
                return V1_16_2_3;
            case "1.16.4":
            case "1.16.5":
                return V1_16_4_5;
            case "1.17":
                return V1_17_0;
            case "1.17.1":
                return V1_17_1;
            case "1.18":
                return V1_18_0;
            case "1.18.1":
                return V1_18_1;
            case "1.18.2":
                return V1_18_2;
            case "1.19":
                return V1_19_0;
            case "1.19.1":
                return V1_19_1;
            case "1.19.2":
                return V1_19_2;
            case "1.19.3":
                return V1_19_3;
            case "1.19.4":
                return V1_19_4;
            case "1.20":
                return V1_20_0;
            case "1.20.1":
                return V1_20_1;
            case "1.20.2":
                return V1_20_2;
            case "1.20.3":
            case "1.20.4":
                return V1_20_3_4;
            case "1.20.5":
                return V1_20_5;
            case "1.20.6":
                return V1_20_6;
            case "1.21":
                return V1_21_0;
            case "1.21.1":
                return V1_21_1;
            case "1.21.2":
            case "1.21.3":
                return V1_21_2_3;
            case "1.21.4":
                return V1_21_4;
            case "1.21.5":
                return V1_21_5;
            case "1.21.6":
            case "1.21.7":
            case "1.21.8":
                return V1_21_6_8;
            case "1.21.9":
            case "1.21.10":
                return V1_21_9_10;
            case "1.21.11":
                return V1_21_11;
            default:
                return getFallbackVersion(version);
        }
    }

    @NotNull
    private static Version getFallbackVersion(@NotNull String version) {
        String[] parts = version.split("\\.");
        int major = parsePart(parts, 0);
        int minor = parsePart(parts, 1);

        if (major > 1 || (major == 1 && minor > 21)) {
            return V1_21_11;
        }

        throw new UnsupportedVersionException("The server version provided is not supported");
    }

    private static int parsePart(@NotNull String[] parts, int index) {
        if (index >= parts.length) {
            return 0;
        }

        try {
            return Integer.parseInt(parts[index]);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
