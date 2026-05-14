package com.croptime;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings({"deprecation", "removal"})
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CroptimeChunkCapability {
    public static final ResourceLocation ID = new ResourceLocation(ExampleMod.MODID, "chunk_data");
    public static final Capability<ChunkCropTimeData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private CroptimeChunkCapability() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ChunkCropTimeData.class);
    }

    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void register(net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent event) {
            CroptimeChunkCapability.register(event);
        }
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<LevelChunk> event) {
        LevelChunk chunk = event.getObject();
        event.addCapability(ID, new Provider(() -> chunk.setUnsaved(true)));
    }

    public static ChunkCropTimeData get(LevelChunk chunk) {
        return chunk.getCapability(CAPABILITY).orElseThrow(() -> new IllegalStateException("Missing croptime chunk capability"));
    }

    public static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final ChunkCropTimeData data;
        private final LazyOptional<ChunkCropTimeData> optional;

        private Provider(Runnable setDirty) {
            this.data = new ChunkCropTimeData(setDirty);
            this.optional = LazyOptional.of(() -> data);
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, net.minecraft.core.Direction side) {
            return cap == CAPABILITY ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            data.deserializeNBT(nbt);
        }
    }

    public static final class ChunkCropTimeData implements INBTSerializable<CompoundTag> {
        // TreeMap key is tick count; value stores encoded positions + ages.
        private final TreeMap<Long, EncodedLocations> blockLocations = new TreeMap<>();
        private final Runnable setDirty;

        public ChunkCropTimeData() {
            this(() -> {});
        }

        private ChunkCropTimeData(Runnable setDirty) {
            this.setDirty = setDirty;
        }

        public Map<Long, Byte> getLastAgesByLocation() {
            if (blockLocations.isEmpty()) {
                return java.util.Collections.emptyMap();
            }
            Map.Entry<Long, EncodedLocations> first = blockLocations.firstEntry();
            Map.Entry<Long, EncodedLocations> last = blockLocations.lastEntry();
            if (first == null || last == null) {
                return java.util.Collections.emptyMap();
            }
            return getLastAgesByLocation(first.getKey(), true, last.getKey(), true);
        }

        public Map<Long, Byte> getLastAgesByLocation(long fromKey, boolean fromInclusive, long toKey, boolean toInclusive) {
            if (blockLocations.isEmpty()) {
                return java.util.Collections.emptyMap();
            }
            if (fromKey > toKey) {
                return java.util.Collections.emptyMap();
            }
            Map<Long, Byte> lastAgesByLocation = new HashMap<>();
            for (Map.Entry<Long, EncodedLocations> entry : blockLocations.subMap(fromKey, fromInclusive, toKey, toInclusive).entrySet()) {
                long[] positions = entry.getValue().positions;
                byte[] ages = entry.getValue().ages;
                int limit = Math.min(positions.length, ages.length);
                for (int i = 0; i < limit; i++) {
                    lastAgesByLocation.put(positions[i], ages[i]);
                }
            }
            return lastAgesByLocation;
        }

        public void addBlockLocation(long tickKey, long pos, int age) {
            EncodedLocations existing = blockLocations.get(tickKey);
            if (existing == null) {
                blockLocations.put(tickKey, EncodedLocations.single(pos, age));
                setDirty.run();
                return;
            }

            long[] newPositions = Arrays.copyOf(existing.positions, existing.positions.length + 1);
            newPositions[newPositions.length - 1] = pos;
            byte[] newAges = Arrays.copyOf(existing.ages, existing.ages.length + 1);
            newAges[newAges.length - 1] = EncodedLocations.toUnsignedByte(age);
            blockLocations.put(tickKey, new EncodedLocations(newPositions, newAges));
            setDirty.run();
        }

        public boolean removeBlockLocation(long tickKey, long pos) {
            EncodedLocations existing = blockLocations.get(tickKey);
            if (existing == null || existing.positions.length == 0) {
                return false;
            }

            int idx = -1;
            for (int i = 0; i < existing.positions.length; i++) {
                if (existing.positions[i] == pos) {
                    idx = i;
                    break;
                }
            }
            if (idx == -1) {
                return false;
            }

            int newLen = existing.positions.length - 1;
            if (newLen == 0) {
                blockLocations.remove(tickKey);
            } else {
                long[] newPositions = new long[newLen];
                byte[] newAges = new byte[newLen];
                for (int i = 0, j = 0; i < existing.positions.length; i++) {
                    if (i == idx) {
                        continue;
                    }
                    newPositions[j] = existing.positions[i];
                    newAges[j] = existing.ages[i];
                    j++;
                }
                blockLocations.put(tickKey, new EncodedLocations(newPositions, newAges));
            }

            setDirty.run();
            return true;
        }

        public void clear() {
            if (!blockLocations.isEmpty()) {
                blockLocations.clear();
                setDirty.run();
            }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag root = new CompoundTag();
            if (blockLocations.isEmpty()) {
                return root;
            }
            ListTag locationsTag = new ListTag();
            for (Map.Entry<Long, EncodedLocations> entry : blockLocations.entrySet()) {
                CompoundTag locationTag = new CompoundTag();
                locationTag.putLong("key", entry.getKey());
                locationTag.putLongArray("positions", entry.getValue().positions);
                locationTag.putByteArray("ages", entry.getValue().ages);
                locationsTag.add(locationTag);
            }
            root.put("croptime_block_locations", locationsTag);
            return root;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            blockLocations.clear();
            if (!nbt.contains("croptime_block_locations", Tag.TAG_LIST)) {
                return;
            }
            ListTag locationsTag = nbt.getList("croptime_block_locations", Tag.TAG_COMPOUND);
            for (int i = 0; i < locationsTag.size(); i++) {
                CompoundTag locationTag = locationsTag.getCompound(i);
                long key = locationTag.getLong("key");
                long[] positions = locationTag.getLongArray("positions");
                byte[] ages = locationTag.getByteArray("ages");
                blockLocations.put(key, EncodedLocations.of(positions, ages));
            }
        }

        private record EncodedLocations(long[] positions, byte[] ages) {

            private static EncodedLocations single(long pos, int cropAge) {
                return new EncodedLocations(new long[]{pos}, new byte[]{toUnsignedByte(cropAge)});
            }

            private static EncodedLocations of(long[] positions, byte[] ages) {
                if (positions == null) {
                    return new EncodedLocations(new long[0], new byte[0]);
                }
                if (ages == null || ages.length != positions.length) {
                    return new EncodedLocations(positions, new byte[positions.length]);
                }
                return new EncodedLocations(positions, ages);
            }

            private static byte toUnsignedByte(int value) {
                return (byte) (value & 0xFF);
            }

            @SuppressWarnings("unused")
            private static int unsignedByteToInt(byte value) {
                return value & 0xFF;
            }

            @SuppressWarnings("unused")
            private List<BlockPos> asBlockPosList() {
                List<BlockPos> list = new ArrayList<>(positions.length);
                for (long pos : positions) {
                    list.add(BlockPos.of(pos));
                }
                return list;
            }
        }
    }
}
