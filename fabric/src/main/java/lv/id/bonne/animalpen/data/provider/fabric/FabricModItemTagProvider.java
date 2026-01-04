package lv.id.bonne.animalpen.data.provider.fabric;


import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.helper.SimpleItemTagAppender;
import lv.id.bonne.animalpen.data.provider.ModItemTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
        var builder = this.tag(tag);

        return new SimpleItemTagAppender() {

            @Override
            public SimpleItemTagAppender add(Item value) {
                BuiltInRegistries.ITEM.getResourceKey(value).
                    ifPresent(builder::add);
                return this;
            }

            @Override
            public SimpleItemTagAppender copy(TagKey<Block> blockTag) {
                FabricModItemTagProvider.this.copy(blockTag, tag);
                return this;
            }

            @Override
            public SimpleItemTagAppender optionalTag(ResourceLocation other) {
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