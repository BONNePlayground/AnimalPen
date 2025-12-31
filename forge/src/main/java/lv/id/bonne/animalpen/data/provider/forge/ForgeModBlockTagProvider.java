package lv.id.bonne.animalpen.data.provider.forge;


import org.jetbrains.annotations.Nullable;

import lv.id.bonne.animalpen.data.helper.SimpleTagAppender;
import lv.id.bonne.animalpen.data.provider.ModBlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;


public class ForgeModBlockTagProvider extends BlockTagsProvider implements ModBlockTagsProvider
{
    public ForgeModBlockTagProvider(DataGenerator arg,
        String modId,
        @Nullable ExistingFileHelper existingFileHelper)
    {
        super(arg, modId, existingFileHelper);
    }


    @Override
    protected void addTags()
    {
        this.addModTags();
    }


    @Override
    public SimpleTagAppender<Block> modTag(TagKey<Block> tag)
    {
        var builder = this.tag(tag);

        return new SimpleTagAppender<>() {

            @Override
            public SimpleTagAppender<Block> add(Block value) {
                builder.add(value);
                return this;
            }
        };
    }
}