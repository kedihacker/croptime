package org.agriad.untitled;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
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
        Block blocktype = world.getBlockState(pos).getBlock();
        int age;
        IntProperty agetype;
        int maxage;
        switch (blocktype) {
            case BeetrootsBlock ignored:
                age = state.get(BeetrootsBlock.AGE);
                agetype = BeetrootsBlock.AGE;
                maxage = BeetrootsBlock.BEETROOTS_MAX_AGE;
                break;
            case CarrotsBlock ignored:
                age = state.get(CarrotsBlock.AGE);
                agetype = CarrotsBlock.AGE;
                maxage = CarrotsBlock.MAX_AGE;
                break;
            case PotatoesBlock ignored:
                age = state.get(PotatoesBlock.AGE);
                agetype = PotatoesBlock.AGE;
                maxage = PotatoesBlock.MAX_AGE;
                break;
            case CropBlock ignored:
                age = state.get(CropBlock.AGE);
                agetype = CropBlock.AGE;
                maxage = CropBlock.MAX_AGE;

                break;

            default:
                throw new IllegalStateException("Unexpected value: sigma boi " + blocktype);
        }


        long perstagetickamount = 10L;
        if (age < maxage) {
            UntitledServer.CONFIG.growthmodel();
            if (first_tick == Long.MIN_VALUE) { // mod is new and existing crops are not yet recorded
                first_tick = maxage - age * perstagetickamount; // preserve progress of the crop
                world.getBlockEntity(pos).markDirty();
            }
            if (first_tick == 0) {
                first_tick = world.getTime();
                world.getBlockEntity(pos).markDirty();
            }
            long elapsed_time = world.getTime() - first_tick;
            long current_stage = elapsed_time / perstagetickamount;
            if (current_stage > age) {
                world.setBlockState(pos, state.with(agetype, (int) current_stage), CropBlock.NOTIFY_LISTENERS);
            }

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

