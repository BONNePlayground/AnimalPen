//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import org.jetbrains.annotations.Nullable;
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
     * @param woodType that is registered.
     * @param woodBlock block which properties are copied.
     */
    public static void registerPen(WoodType woodType, @Nullable Block woodBlock)
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
                BlockBehaviour.Properties.copy(woodBlock != null ? woodBlock : Blocks.OAK_WOOD).
                    strength(1.0f).
                    sound(SoundType.WOOD).
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

    static {
        registerPen(WoodType.OAK, Blocks.OAK_WOOD);
        registerPen(WoodType.SPRUCE, Blocks.SPRUCE_WOOD);
        registerPen(WoodType.BIRCH, Blocks.BIRCH_WOOD);
        registerPen(WoodType.ACACIA, Blocks.ACACIA_WOOD);
        registerPen(WoodType.JUNGLE, Blocks.JUNGLE_WOOD);
        registerPen(WoodType.DARK_OAK, Blocks.DARK_OAK_WOOD);
        registerPen(WoodType.CRIMSON, Blocks.CRIMSON_STEM);
        registerPen(WoodType.WARPED, Blocks.WARPED_STEM);
    }
}
