package com.croptime;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.Map;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "croptime";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ExampleMod(FMLJavaModLoadingContext context) {
        context.getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
    }
    @SubscribeEvent
    public void onCropGrow(BlockEvent.CropGrowEvent.Pre event) {

        if (event.getState().getBlock().getClass() == CropBlock.class) {
            LOGGER.info("Crop Grow Event");
        }
    }

    @SuppressWarnings("unused")
    public Map<Long, Byte> getLastAgesByLocation(LevelChunk chunk) {
        CroptimeChunkCapability.ChunkCropTimeData data = CroptimeChunkCapability.get(chunk);
        if (data == null) {
            return java.util.Collections.emptyMap();
        }
        return data.getLastAgesByLocation();
    }

    @SuppressWarnings("unused")
    public Map<Long, Byte> getLastAgesByLocation(LevelChunk chunk, long fromKey, boolean fromInclusive, long toKey, boolean toInclusive) {
        CroptimeChunkCapability.ChunkCropTimeData data = CroptimeChunkCapability.get(chunk);
        if (data == null) {
            return java.util.Collections.emptyMap();
        }
        return data.getLastAgesByLocation(fromKey, fromInclusive, toKey, toInclusive);
    }

    /**
     * Add a single position+age to the stored map for a given chunk and tick key.
     * If the tick entry doesn't exist it will be created.
     */
    @SuppressWarnings("unused")
    public void addBlockLocation(LevelChunk chunk, long tickKey, long pos, int age) {
        CroptimeChunkCapability.ChunkCropTimeData data = CroptimeChunkCapability.get(chunk);
        if (data != null) {
            data.addBlockLocation(tickKey, pos, age);
        }
    }

    /**
     * Remove a single position from the stored map for a given chunk and tick key.
     * Returns true if something was removed, false if the position wasn't found.
     */
    @SuppressWarnings("unused")
    public boolean removeBlockLocation(LevelChunk chunk, long tickKey, long pos) {
        CroptimeChunkCapability.ChunkCropTimeData data = CroptimeChunkCapability.get(chunk);
        return data != null && data.removeBlockLocation(tickKey, pos);
    }

    /** Remove all stored locations for a chunk. */
    @SuppressWarnings("unused")
    public void removeChunk(LevelChunk chunk) {
        CroptimeChunkCapability.ChunkCropTimeData data = CroptimeChunkCapability.get(chunk);
        if (data != null) {
            data.clear();
        }
    }
}
