package lv.id.bonne.animalpen.data.provider.neoforge;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.helper.SimpleItemTagAppender;
import lv.id.bonne.animalpen.data.provider.ModItemTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;


public class NeoForgeModItemTagProvider extends ItemTagsProvider implements ModItemTagsProvider
{
    public NeoForgeModItemTagProvider(PackOutput arg,
        CompletableFuture<HolderLookup.Provider> completableFuture,
        TagsProvider<Block> arg2,
        String modId,
        @Nullable ExistingFileHelper existingFileHelper)
    {
        super(arg, completableFuture, arg2.contentsGetter(), modId, existingFileHelper);
    }


    @Override
    protected void addTags(@NotNull HolderLookup.Provider arg)
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
                builder.add(value);
                return this;
            }

            @Override
            public SimpleItemTagAppender copy(TagKey<Block> blockTag) {
                NeoForgeModItemTagProvider.this.copy(blockTag, tag);
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