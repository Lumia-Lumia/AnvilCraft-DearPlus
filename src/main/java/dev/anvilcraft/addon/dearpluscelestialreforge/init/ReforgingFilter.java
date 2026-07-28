package dev.anvilcraft.addon.dearpluscelestialreforge.init;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * 重锻面板的筛选条件类型。
 */
public enum ReforgingFilter implements StringRepresentable {
    /** 磁场：无/几乎没有/非常弱/弱/中等/强/非常强 */
    MAGNETIC_FIELD(6),
    /** 转速：非常慢/慢/中等/快/非常快 */
    ROTATION_SPEED(4),
    /** 温度：极寒/寒冷/温和/炎热/焦土 */
    TEMPERATURE(4),
    /** 流体覆盖率：无/低/中等/高 */
    FLUID_COVERAGE(3),
    /** 行星特殊资源：无/生物资源/文明资源/任意资源 */
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

    // ========== 各条件值的展示名称 ==========

    public static String getValueDisplayName(ReforgingFilter filter, int value) {
        return switch (filter) {
            case MAGNETIC_FIELD -> switch (value) {
                case 0 -> "gui.reforging_panel.magnetic.none";
                case 1 -> "gui.reforging_panel.magnetic.barely";
                case 2 -> "gui.reforging_panel.magnetic.very_weak";
                case 3 -> "gui.reforging_panel.magnetic.weak";
                case 4 -> "gui.reforging_panel.magnetic.medium";
                case 5 -> "gui.reforging_panel.magnetic.strong";
                case 6 -> "gui.reforging_panel.magnetic.very_strong";
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
            case TEMPERATURE -> switch (value) {
                case 0 -> "gui.reforging_panel.temp.freezing";
                case 1 -> "gui.reforging_panel.temp.cold";
                case 2 -> "gui.reforging_panel.temp.mild";
                case 3 -> "gui.reforging_panel.temp.hot";
                case 4 -> "gui.reforging_panel.temp.scorched";
                default -> "???";
            };
            case FLUID_COVERAGE -> switch (value) {
                case 0 -> "gui.reforging_panel.fluid.none";
                case 1 -> "gui.reforging_panel.fluid.low";
                case 2 -> "gui.reforging_panel.fluid.medium";
                case 3 -> "gui.reforging_panel.fluid.high";
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
