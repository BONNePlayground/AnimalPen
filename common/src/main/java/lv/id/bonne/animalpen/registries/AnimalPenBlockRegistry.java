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
import lv.id.bonne.animalpen.blocks.AviaryBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;


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
            new BlockItem(block.get(),
                new Item.Properties().arch$tab(AnimalPensCreativeTabRegistry.ANIMAL_PEN_TAB).
                    setId(ResourceKey.create(Registries.ITEM,
                        AnimalPen.resourceOf(name)))));
    }


    /**
     * This method registers animal pen with specified wood type
     *
     * @param woodType that is registered.
     */
    public static void registerPen(WoodType woodType,
        MapColor mapColor,
        FeatureFlag... flags)
    {
        String woodName;

        if (woodType.name().contains(":"))
        {
            // replace ':' with '_'. Tinkers construct adds wood type as `<modid>:<name>`
            woodName = woodType.name().toLowerCase().replaceAll(":", "_");
        }
        else
        {
            woodName = woodType.name().toLowerCase();
        }

        // Register the block
        RegistrySupplier<Block> block = registerBlock("animal_pen_" + woodName,
            () -> new AnimalPenBlock(
                BlockBehaviour.Properties.of().
                    mapColor(mapColor).
                    strength(1.0f).
                    sound(woodType.soundType()).
                    noOcclusion().
                    requiredFeatures(flags).
                    setId(ResourceKey.create(Registries.BLOCK,
                        AnimalPen.resourceOf("animal_pen_" + woodName)))
            ));

        ANIMAL_PENS.put(woodType, block);
    }


    /**
     * The main block registry.
     */
    public static final DeferredRegister<Block> REGISTRY =
        DeferredRegister.create(AnimalPen.MOD_ID, Registries.BLOCK);

    public static final Map<WoodType, RegistrySupplier<Block>> ANIMAL_PENS = new HashMap<>();

    public static final RegistrySupplier<Block> AQUARIUM = registerBlock("aquarium_block",
        () -> new AquariumBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.LOOM).
                strength(1.0f).
                sound(SoundType.GLASS).
                noOcclusion().
                setId(ResourceKey.create(Registries.BLOCK,
                    AnimalPen.resourceOf("aquarium_block")))
        )
    );

    public static final RegistrySupplier<Block> AVIARY = registerBlock("aviary",
        () -> new AviaryBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.LOOM).
                strength(1.0f).
                sound(SoundType.GLASS).
                noOcclusion().
                setId(ResourceKey.create(Registries.BLOCK,
                    AnimalPen.resourceOf("aviary")))
        )
    );

    static
    {
        registerPen(WoodType.OAK, MapColor.WOOD);
        registerPen(WoodType.SPRUCE,  MapColor.PODZOL);
        registerPen(WoodType.BIRCH, MapColor.SAND);
        registerPen(WoodType.ACACIA, MapColor.COLOR_GRAY);
        registerPen(WoodType.JUNGLE, MapColor.DIRT);
        registerPen(WoodType.DARK_OAK, MapColor.COLOR_BROWN);
        registerPen(WoodType.CRIMSON, MapColor.CRIMSON_STEM);
        registerPen(WoodType.WARPED, MapColor.WARPED_STEM);
        registerPen(WoodType.MANGROVE, MapColor.COLOR_RED);
        registerPen(WoodType.BAMBOO, MapColor.COLOR_YELLOW);
        registerPen(WoodType.CHERRY, MapColor.TERRACOTTA_GRAY);
        registerPen(WoodType.PALE_OAK, MapColor.STONE, FeatureFlags.WINTER_DROP);
    }
}
