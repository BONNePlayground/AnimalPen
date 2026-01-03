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
import lv.id.bonne.animalpen.blocks.entities.AviaryTileEntity;
import net.minecraft.core.Registry;
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
        DeferredRegister.create(AnimalPen.MOD_ID, Registry.BLOCK_ENTITY_TYPE_REGISTRY);

    public static final RegistrySupplier<BlockEntityType<AnimalPenTileEntity>> ANIMAL_PEN_TILE_ENTITY =
        REGISTRY.register("animal_pen_tile_entity",
            () -> BlockEntityType.Builder.of(AnimalPenTileEntity::new,
                    AnimalPenBlockRegistry.ANIMAL_PENS.values().stream().map(Supplier::get).toArray(Block[]::new)).
                build(null));

    public static final RegistrySupplier<BlockEntityType<AquariumTileEntity>> AQUARIUM_TILE_ENTITY =
        REGISTRY.register("aquarium_tile_entity",
            () -> BlockEntityType.Builder.of(AquariumTileEntity::new,
                    AnimalPenBlockRegistry.AQUARIUM.get()).
                build(null));

    public static final RegistrySupplier<BlockEntityType<AviaryTileEntity>> AVIARY_TILE_ENTITY =
        REGISTRY.register("aviary_tile_entity",
            () -> BlockEntityType.Builder.of(AviaryTileEntity::new,
                    AnimalPenBlockRegistry.AVIARY.get()).
                build(null));
}
