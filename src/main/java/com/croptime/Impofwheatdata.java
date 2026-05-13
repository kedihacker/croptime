package com.croptime;

import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class Impofwheatdata implements IwheatData , ServerTickingComponent {
    // Minimal implementation: only store and retrieve the TreeMap schedule.
    private TreeMap<Long, List<BlockPos>> tree = new TreeMap<>();
    private ChunkAccess chunk;
    public Logger logger;
 // parameter provided by the component factory, not needed here
    public Impofwheatdata(ChunkAccess chunk) {
        this.chunk = chunk;
        this.logger = LoggerFactory.getLogger(CropTime.MOD_ID);
    }

    @Override
    public TreeMap<Long, List<BlockPos>> gettree() {
        return tree;
    }

    @Override
    public void settree(TreeMap<Long, List<BlockPos>> tree) {
        this.tree = tree != null ? tree : new TreeMap<>();
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        tree.clear();
        CompoundTag scheduleTag = tag.getCompound("schedule");

        for (String key : scheduleTag.getAllKeys()) {
            long tick = Long.parseLong(key);
            net.minecraft.nbt.ListTag posListTag = scheduleTag.getList(key, net.minecraft.nbt.Tag.TAG_COMPOUND);
            List<BlockPos> positions = new java.util.ArrayList<>();

            for (int i = 0; i < posListTag.size(); i++) {
                CompoundTag posTag = posListTag.getCompound(i);
                BlockPos pos = BlockPos.of(posTag.getLong("pos"));
                positions.add(pos);
            }

            tree.put(tick, positions);
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        CompoundTag scheduleTag = new CompoundTag();

        for (Map.Entry<Long, List<BlockPos>> entry : tree.entrySet()) {
            net.minecraft.nbt.ListTag posListTag = new net.minecraft.nbt.ListTag();

            for (BlockPos pos : entry.getValue()) {
                CompoundTag posTag = new CompoundTag();
                posTag.putLong("pos", pos.asLong());
                posListTag.add(posTag);
            }

            scheduleTag.put(String.valueOf(entry.getKey()), posListTag);
        }

        tag.put("schedule", scheduleTag);
    }

    @Override
    public void serverTick() {

    }


}
