package org.agriad.untitled;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class CropBlockEntityTypes {
    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of("agriad", path), blockEntityType);
    }

    public static void initialize() {
    System.out.println("CropBlockEntityTypes initialized");
    }


    public static final BlockEntityType<Cropmixinentity> DEMO_BLOCK = register(
            "crop_block", FabricBlockEntityTypeBuilder.create(Cropmixinentity::new, Blocks.WHEAT).build()
    );


}
