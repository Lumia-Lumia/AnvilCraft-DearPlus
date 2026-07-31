package dev.anvilcraft.addon.dearpluscelestialreforge.init;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum ReforgingFilter implements StringRepresentable {
    MAGNETIC_FIELD(5),
    ROTATION_SPEED(4),
    RESOURCE(3);

    private final int maxValue;

    ReforgingFilter(int maxValue) {
        this.maxValue = maxValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static String getValueDisplayName(ReforgingFilter filter, int value) {
        return switch (filter) {
            case MAGNETIC_FIELD -> switch (value) {
                case 0 -> "gui.reforging_panel.magnetic.none";
                case 1 -> "gui.reforging_panel.magnetic.barely";
                case 2 -> "gui.reforging_panel.magnetic.weak";
                case 3 -> "gui.reforging_panel.magnetic.medium";
                case 4 -> "gui.reforging_panel.magnetic.strong";
                case 5 -> "gui.reforging_panel.magnetic.very_strong";
                default -> "???";
            };
            case ROTATION_SPEED -> switch (value) {
                case 0 -> "gui.reforging_panel.speed.very_slow";
                case 1 -> "gui.reforging_panel.speed.slow";
                case 2 -> "gui.reforging_panel.speed.medium";
                case 3 -> "gui.reforging_panel.speed.fast";
                case 4 -> "gui.reforging_panel.speed.very_fast";
                default -> "???";
            };
            case RESOURCE -> switch (value) {
                case 0 -> "gui.reforging_panel.resource.none";
                case 1 -> "gui.reforging_panel.resource.biological";
                case 2 -> "gui.reforging_panel.resource.civilization";
                case 3 -> "gui.reforging_panel.resource.any";
                default -> "???";
            };
        };
    }
}
