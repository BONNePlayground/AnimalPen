package lv.id.bonne.animalpen.data.provider.fabric;


import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.helper.SimpleItemTagAppender;
import lv.id.bonne.animalpen.data.provider.ModItemTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;


public class FabricModItemTagProvider extends FabricTagProvider.ItemTagProvider implements ModItemTagsProvider
{
    public FabricModItemTagProvider(FabricDataOutput output,
        CompletableFuture<HolderLookup.Provider> completableFuture,
        @Nullable BlockTagProvider blockTagProvider)
    {
        super(output, completableFuture, blockTagProvider);
    }


    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        this.addModTags();
    }


    @Override
    public SimpleItemTagAppender modTag(TagKey<Item> tag)
    {
        TagAppender<Item, Item> builder = this.valueLookupBuilder(tag);

        return new SimpleItemTagAppender() {

            @Override
            public SimpleItemTagAppender add(Item value) {
                builder.add(value);
                return this;
            }

            @Override
            public SimpleItemTagAppender copy(TagKey<Block> blockTag) {
                FabricModItemTagProvider.this.copy(blockTag, tag);
                return this;
            }

            @Override
            public SimpleItemTagAppender optionalTag(TagKey<Item> other) {
                builder.addOptionalTag(other);
                return this;
            }


            @Override
            public SimpleItemTagAppender tag(TagKey<Item> itemTag)
            {
                builder.addTag(itemTag);
                return this;
            }
        };
    }
}