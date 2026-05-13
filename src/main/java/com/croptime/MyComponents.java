package com.croptime;

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.resources.ResourceLocation;

public final class MyComponents implements ChunkComponentInitializer {
    public static final ComponentKey<IwheatData> WHEAT = ComponentRegistry.getOrCreate(
            new ResourceLocation(CropTime.MOD_ID, "wheat"),
            IwheatData.class
    );

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registry.register(WHEAT, Impofwheatdata::new);
    }
}
