package dev.anvilcraft.addon.dearpluscelestialreforge;

import dev.anvilcraft.lib.v2.config.BoundedDiscrete;
import dev.anvilcraft.lib.v2.config.Comment;
import dev.anvilcraft.lib.v2.config.Config;

@Config(name = AnvilCraftDearPlusCelestialReforge.MOD_ID)
public class AddonConfig {
    @Comment("How to display affix levels: 0 = mixed (Roman for ≤ 10, Arabic for > 10), 1 = all Roman, 2 = all Arabic")
    @BoundedDiscrete(max = 2, min = 0)
    public int affixNumberStyle = 0;
}
