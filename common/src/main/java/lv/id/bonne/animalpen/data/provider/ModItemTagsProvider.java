//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.data.provider;


import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;


public class ModItemTagsProvider extends ItemTagsProvider
{
    public ModItemTagsProvider(DataGenerator generator, BlockTagsProvider blockTagsProvider)
    {
        super(generator, blockTagsProvider);
    }


    @Override
    protected void addTags()
    {
        this.tag(AnimalPenTags.ANIMAL_PEN_ATTACK_TOOLS).
            add(Items.WOODEN_SWORD).
            add(Items.STONE_SWORD).
            add(Items.IRON_SWORD).
            add(Items.DIAMOND_SWORD).
            add(Items.NETHERITE_SWORD).
            add(Items.WOODEN_AXE).
            add(Items.STONE_AXE).
            add(Items.IRON_AXE).
            add(Items.DIAMOND_AXE).
            add(Items.NETHERITE_AXE).
            addOptionalTag(ResourceLocation.tryParse("forge:knives")).
            addOptionalTag(ResourceLocation.tryParse("forge:tools/knives")).
            addOptionalTag(ResourceLocation.tryParse("c:axes")).
            addOptionalTag(ResourceLocation.tryParse("c:swords")).
            addOptionalTag(ResourceLocation.tryParse("c:knives"));

        this.tag(AnimalPenTags.AQUARIUM_ATTACK_TOOLS).
            add(Items.WOODEN_SWORD).
            add(Items.STONE_SWORD).
            add(Items.IRON_SWORD).
            add(Items.DIAMOND_SWORD).
            add(Items.NETHERITE_SWORD).
            add(Items.WOODEN_AXE).
            add(Items.STONE_AXE).
            add(Items.IRON_AXE).
            add(Items.DIAMOND_AXE).
            add(Items.NETHERITE_AXE).
            addOptionalTag(ResourceLocation.tryParse("forge:knives")).
            addOptionalTag(ResourceLocation.tryParse("forge:tools/knives")).
            addOptionalTag(ResourceLocation.tryParse("c:axes")).
            addOptionalTag(ResourceLocation.tryParse("c:swords")).
            addOptionalTag(ResourceLocation.tryParse("c:knives"));

        this.copy(AnimalPenTags.ANIMAL_PEN_BLOCKS, AnimalPenTags.ANIMAL_PEN_ITEMS);
    }


    @Override
    public String getName()
    {
        return "Animal Pen Item Tags";
    }
}
