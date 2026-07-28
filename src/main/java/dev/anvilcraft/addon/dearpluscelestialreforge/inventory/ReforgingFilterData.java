package dev.anvilcraft.addon.dearpluscelestialreforge.inventory;

import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.ReforgingFilter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

/**
 * 重锻面板筛选条件数据。
 * 每个条件存储是否启用(enable)和当前值(value)。
 */
public class ReforgingFilterData {
    public static final String TAG_FILTERS = "filters";
    public static final String TAG_ENABLED = "enabled";
    public static final String TAG_VALUE = "value";

    private final EnumMap<ReforgingFilter, Condition> conditions;

    public ReforgingFilterData() {
        this.conditions = new EnumMap<>(ReforgingFilter.class);
        for (ReforgingFilter filter : ReforgingFilter.values()) {
            conditions.put(filter, new Condition(false, 0));
        }
    }

    public Condition get(ReforgingFilter filter) {
        return conditions.get(filter);
    }

    public void set(ReforgingFilter filter, boolean enabled, int value) {
        conditions.put(filter, new Condition(enabled, value));
    }

    public void setEnabled(ReforgingFilter filter, boolean enabled) {
        Condition c = conditions.get(filter);
        conditions.put(filter, new Condition(enabled, c.value()));
    }

    public void setValue(ReforgingFilter filter, int value) {
        Condition c = conditions.get(filter);
        conditions.put(filter, new Condition(c.enabled(), value));
    }

    public Map<ReforgingFilter, Condition> getAll() {
        return conditions.clone();
    }

    /**
     * 序列化到 NBT
     */
    public CompoundTag toTag() {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        for (var entry : conditions.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", entry.getKey().getSerializedName());
            tag.putBoolean(TAG_ENABLED, entry.getValue().enabled());
            tag.putInt(TAG_VALUE, entry.getValue().value());
            list.add(tag);
        }
        root.put(TAG_FILTERS, list);
        return root;
    }

    /**
     * 从 NBT 反序列化
     */
    public static ReforgingFilterData fromTag(CompoundTag root) {
        ReforgingFilterData data = new ReforgingFilterData();
        if (!root.contains(TAG_FILTERS, Tag.TAG_LIST)) return data;
        ListTag list = root.getList(TAG_FILTERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            String typeName = tag.getString("type");
            for (ReforgingFilter filter : ReforgingFilter.values()) {
                if (filter.getSerializedName().equals(typeName)) {
                    data.set(filter, tag.getBoolean(TAG_ENABLED), tag.getInt(TAG_VALUE));
                    break;
                }
            }
        }
        return data;
    }

    /**
     * 单个筛选条件
     */
    public record Condition(boolean enabled, int value) {
    }
}
