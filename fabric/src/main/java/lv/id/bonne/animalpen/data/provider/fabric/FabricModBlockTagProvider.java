package lv.id.bonne.animalpen.data.provider.fabric;


import lv.id.bonne.animalpen.data.helper.SimpleTagAppender;
import lv.id.bonne.animalpen.data.provider.ModBlockTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;


public class FabricModBlockTagProvider extends FabricTagProvider.BlockTagProvider implements ModBlockTagsProvider
{
    public FabricModBlockTagProvider(FabricDataGenerator dataGenerator)
    {
        super(dataGenerator);
    }


    @Override
    protected void generateTags()
    {
        this.addModTags();
    }


    @Override
    public SimpleTagAppender<Block> modTag(TagKey<Block> tag)
    {
        var builder = this.tag(tag);

        return new SimpleTagAppender<>()
        {
            @Override
            public SimpleTagAppender<Block> add(Block value)
            {
                builder.add(value);
                return this;
            }
        };
    }
}