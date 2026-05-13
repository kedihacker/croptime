package com.croptime;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.CropBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "croptime";
    public static final Logger LOGGER = LogUtils.getLogger();
    // The map key is the chunk pos encoded as a long. TreeMap key is tick count; value stores encoded positions + ages.
    private static final Map<Long, TreeMap<Long, EncodedLocations>> blockLocationsByChunk = new HashMap<>();

    public ExampleMod(FMLJavaModLoadingContext context) {
        context.getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SuppressWarnings("unused")
    public void addBlockLocation(long chunkKey, long key, BlockPos blockPos) {
        addBlockLocation(chunkKey, key, blockPos, 0);
    }

    @SuppressWarnings("unused")
    public void addBlockLocation(long chunkKey, long key, BlockPos blockPos, int cropAge) {
        blockLocationsByChunk.computeIfAbsent(chunkKey, ignored -> new TreeMap<>())
                .put(key, EncodedLocations.single(blockPos.asLong(), cropAge));
    }

    @SubscribeEvent
    public void onCropGrow(BlockEvent.CropGrowEvent.Pre event) {
        LOGGER.info("Crop Grow Event");
        if (event.getState().getBlock().getClass() == CropBlock.class) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void chunksave(ChunkDataEvent.Save event) {
        CompoundTag chunkData = event.getData();
        ListTag locationsTag = new ListTag();
        long chunkKey = event.getChunk().getPos().toLong();
        TreeMap<Long, EncodedLocations> blockLocations = blockLocationsByChunk.get(chunkKey);

        if (blockLocations == null || blockLocations.isEmpty()) {
            chunkData.remove("croptime_block_locations");
            return;
        }

        for (Map.Entry<Long, EncodedLocations> entry : blockLocations.entrySet()) {
            CompoundTag locationTag = new CompoundTag();
            locationTag.putLong("key", entry.getKey());
            locationTag.putLongArray("positions", entry.getValue().positions);
            locationTag.putByteArray("ages", entry.getValue().ages);
            locationsTag.add(locationTag);
        }

        chunkData.put("croptime_block_locations", locationsTag);
    }

    @SubscribeEvent
    public void chunkload(ChunkDataEvent.Load event) {
        long chunkKey = event.getChunk().getPos().toLong();
        CompoundTag data = event.getData();
        if (!data.contains("croptime_block_locations", Tag.TAG_LIST)) {
            return;
        }
        ListTag locationsTag = data.getList("croptime_block_locations", Tag.TAG_COMPOUND);
        TreeMap<Long, EncodedLocations> blockLocations = new TreeMap<>();

        for (int i = 0; i < locationsTag.size(); i++) {
            CompoundTag locationTag = locationsTag.getCompound(i);
            long key = locationTag.getLong("key");
            long[] positions = locationTag.getLongArray("positions");
            byte[] ages = locationTag.getByteArray("ages");
            blockLocations.put(key, EncodedLocations.of(positions, ages));
        }

        if (blockLocations.isEmpty()) {
            blockLocationsByChunk.remove(chunkKey);
        } else {
            blockLocationsByChunk.put(chunkKey, blockLocations);
        }
    }

    @SuppressWarnings("unused")
    public Map<Long, Byte> getLastAgesByLocation(long chunkKey) {
        TreeMap<Long, EncodedLocations> blockLocations = blockLocationsByChunk.get(chunkKey);
        if (blockLocations == null || blockLocations.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        Map.Entry<Long, EncodedLocations> first = blockLocations.firstEntry();
        Map.Entry<Long, EncodedLocations> last = blockLocations.lastEntry();
        if (first == null || last == null) {
            return java.util.Collections.emptyMap();
        }
        return getLastAgesByLocation(chunkKey, first.getKey(), true, last.getKey(), true);
    }

    @SuppressWarnings("unused")
    public Map<Long, Byte> getLastAgesByLocation(long chunkKey, long fromKey, boolean fromInclusive, long toKey, boolean toInclusive) {
        TreeMap<Long, EncodedLocations> blockLocations = blockLocationsByChunk.get(chunkKey);
        if (blockLocations == null || blockLocations.isEmpty()) {
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
