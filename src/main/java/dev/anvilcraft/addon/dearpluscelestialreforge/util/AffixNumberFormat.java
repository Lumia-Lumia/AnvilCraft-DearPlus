package dev.anvilcraft.addon.dearpluscelestialreforge.util;

import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;

/**
 * 词条等级数字显示格式。
 *
 * <p>由配置 {@code affixNumberStyle} 控制：</p>
 * <ul>
 *     <li>0（默认，混合）：等级 ≤ 10 用罗马数字，> 10 用阿拉伯数字；</li>
 *     <li>1：全部用罗马数字；</li>
 *     <li>2：全部用阿拉伯数字。</li>
 * </ul>
 */
public final class AffixNumberFormat {
    private AffixNumberFormat() {
    }

    /** 将词条等级格式化为罗马/阿拉伯数字文本；达到上限时显示 {@code MAX}。 */
    public static String format(int level, int maxLevel) {
        if (level >= maxLevel) {
            return "MAX";
        }
        int style = AnvilCraftDearPlusCelestialReforge.CONFIG.affixNumberStyle;
        boolean useRoman = switch (style) {
            case 1 -> true;
            case 2 -> false;
            default -> level <= 10;
        };
        return useRoman ? toRoman(level) : String.valueOf(level);
    }

    /** 整数转罗马数字（支持 1~3999；0 或超出范围用阿拉伯数字）。 */
    private static String toRoman(int n) {
        if (n <= 0 || n >= 4000) {
            return String.valueOf(n);
        }
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (n >= values[i]) {
                n -= values[i];
                sb.append(symbols[i]);
            }
        }
        return sb.toString();
    }
}
