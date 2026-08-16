package dev.anvilcraft.addon.dearplus.block;

import com.mojang.serialization.MapCodec;
import dev.anvilcraft.addon.dearplus.block.entity.ReforgingPanelBlockEntity;
import dev.anvilcraft.addon.dearplus.init.ModBlockEntities;
import dev.anvilcraft.lib.v2.util.ShapeUtil;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.block.cfa.CelestialForgingAnvilBlock;
import dev.dubhe.anvilcraft.block.state.Cube323PartHalf;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ReforgingPanelBlock extends HorizontalDirectionalBlock implements EntityBlock, IHammerRemovable {
    public static final MapCodec<ReforgingPanelBlock> CODEC = simpleCodec(ReforgingPanelBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty EMITTING = BlockStateProperties.ENABLED;

    // 物流接口碰撞箱（朝北）
    private static final VoxelShape NORTH_SHAPE = ShapeUtil.merge(
        new AABB(0, 0, 2, 16, 4, 16),
        new AABB(0, 4, 8, 16, 8, 16),
        new AABB(0, 8, 6, 16, 12, 16),
        new AABB(7, 2, -1, 9, 3.75, 0),
        new AABB(3, 0, 0, 13, 1.75, 2),
        new AABB(5, 0, -2, 11, 1.75, 0),
        new AABB(7, 0, -4, 9, 1.75, -2),
        new AABB(4, 8, 6, 12, 16, 14),
        new AABB(5, 7, 1, 11, 13, 7),
        new AABB(3, 14, 5, 13, 17, 8),
        new AABB(3, 17, 8, 13, 19, 10),
        new AABB(3, 19, 10, 13, 22, 13)
    );
    private static final VoxelShape EAST_SHAPE = ShapeUtil.rotate(Direction.Axis.Y, 270, NORTH_SHAPE);
    private static final VoxelShape SOUTH_SHAPE = ShapeUtil.rotate(Direction.Axis.Y, 180, NORTH_SHAPE);
    private static final VoxelShape WEST_SHAPE = ShapeUtil.rotate(Direction.Axis.Y, 90, NORTH_SHAPE);

    public ReforgingPanelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
            .setValue(FACING, Direction.NORTH)
            .setValue(POWERED, false)
            .setValue(EMITTING, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, POWERED, EMITTING));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    // ========== 放置限制：仅贴紧锻星砧底边 ==========

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (player == null) return null;

        // 检查四个水平方向是否有锻星砧底边方块
        List<Direction> cfaDirs = new ArrayList<>();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos cfaPos = pos.relative(dir);
            BlockState cfaState = level.getBlockState(cfaPos);
            if (cfaState.is(ModBlocks.CELESTIAL_FORGING_ANVIL)) {
                Cube323PartHalf half = cfaState.getValue(CelestialForgingAnvilBlock.HALF);
                if (isBottomHalf(half)) {
                    cfaDirs.add(dir);
                }
            }
        }

        if (!cfaDirs.isEmpty()) {
            // 方向固定：背对锻星砧
            Direction facing;
            if (cfaDirs.contains(player.getDirection())) {
                facing = player.getDirection().getOpposite();
            } else {
                facing = cfaDirs.getFirst().getOpposite();
            }
            return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(POWERED, false);
        }

        // 不在锻星砧旁边 → 禁止放置并提示
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(
                Component.translatable("screen.anvilcraft.tooltip.cfa_interface")
                    .withStyle(ChatFormatting.RED),
                true
            );
        }
        return null;
    }

    private static boolean isBottomHalf(Cube323PartHalf half) {
        return half == Cube323PartHalf.BOTTOM_E
            || half == Cube323PartHalf.BOTTOM_W
            || half == Cube323PartHalf.BOTTOM_N
            || half == Cube323PartHalf.BOTTOM_S
            || half == Cube323PartHalf.BOTTOM_NE
            || half == Cube323PartHalf.BOTTOM_SE
            || half == Cube323PartHalf.BOTTOM_NW
            || half == Cube323PartHalf.BOTTOM_SW;
    }

    // ========== GUI ==========

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult
    ) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ReforgingPanelBlockEntity panel
            && player instanceof ServerPlayer sp) {
            sp.openMenu(panel, pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ReforgingPanelBlockEntity panel) {
                panel.markRedstoneDirty();
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // 面板被破坏/锤拆时释放对锻星砧的锁定（已有巨构则保留），避免锁定残留
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ReforgingPanelBlockEntity panel) {
                panel.releaseCfaLock();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // ========== 红石输出 ==========

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        // 注意：getSignal 的 direction 参数方向约定容易搞反（见交接文档会话 2「红石逻辑迭代」）。
        // 此处向锻星砧方向（FACING 侧）及上下两面输出强度 3 的弱充能信号，
        // 此写法经实际测试验证正确，勿随意改动方向。
        if (!state.getValue(EMITTING)) return 0;
        return (direction == Direction.DOWN || direction == Direction.UP || direction == state.getValue(FACING)) ? 3 : 0;
    }

    // ========== BlockEntity ==========

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReforgingPanelBlockEntity(ModBlockEntities.REFORGING_PANEL.get(), pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.REFORGING_PANEL.get(),
            (lvl, pos, st, be) -> be.tick());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <E extends BlockEntity, T extends BlockEntity> BlockEntityTicker<T> createTickerHelper(
        BlockEntityType<T> type, BlockEntityType<E> target, BlockEntityTicker<? super E> ticker
    ) {
        return type == target ? (BlockEntityTicker<T>) ticker : null;
    }
}
