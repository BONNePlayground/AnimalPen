//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.data.provider;


import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.world.level.block.Block;


public class ModBlockTagsProvider extends BlockTagsProvider
{
    public ModBlockTagsProvider(DataGenerator generator)
    {
        super(generator);
    }


    @Override
    protected void addTags()
    {
        TagAppender<Block> tag = this.tag(AnimalPenTags.ANIMAL_PEN_BLOCKS);

        AnimalPenBlockRegistry.ANIMAL_PENS.values().
            forEach(blockSupplier -> tag.add(blockSupplier.get()));
    }


    @Override
    public String getName()
    {
        return "Animal Pen Block Tags";
    }
}
