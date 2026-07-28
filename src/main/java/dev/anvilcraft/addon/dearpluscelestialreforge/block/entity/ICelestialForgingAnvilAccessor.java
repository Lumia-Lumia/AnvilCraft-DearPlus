package dev.anvilcraft.addon.dearpluscelestialreforge.block.entity;

import dev.dubhe.anvilcraft.block.entity.celestial.CelestialBodyData;
import dev.dubhe.anvilcraft.block.entity.celestial.PlanetaryResourceSet;

import javax.annotation.Nullable;

/**
 * 向 CelestialForgingAnvilBlockEntity 添加公开访问器的接口，
 * 供重锻面板读取内部状态。
 */
public interface ICelestialForgingAnvilAccessor {
    /** 锻星砧是否正在搜索中 */
    boolean cfaIsSearching();

    /** 获取当前天体数据 */
    @Nullable
    CelestialBodyData cfaGetBodyData();

    /** 获取当前行星资源集 */
    @Nullable
    PlanetaryResourceSet cfaGetResourceSet();
}
