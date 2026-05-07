//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import java.util.function.Supplier;
import java.util.stream.Stream;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenTileEntity;
import lv.id.bonne.animalpen.blocks.entities.AquariumTileEntity;
import lv.id.bonne.animalpen.blocks.entities.AviaryTileEntity;
import lv.id.bonne.animalpen.platform.Services;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;


public class AnimalPenTileEntityRegistry
{
    public static void register()
    {
    }


    public static final Supplier<BlockEntityType<AnimalPenTileEntity>> ANIMAL_PEN_TILE_ENTITY =
        Services.REGISTRY.registerBlockEntityType(AnimalPen.resourceOf("animal_pen_tile_entity"), () ->
            Services.PLATFORM.createBlockEntity(
                AnimalPenTileEntity::new,
                AnimalPenBlockRegistry.ANIMAL_PENS.values()
                    .stream()
                    .map(Supplier::get)
                    .toArray(Block[]::new)
            )
        );

    public static final Supplier<BlockEntityType<AquariumTileEntity>> AQUARIUM_TILE_ENTITY =
        Services.REGISTRY.registerBlockEntityType(AnimalPen.resourceOf("aquarium_tile_entity"), () ->
            Services.PLATFORM.createBlockEntity(
                AquariumTileEntity::new,
                AnimalPenBlockRegistry.AQUARIUM.get()
            )
        );

    public static final Supplier<BlockEntityType<AviaryTileEntity>> AVIARY_TILE_ENTITY =
        Services.REGISTRY.registerBlockEntityType(AnimalPen.resourceOf("aviary_tile_entity"), () ->
            Services.PLATFORM.createBlockEntity(
                AviaryTileEntity::new,
                Stream.concat(
                    Stream.concat(
                        AnimalPenBlockRegistry.COPPER_AVIARIES.values().stream().map(Supplier::get),
                        AnimalPenBlockRegistry.WAXED_COPPER_AVIARIES.values().stream().map(Supplier::get)
                    ),
                    Stream.of(
                        AnimalPenBlockRegistry.AVIARY.get(),
                        AnimalPenBlockRegistry.GOLD_AVIARY.get()
                    )
                ).toArray(Block[]::new)
            )
        );
}
