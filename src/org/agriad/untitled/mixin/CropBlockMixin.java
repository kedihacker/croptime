package org.agriad.untitled.mixin;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.agriad.untitled.CropBlockEntityTypes;
import org.agriad.untitled.Cropmixinentity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin implements BlockEntityProvider {
    @Shadow
    protected static float getAvailableMoisture(net.minecraft.block.Block block, net.minecraft.world.BlockView world, BlockPos pos) {
        return 0;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new Cropmixinentity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return Cropmixinentity.validtick(world, state, type);
    }

    @Shadow
    protected abstract int getAge(BlockState state);

    @Shadow
    public abstract int getMaxAge();

    @Shadow
    protected abstract BlockState withAge(int age);

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    public void on$RandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        ci.cancel(); // cancel original method
    }

    @Inject(method = "grow", at = @At("HEAD"), cancellable = true)
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state, CallbackInfo ci) {
        world.getBlockEntity(
                pos, CropBlockEntityTypes.DEMO_BLOCK).ifPresent(blockEntity -> {
            blockEntity.grow(world, random, pos,state);
        });
        ci.cancel();
    }

}

