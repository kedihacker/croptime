package org.agriad.untitled;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;

public class Init implements ModInitializer {

    @Override
    public void onInitialize() {
        CropBlockEntityTypes.initialize();

    }
}

