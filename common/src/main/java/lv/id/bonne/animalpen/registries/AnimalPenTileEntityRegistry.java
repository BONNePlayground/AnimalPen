//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import java.util.function.Supplier;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenTileEntity;
import lv.id.bonne.animalpen.blocks.entities.AquariumTileEntity;
import lv.id.bonne.animalpen.platform.ClientPlatformHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;


public class AnimalPenTileEntityRegistry
{
    public static void register()
    {
        REGISTRY.register();
    }


    /**
     * The main block entity registry.
     */
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY =
        DeferredRegister.create(AnimalPen.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<AnimalPenTileEntity>> ANIMAL_PEN_TILE_ENTITY =
        REGISTRY.register("animal_pen_tile_entity",
            () -> ClientPlatformHelper.create(AnimalPenTileEntity::new,
                    AnimalPenBlockRegistry.ANIMAL_PENS.values().stream().map(Supplier::get).toArray(Block[]::new)));

    public static final RegistrySupplier<BlockEntityType<AquariumTileEntity>> AQUARIUM_TILE_ENTITY =
        REGISTRY.register("aquarium_tile_entity",
            () -> ClientPlatformHelper.create(AquariumTileEntity::new, AnimalPenBlockRegistry.AQUARIUM.get()));
}
