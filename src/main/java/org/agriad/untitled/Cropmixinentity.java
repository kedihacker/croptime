package org.agriad.untitled;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

record advancedmathrecord(long virtualfirsttick, long[] tickprogession) {
}

public class Cropmixinentity extends BlockEntity {

    private long first_tick = 0;
    private long[] prog = {};

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

    // return a int and a array of ints
    private static advancedmathrecord advancedmath(float[] randompercent, float chanchepernottick, int totallevels, int currentlevel, long currenttick) {
        if (randompercent.length != totallevels) {
            throw new IllegalArgumentException("randompercent array length must be equal to totallevels");
        }
        if (currentlevel > totallevels) {
            throw new IllegalArgumentException("currentlevel must be less than or equal to totallevels");
        }
        long[] interlist = listmath(randompercent, chanchepernottick, totallevels);
        long before = 0;
        for (int i = currentlevel - 1; i >= 0; i--) {
            before += interlist[i];
        }
        long virtualtick = currenttick - before;

        for (int i = 0; i < totallevels; i++) {
            long intermediate = interlist[i];
            interlist[i] = virtualtick + intermediate ;

        }
        return new advancedmathrecord(virtualtick, interlist);
    }

    private static int domath(float randompercent, float chanchepernottick, int levels) {
        return (int) Math.floor(Math.log(1 - randompercent) / Math.log(chanchepernottick));
    }

    private static long[] listmath(float[] randompercent, float chanchepernottick, int levels) {
        long[] v = new long[levels];
        int total = 0;
        for (int i = 0; i < levels; i++) {
            int var = domath(randompercent[i], chanchepernottick, levels);
            v[i] = var + total;
            total += var;
        }
        return v;
    }

    public void tick(World world, BlockPos pos, BlockState state, Cropmixinentity blockEntity) {
        Block blocktype = world.getBlockState(pos).getBlock();
        int age;

        IntProperty agetype;
        int maxage;
        float chanchepernottick = (float) 1 - ((float) 3 / (4096 * 4));
        switch (blocktype) {
            case BeetrootsBlock ignored:
                age = state.get(BeetrootsBlock.AGE);
                agetype = BeetrootsBlock.AGE;
                maxage = BeetrootsBlock.BEETROOTS_MAX_AGE;
                chanchepernottick = (float) 4095 / 4096;
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


        int ageacc = 0;
        if (age < maxage) {
            if (prog.length == 0) {
                float[] randomlist = new float[maxage];
                for (int i = 0; i < maxage; i++) {
                    randomlist[i] = (float) world.random.nextFloat();
                }
                System.out.println(pos);
                advancedmathrecord record = advancedmath(randomlist, chanchepernottick, maxage, world.getBlockState(pos).get(agetype), world.getTime());
                prog = record.tickprogession();
                first_tick = record.virtualfirsttick();
                this.markDirty();
            }
            long worldtime = world.getTime();
            for (long l : prog) {
                if (l < worldtime) {
                    ageacc++;


                }
            }
            if (age != ageacc) {
                world.setBlockState(pos, state.with(agetype, ageacc), CropBlock.NOTIFY_LISTENERS);
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putLong("first_tick", first_tick);
        nbt.putLongArray("prog", prog);

        super.writeNbt(nbt, registryLookup);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Optional<Long> first_tick_option = nbt.getLong("first_tick");
        Optional<long[]> prog_option = nbt.getLongArray("prog");
        first_tick = first_tick_option.orElse(Long.MIN_VALUE);
        prog = prog_option.orElse(new long[]{});
    }


}

