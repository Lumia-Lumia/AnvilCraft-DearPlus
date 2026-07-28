package dev.anvilcraft.addon.dearpluscelestialreforge.mixin;

import dev.anvilcraft.addon.dearpluscelestialreforge.block.entity.ICelestialForgingAnvilAccessor;
import dev.dubhe.anvilcraft.block.entity.CelestialForgingAnvilBlockEntity;
import dev.dubhe.anvilcraft.block.entity.celestial.CelestialBodyData;
import dev.dubhe.anvilcraft.block.entity.celestial.PlanetaryResourceSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(CelestialForgingAnvilBlockEntity.class)
public abstract class CelestialForgingAnvilBlockEntityMixin
    extends BlockEntity
    implements ICelestialForgingAnvilAccessor {

    public CelestialForgingAnvilBlockEntityMixin(
        BlockEntityType<?> type, BlockPos pos, BlockState state
    ) {
        super(type, pos, state);
    }

    @Shadow
    private boolean searching;

    @Shadow
    @Nullable
    private CelestialBodyData celestialBodyData;

    @Shadow
    @Nullable
    private PlanetaryResourceSet planetaryResourceSet;

    @Override
    public boolean cfaIsSearching() {
        return searching;
    }

    @Override
    @Nullable
    public CelestialBodyData cfaGetBodyData() {
        return celestialBodyData;
    }

    @Override
    @Nullable
    public PlanetaryResourceSet cfaGetResourceSet() {
        return planetaryResourceSet;
    }
}
