package dev.anvilcraft.addon.dearpluscelestialreforge.block.entity;

import dev.anvilcraft.addon.dearpluscelestialreforge.AnvilCraftDearPlusCelestialReforge;
import dev.anvilcraft.addon.dearpluscelestialreforge.block.ReforgingPanelBlock;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.ModBlockEntities;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.ModMenuTypes;
import dev.anvilcraft.addon.dearpluscelestialreforge.init.ReforgingFilter;
import dev.anvilcraft.addon.dearpluscelestialreforge.inventory.ReforgingFilterData;
import dev.anvilcraft.addon.dearpluscelestialreforge.inventory.ReforgingPanelMenu;
import dev.dubhe.anvilcraft.block.cfa.CelestialForgingAnvilBlock;
import dev.dubhe.anvilcraft.block.entity.CelestialForgingAnvilBlockEntity;
import dev.dubhe.anvilcraft.block.entity.celestial.*;
import dev.dubhe.anvilcraft.block.state.Cube323PartHalf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ReforgingPanelBlockEntity extends BlockEntity implements MenuProvider {
    private static final int REFORGE_COOLDOWN = 250;       // 重锻冷却 ticks
    private static final int REDSTONE_CACHE_TICKS = 5;     // 红石缓存 ticks

    private ReforgingFilterData filterData = new ReforgingFilterData();
    private boolean isEmitting = false;
    private int reforgeCooldown = 0;
    private int redstoneCheckTick = 0;
    private boolean hasRedstoneSignal = false;

    public ReforgingPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ========== 公开 API ==========

    public ReforgingFilterData getFilterData() {
        return filterData;
    }

    public void setFilterData(ReforgingFilterData filterData) {
        this.filterData = filterData;
        setChanged();
    }

    public boolean isEmitting() {
        return isEmitting;
    }

    public void markRedstoneDirty() {
        redstoneCheckTick = 0;
    }

    // ========== 核心 Tick 逻辑 ==========

    public void tick() {
        if (level == null || level.isClientSide()) return;
        Level lvl = level;

        // 递减冷却
        if (reforgeCooldown > 0) reforgeCooldown--;

        // 红石缓存刷新
        redstoneCheckTick--;
        if (redstoneCheckTick <= 0) {
            hasRedstoneSignal = lvl.hasNeighborSignal(worldPosition);
            redstoneCheckTick = REDSTONE_CACHE_TICKS;
        }

        if (!hasRedstoneSignal) {
            // 无红石信号：停止发射
            setEmitting(false);
            return;
        }

        // 有红石信号且冷却已到 → 执行逻辑
        if (reforgeCooldown > 0) return;

        CelestialForgingAnvilBlockEntity cfaController = findCfaController();
        if (cfaController == null) {
            // 找不到 CFA → 发射信号表示错误
            setEmitting(true);
            return;
        }

        ICelestialForgingAnvilAccessor cfaAccess = (ICelestialForgingAnvilAccessor) cfaController;

        // 如果 CFA 正在搜索中，等待
        if (cfaAccess.cfaIsSearching()) {
            setEmitting(false);
            return;
        }

        CelestialBodyData body = cfaAccess.cfaGetBodyData();
        PlanetaryResourceSet resources = cfaAccess.cfaGetResourceSet();

        // 判断当前天体是否匹配筛选条件
        boolean matches = matchesFilter(body, resources);

        if (matches) {
            // 匹配 → 发射红石信号（通知已找到目标）
            setEmitting(true);
        } else {
            // 不匹配 → 尝试重锻
            // 检查是否可以重锻（有砧子数、未锁定）
            if (canReforge(cfaController)) {
                cfaController.startSearch();
                reforgeCooldown = REFORGE_COOLDOWN;
                setEmitting(false);
            } else {
                // 无法重锻 → 发射错误信号
                setEmitting(true);
            }
        }
    }

    // ========== 筛选比对 ==========

    private boolean matchesFilter(@Nullable CelestialBodyData body, @Nullable PlanetaryResourceSet resources) {
        if (body == null) return false;

        for (ReforgingFilter filter : ReforgingFilter.values()) {
            ReforgingFilterData.Condition cond = filterData.get(filter);
            if (!cond.enabled()) continue;

            boolean conditionMet = switch (filter) {
                case MAGNETIC_FIELD -> body.magneticFieldStrength() == cond.value();
                case ROTATION_SPEED -> body.rotationSpeed() == cond.value();
                case TEMPERATURE -> {
                    if (!(body instanceof RockyPlanetData rocky)) yield false;
                    yield rocky.temperature().ordinal() == cond.value();
                }
                case FLUID_COVERAGE -> {
                    if (!(body instanceof RockyPlanetData rocky)) yield false;
                    yield rocky.liquidCoverage().ordinal() == cond.value();
                }
                case RESOURCE -> {
                    if (resources == null) yield false;
                    boolean hasBiological = !resources.getBiologicalItems().isEmpty()
                        || !resources.getBiologicalFluids().isEmpty();
                    boolean hasOffering = !resources.getOfferings().isEmpty();
                    boolean hasWasteland = !resources.getWastelandItems().isEmpty();
                    boolean hasCivilization = hasOffering || hasWasteland;
                    yield switch (cond.value()) {
                        case 0 -> !hasBiological && !hasCivilization; // 无
                        case 1 -> hasBiological;                       // 生物资源
                        case 2 -> hasCivilization;                     // 文明资源
                        case 3 -> hasBiological || hasCivilization;    // 任意资源
                        default -> false;
                    };
                }
            };

            if (!conditionMet) return false;
        }
        return true;
    }

    // ========== CFA 交互 ==========

    /**
     * 沿 FACING 反向查找锻星砧控制器。
     */
    @Nullable
    private CelestialForgingAnvilBlockEntity findCfaController() {
        if (level == null) return null;
        Direction towardsCfa = getBlockState().getValue(ReforgingPanelBlock.FACING).getOpposite();
        BlockPos adjacentPos = worldPosition.relative(towardsCfa);
        BlockState adjacentState = level.getBlockState(adjacentPos);
        if (adjacentState.getBlock() instanceof CelestialForgingAnvilBlock cfaBlock) {
            Cube323PartHalf half = adjacentState.getValue(CelestialForgingAnvilBlock.HALF);
            BlockPos controllerPos = adjacentPos.offset(half.getOffset().multiply(-1));
            BlockEntity be = level.getBlockEntity(controllerPos);
            if (be instanceof CelestialForgingAnvilBlockEntity cfaBe) {
                return cfaBe;
            }
        }
        return null;
    }

    /**
     * 检查锻星砧是否可以开始重锻。
     */
    private boolean canReforge(CelestialForgingAnvilBlockEntity cfa) {
        // 检查砧子：至少有一个砧位有 1-64 数量的生物
        // 这里简单检查四个砧位中是否有任何一个计数 > 0
        //（AnvilCraft 内部会做详细预检，这里只做快速检查）
        for (int i = 0; i < 4; i++) {
            if (cfa.getAnvilCount(i) > 0) return true;
        }
        return false;
    }

    // ========== 红石发射管理 ==========

    private void setEmitting(boolean emitting) {
        if (this.isEmitting != emitting) {
            this.isEmitting = emitting;
            setChanged();
            if (level != null) {
                BlockState state = level.getBlockState(worldPosition);
                level.setBlock(worldPosition, state.setValue(
                    dev.anvilcraft.addon.dearpluscelestialreforge.block.ReforgingPanelBlock.POWERED, emitting
                ), 3);
                // 通知邻居更新红石信号
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            }
        }
    }

    // ========== MenuProvider ==========

    @Override
    public Component getDisplayName() {
        return Component.translatable("container." + AnvilCraftDearPlusCelestialReforge.MOD_ID + ".reforging_panel");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ReforgingPanelMenu(ModMenuTypes.REFORGING_PANEL.get(), containerId, inventory, this);
    }

    // ========== NBT 序列化 ==========

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("filter_data", filterData.toTag());
        tag.putBoolean("emitting", isEmitting);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("filter_data")) {
            filterData = ReforgingFilterData.fromTag(tag.getCompound("filter_data"));
        }
        isEmitting = tag.getBoolean("emitting");
    }
}
