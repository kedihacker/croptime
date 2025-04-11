package org.agriad.untitled;

import net.fabricmc.api.ModInitializer;
import org.spongepowered.asm.mixin.Mixin;

public class Untitled implements ModInitializer {

    @Override
    public void onInitialize() {
        CropBlockEntityTypes.initialize();
    }
}

