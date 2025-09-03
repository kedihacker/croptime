package org.agriad.untitled;


import net.fabricmc.api.ModInitializer;

public class UntitledServer implements ModInitializer {
    @Override
    public void onInitialize() {
        CropBlockEntityTypes.initialize();


    }
}

