package com.croptime.mixins;

import com.croptime.ExampleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CropBlock.class)
public abstract class cropmixin {
    @Inject(method = "isRandomlyTicking", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    public void isRandomlyTicking(BlockState p_52288_, CallbackInfoReturnable<Boolean> cir) {
        if (p_52288_.getBlock().getClass() == CropBlock.class) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "randomTick", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    public void randomTick(BlockState p_221050_, ServerLevel p_221051_, BlockPos p_221052_, RandomSource p_221053_, CallbackInfo ci) {
        if (p_221050_.getBlock().getClass() == CropBlock.class) {
            ci.cancel();
        }
    }
}
