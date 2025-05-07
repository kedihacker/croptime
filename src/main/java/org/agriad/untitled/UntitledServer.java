package org.agriad.untitled;


import net.fabricmc.api.ModInitializer;

public class UntitledServer implements ModInitializer {
    public static final org.agriad.untitled.MyConfig CONFIG = org.agriad.untitled.MyConfig.createAndLoad();

    @Override
    public void onInitialize() {
        CropBlockEntityTypes.initialize();


    }
}

