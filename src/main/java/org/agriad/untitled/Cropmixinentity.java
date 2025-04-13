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

record advancedmathrecord(double virtualfirsttick, double[] tickprogession) {
}

public class Cropmixinentity extends BlockEntity {

    private long first_tick = 0;
    private int[] prog = {};

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
    private static advancedmathrecord advancedmath(float[] randompercent, float chanchepernottick, int totallevels, int currentlevel, double currenttick) {
        if (randompercent.length != totallevels) {
            throw new IllegalArgumentException("randompercent array length must be equal to totallevels");
        }
        if (currentlevel > totallevels) {
            throw new IllegalArgumentException("currentlevel must be less than or equal to totallevels");
        }
        double[] interlist = listmath(randompercent, chanchepernottick, totallevels);
        int before = 0;
        for (int i = currentlevel - 1; i >= 0; i--) {
            before += interlist[i];
        }
        double virtualtick = currenttick - before;
        int acc = 0;
        for (int i = 0; i < totallevels; i++) {
            double intermediate = interlist[i];
            interlist[i] = virtualtick + acc;
            acc += intermediate;
        }
        return new advancedmathrecord(virtualtick, interlist);
    }

    private static int domath(float randompercent, float chanchepernottick, int levels) {
        float reversed = 1 - randompercent;
        return (int) Math.floor(Math.log(reversed) / Math.log(chanchepernottick));
    }

    private static double[] listmath(float[] randompercent, float chanchepernottick, int levels) {
        double[] v = new double[levels];
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


        long perstagetickamount = 10L;
        if (age < maxage) {
            if (prog == null || prog.length == 0) {
                float[] randomlist = new float[maxage];
                for (int i = 0; i < maxage; i++) {
                    randomlist[i] = (float) Math.random();
                }
                advancedmathrecord record = advancedmath(randomlist, chanchepernottick, maxage, world.getBlockState(pos).get(agetype), world.getTime());

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
        nbt.putIntArray("prog", prog);

        super.writeNbt(nbt, registryLookup);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Optional<Long> first_tick_option = nbt.getLong("first_tick");
        Optional<int[]> prog_option = nbt.getIntArray("prog");
        first_tick = first_tick_option.orElse(Long.MIN_VALUE);
        prog = prog_option.orElse(new int[]{});
    }


}

