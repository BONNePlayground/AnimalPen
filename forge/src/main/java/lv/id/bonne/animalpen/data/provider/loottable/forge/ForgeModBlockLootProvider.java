package lv.id.bonne.animalpen.data.provider.loottable.forge;


import org.jetbrains.annotations.NotNull;
import java.util.Set;

import lv.id.bonne.animalpen.data.provider.ModBlockLootProvider;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;


public class ForgeModBlockLootProvider extends BlockLootSubProvider implements ModBlockLootProvider
{
    public ForgeModBlockLootProvider()
    {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }


    @Override
    protected void generate()
    {
        this.addModLoot();
    }


    @Override
    public void modDropSelf(Block block)
    {
        this.dropSelf(block);
    }


    @Override
    @NotNull
    protected Iterable<Block> getKnownBlocks()
    {
        return this.getMobBlockList();
    }
}