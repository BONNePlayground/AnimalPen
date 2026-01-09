package lv.id.bonne.animalpen.data.provider.neoforge;


import org.jetbrains.annotations.NotNull;
import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.helper.SimpleTagAppender;
import lv.id.bonne.animalpen.data.provider.ModBlockTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;


public class NeoForgeModBlockTagProvider extends BlockTagsProvider implements ModBlockTagsProvider
{
    public NeoForgeModBlockTagProvider(PackOutput output,
        CompletableFuture<HolderLookup.Provider> lookupProvider,
        String modId)
    {
        super(output, lookupProvider, modId);
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