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
import net.minecraft.util.math.random.Random;
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
            interlist[i] = virtualtick + intermediate;
        }
        return new advancedmathrecord(virtualtick, interlist);
    }

    private static long[] listmath(float[] randompercent, float chanchepernottick, int levels) {
        long[] v = new long[levels];
        int total = 0;
        for (int i = 0; i < levels; i++) {
            int var = ingertare(randompercent[i], chanchepernottick);
            v[i] = var + total;
            total += var;
        }
        return v;
    }

    private static int ingertare(float randompercent, float chanchepernottick) {
        return (int) Math.floor(Math.log(1 - randompercent) / Math.log(chanchepernottick));
    }

    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        System.out.println("grow");
        this.calc(world, pos, state, state.getBlock(), true);
    }

    public void tick(World world, BlockPos pos, BlockState state, Cropmixinentity blockEntity) {
        Block blocktype = world.getBlockState(pos).getBlock();
        int age;

        IntProperty agetype;
        int maxage;
        calc(world, pos, state, blocktype, false);
    }

    private void calc(World world, BlockPos pos, BlockState state, Block blocktype, boolean dogrow) {
        int maxage;
        IntProperty agetype;
        int age;
        float chanchepernottick = (float) 1 - ((float) 3 / (4096 * 4));
        switch (blocktype) {
            case BeetrootsBlock ignored:
                age = state.get(BeetrootsBlock.AGE);

                agetype = BeetrootsBlock.AGE;
                maxage = BeetrootsBlock.BEETROOTS_MAX_AGE;
                if (dogrow) {
                    age = Math.min(age + 1, maxage);
                }
                chanchepernottick = (float) 4095 / 4096;
                break;
            case CarrotsBlock ignored:
                age = state.get(CarrotsBlock.AGE);
                agetype = CarrotsBlock.AGE;
                maxage = CarrotsBlock.MAX_AGE;
                if (dogrow) {
                    age = Math.min(age + world.getRandom().nextBetween(2,5), maxage);
                }
                break;
            case PotatoesBlock ignored:
                age = state.get(PotatoesBlock.AGE);
                agetype = PotatoesBlock.AGE;
                maxage = PotatoesBlock.MAX_AGE;
                if (dogrow) {
                    age = Math.min(age + world.getRandom().nextBetween(2,5), maxage);
                }
                break;
            case CropBlock ignored:
                age = state.get(CropBlock.AGE);
                agetype = CropBlock.AGE;
                maxage = CropBlock.MAX_AGE;
                if (dogrow) {
                    age = Math.min(age + world.getRandom().nextBetween(2,5), maxage);
                }
                break;

            default:
                throw new IllegalStateException("Unexpected value: sigma boi " + blocktype);
        }
        if (dogrow) {
            float[] randomlist = new float[maxage];
            for (int i = 0; i < maxage; i++) {
                randomlist[i] = world.random.nextFloat();
            }
            advancedmathrecord record = advancedmath(randomlist, chanchepernottick, maxage, age, world.getTime());
            prog = record.tickprogession();
            first_tick = record.virtualfirsttick();
            this.markDirty();

        }

        int ageacc = 0;
        if (age < maxage) {
            if (prog.length == 0) {
                float[] randomlist = new float[maxage];
                for (int i = 0; i < maxage; i++) {
                    randomlist[i] = world.random.nextFloat();
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
                System.out.println(pos + " " + ageacc + world.getTime());
                world.setBlockState(pos, state.with(agetype, ageacc), CropBlock.NOTIFY_LISTENERS);
            }
        }
    }

    @Override
    protected void writeData(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putLong("first_tick", first_tick);
        nbt.putLongArray("prog", prog);

        super.writeData(nbt, registryLookup);
    }

    @Override
    public void readData(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readData(nbt, registryLookup);
        Optional<Long> first_tick_option = nbt.getLong("first_tick");
        Optional<long[]> prog_option = nbt.getLongArray("prog");
        first_tick = first_tick_option.orElse(Long.MIN_VALUE);
        prog = prog_option.orElse(new long[]{});
    }


}

