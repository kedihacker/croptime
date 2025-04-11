package org.agriad.untitled;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class Cropmixinentity extends BlockEntity {

    private long first_tick = 0;

    public Cropmixinentity(BlockPos pos, BlockState state) {
        super(CropBlockEntityTypes.DEMO_BLOCK, pos, state);
        System.out.println(CropBlockEntityTypes.DEMO_BLOCK.toString());
    }

    public static <T extends BlockEntity> BlockEntityTicker<T> validtick(World world, BlockState state, BlockEntityType<T> type) {
        if (world instanceof ServerWorld) {
            return (worldx, pos, statex, blockEntity) -> {
                if (blockEntity instanceof Cropmixinentity) {
                    ((Cropmixinentity) blockEntity).tick(worldx, pos, statex, (Cropmixinentity) blockEntity);
                }
            };
        }

        return null;
    }

    public void tick(World world, BlockPos pos, BlockState state, Cropmixinentity blockEntity) {
        int age = world.getBlockState(pos).get(CropBlock.AGE);
        long perstagetickamount = 200L;
        if (age < 7) {

            if (first_tick == Long.MIN_VALUE) {
                first_tick = CropBlock.MAX_AGE - age * perstagetickamount;
            }
            long elapsed_time = world.getTime() - first_tick;
            long current_stage = elapsed_time / perstagetickamount;
            if (current_stage > age) {
                world.setBlockState(pos, state.with(CropBlock.AGE, (int) current_stage));

            }

        } else {


        }

    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putLong("first_tick", first_tick);

        super.writeNbt(nbt, registryLookup);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Optional<Long> first_tick_option = nbt.getLong("first_tick");
        first_tick = first_tick_option.orElse(Long.MIN_VALUE);
    }

}

