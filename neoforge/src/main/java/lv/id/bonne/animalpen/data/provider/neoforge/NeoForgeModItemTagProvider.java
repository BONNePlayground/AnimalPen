package lv.id.bonne.animalpen.data.provider.neoforge;


import org.jetbrains.annotations.NotNull;
import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.helper.SimpleItemTagAppender;
import lv.id.bonne.animalpen.data.provider.ModItemTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ItemTagsProvider;


public class NeoForgeModItemTagProvider extends ItemTagsProvider implements ModItemTagsProvider
{
    public NeoForgeModItemTagProvider(PackOutput arg,
        CompletableFuture<HolderLookup.Provider> completableFuture,
        String modId)
    {
        super(arg, completableFuture, modId);
    }


    @Override
    protected void addTags(@NotNull HolderLookup.Provider arg)
    {
        this.addModTags();
    }


    @Override
    public SimpleItemTagAppender modTag(TagKey<Item> tag)
    {
        TagAppender<Item, Item> builder = this.tag(tag);

        return new SimpleItemTagAppender() {

            @Override
            public SimpleItemTagAppender add(Item value) {
                builder.add(value);
                return this;
            }

            @Override
            public SimpleItemTagAppender copy(TagKey<Block> blockTag) {
                // NeoForge made a separate copy block to item provider
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