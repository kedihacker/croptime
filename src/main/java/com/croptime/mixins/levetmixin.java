package com.croptime.mixins;

import com.croptime.ExampleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class levetmixin {
    @Shadow
    @Final
    public boolean isClientSide;

    @Inject(method ="setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",at = @At("RETURN"))
    public void setBlock(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<Boolean> cir) {
            if (state.getBlock().getClass() == net.minecraft.world.level.block.CropBlock.class && !this.isClientSide) {
                ExampleMod.LOGGER.info("set block");
            }
    }
}
