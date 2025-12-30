//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import java.util.HashMap;
import java.util.Map;
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
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;


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
     * This method registers animal pen with specified wood type
     *
     * @param woodType that is registered.
     */
    public static void registerPen(WoodType woodType,
        Material material,
        MaterialColor materialColor,
        SoundType soundType)
    {
        String woodName = woodType.name().toLowerCase();

        if (woodType.name().contains(":"))
        {
            // replace ':' with '_'. Tinkers construct adds wood type as `<modid>:<name>`
            woodName = woodName.replaceAll(":", "_");
        }

        // Register the block
        RegistrySupplier<Block> block = registerBlock("animal_pen_" + woodName,
            () -> new AnimalPenBlock(
                BlockBehaviour.Properties.of(material, materialColor).
                    strength(1.0f).
                    sound(soundType).
                    noOcclusion()));

        ANIMAL_PENS.put(woodType, block);
    }


    /**
     * The main block registry.
     */
    public static final DeferredRegister<Block> REGISTRY =
        DeferredRegister.create(AnimalPen.MOD_ID, Registry.BLOCK_REGISTRY);

    public static final Map<WoodType, RegistrySupplier<Block>> ANIMAL_PENS = new HashMap<>();

    public static final RegistrySupplier<Block> AQUARIUM = registerBlock("aquarium_block",
        () -> new AquariumBlock(
            BlockBehaviour.Properties.copy(Blocks.GLASS).
                strength(1.0f).
                sound(SoundType.GLASS).
                noOcclusion())
    );

    static
    {
        registerPen(WoodType.OAK, Material.WOOD, MaterialColor.WOOD, SoundType.WOOD);
        registerPen(WoodType.SPRUCE, Material.WOOD, MaterialColor.PODZOL, SoundType.WOOD);
        registerPen(WoodType.BIRCH, Material.WOOD, MaterialColor.SAND, SoundType.WOOD);
        registerPen(WoodType.ACACIA, Material.WOOD, MaterialColor.COLOR_GRAY, SoundType.WOOD);
        registerPen(WoodType.JUNGLE, Material.WOOD, MaterialColor.DIRT, SoundType.WOOD);
        registerPen(WoodType.DARK_OAK, Material.WOOD, MaterialColor.COLOR_BROWN, SoundType.WOOD);
        registerPen(WoodType.CRIMSON, Material.NETHER_WOOD, MaterialColor.CRIMSON_STEM, SoundType.STEM);
        registerPen(WoodType.WARPED, Material.NETHER_WOOD, MaterialColor.WARPED_STEM, SoundType.STEM);
    }
}
