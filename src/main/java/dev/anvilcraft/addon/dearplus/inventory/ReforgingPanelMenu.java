package dev.anvilcraft.addon.dearplus.inventory;

import dev.anvilcraft.addon.dearplus.block.ReforgingPanelBlock;
import dev.anvilcraft.addon.dearplus.block.entity.ReforgingPanelBlockEntity;
import dev.anvilcraft.addon.dearplus.init.ModMenuTypes;
import dev.anvilcraft.addon.dearplus.init.ReforgingFilter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ReforgingPanelMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = ReforgingFilter.values().length * 2; // 12

    private final ReforgingPanelBlockEntity blockEntity;
    private final ContainerData filterData;

    // 服务端构造函数
    public ReforgingPanelMenu(@Nullable MenuType<?> menuType, int containerId, Inventory inv, ReforgingPanelBlockEntity be) {
        super(menuType, containerId);
        this.blockEntity = be;
        this.filterData = new SimpleContainerData(DATA_COUNT);
        loadFromBe();
        addDataSlots();
    }

    // 客户端构造函数
    public ReforgingPanelMenu(@Nullable MenuType<?> menuType, int containerId, Inventory inv, RegistryFriendlyByteBuf buffer) {
        super(menuType, containerId);
        var be = inv.player.level().getBlockEntity(buffer.readBlockPos());
        this.blockEntity = (ReforgingPanelBlockEntity) be;
        this.filterData = new SimpleContainerData(DATA_COUNT);
        loadFromBe();
        addDataSlots();
    }

    private void addDataSlots() {
        for (int i = 0; i < DATA_COUNT; i++) {
            this.addDataSlot(DataSlot.forContainer(filterData, i));
        }
    }

    private void loadFromBe() {
        if (blockEntity != null) {
            int idx = 0;
            for (ReforgingFilter filter : ReforgingFilter.values()) {
                var cond = blockEntity.getFilterData().get(filter);
                filterData.set(idx++, cond.enabled() ? 1 : 0);
                filterData.set(idx++, cond.value());
            }
        }
    }

    public void saveToBe() {
        if (blockEntity != null) {
            var data = new ReforgingFilterData();
            int idx = 0;
            for (ReforgingFilter filter : ReforgingFilter.values()) {
                boolean enabled = filterData.get(idx++) != 0;
                int value = filterData.get(idx++);
                data.set(filter, enabled, value);
            }
            blockEntity.setFilterData(data);
        }
    }

    public ContainerData getFilterData() {
        return filterData;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        int ordinal = id % 10;
        if (ordinal < 0 || ordinal >= ReforgingFilter.values().length) return false;
        ReforgingFilter filter = ReforgingFilter.values()[ordinal];

        if (id < 10) {
            int idx = ordinal * 2;
            filterData.set(idx, filterData.get(idx) == 0 ? 1 : 0);
            saveToBe();
            return true;
        } else if (id < 20) {
            int idx = ordinal * 2 + 1;
            int max = filter.getMaxValue();
            int newVal = (filterData.get(idx) + 1) % (max + 1);
            filterData.set(idx, newVal);
            saveToBe();
            return true;
        } else if (id < 30) {
            // 反向切换（右键/滚轮向下）
            int idx = ordinal * 2 + 1;
            int max = filter.getMaxValue();
            int newVal = (filterData.get(idx) - 1 + max + 1) % (max + 1);
            filterData.set(idx, newVal);
            saveToBe();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        // 面板方块仍在且玩家在交互范围内（8 格），否则 GUI 关闭
        return blockEntity != null
            && blockEntity.getLevel() != null
            && blockEntity.getBlockState().getBlock() instanceof ReforgingPanelBlock
            && player.canInteractWithBlock(blockEntity.getBlockPos(), 8.0F);
    }
}
