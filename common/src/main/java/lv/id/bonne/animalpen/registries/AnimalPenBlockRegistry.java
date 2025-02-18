//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import java.util.function.Supplier;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.AnimalPenBlock;
import lv.id.bonne.animalpen.blocks.AquariumBlock;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;


public class AnimalPenBlockRegistry
{
    public static void register()
    {
        REGISTRY.register();
    }

    private static <T extends Block> RegistrySupplier<T> registerBlock(String name, Supplier<T> block)
    {
        RegistrySupplier<T> toReturn = REGISTRY.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistrySupplier<Item> registerBlockItem(String name, RegistrySupplier<T> block)
    {
        return AnimalPensItemRegistry.REGISTRY.register(name, () ->
            new BlockItem(block.get(), new Item.Properties().tab(AnimalPensCreativeTabRegistry.ANIMAL_PEN_TAB)));
    }

    /**
     * The main block registry.
     */
    public static final DeferredRegister<Block> REGISTRY =
        DeferredRegister.create(AnimalPen.MOD_ID, Registry.BLOCK_REGISTRY);


    public static final RegistrySupplier<Block> ANIMAL_PEN = registerBlock("animal_pen_block",
        () -> new AnimalPenBlock(
            BlockBehaviour.Properties.copy(Blocks.OAK_WOOD).
                strength(1.0f).
                sound(SoundType.WOOD).
                noOcclusion())
    );

    public static final RegistrySupplier<Block> AQUARIUM = registerBlock("aquarium_block",
        () -> new AquariumBlock(
            BlockBehaviour.Properties.copy(Blocks.GLASS).
                strength(1.0f).
                sound(SoundType.GLASS).
                noOcclusion())
    );
}
