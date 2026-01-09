package lv.id.bonne.animalpen.data.provider.neoforge;


import org.jetbrains.annotations.NotNull;
import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;


public class NeoForgeModBlockTagCopyingItemTagProvider extends BlockTagCopyingItemTagProvider
{
    public NeoForgeModBlockTagCopyingItemTagProvider(PackOutput output,
        CompletableFuture<HolderLookup.Provider> lookupProvider,
        CompletableFuture<TagLookup<Block>> blockTags,
        String modID)
    {
        super(output, lookupProvider, blockTags, modID);
    }


    @Override
    protected void addTags(@NotNull HolderLookup.Provider lookupProvider)
    {
        this.copy(AnimalPenTags.ANIMAL_PEN_BLOCKS, AnimalPenTags.ANIMAL_PEN_ITEMS);
    }
}