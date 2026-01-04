package lv.id.bonne.animalpen.data.provider.fabric;


import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.helper.SimpleTagAppender;
import lv.id.bonne.animalpen.data.provider.ModBlockTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;


public class FabricModBlockTagProvider extends FabricTagProvider.BlockTagProvider implements ModBlockTagsProvider
{
    public FabricModBlockTagProvider(FabricDataOutput output,
        CompletableFuture<HolderLookup.Provider> registriesFuture)
    {
        super(output, registriesFuture);
    }


    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        this.addModTags();
    }


    @Override
    public SimpleTagAppender<Block> modTag(TagKey<Block> tag)
    {
        TagAppender<Block> builder = this.tag(tag);

        return new SimpleTagAppender<>()
        {
            @Override
            public SimpleTagAppender<Block> add(Block value)
            {
                BuiltInRegistries.BLOCK.getResourceKey(value).
                    ifPresent(builder::add);
                return this;
            }
        };
    }
}