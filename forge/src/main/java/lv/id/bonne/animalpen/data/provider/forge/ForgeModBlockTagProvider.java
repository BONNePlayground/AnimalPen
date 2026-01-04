package lv.id.bonne.animalpen.data.provider.forge;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.helper.SimpleTagAppender;
import lv.id.bonne.animalpen.data.provider.ModBlockTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;


public class ForgeModBlockTagProvider extends BlockTagsProvider implements ModBlockTagsProvider
{
    public ForgeModBlockTagProvider(PackOutput output,
        CompletableFuture<HolderLookup.Provider> lookupProvider,
        String modId,
        @Nullable ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, modId, existingFileHelper);
    }


    @Override
    protected void addTags(@NotNull HolderLookup.Provider arg)
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