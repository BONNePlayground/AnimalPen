package lv.id.bonne.animalpen.data.provider.loottable.forge;


import lv.id.bonne.animalpen.data.provider.ModBlockLootProvider;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.level.block.Block;


public class ForgeModBlockLootProvider extends BlockLoot implements ModBlockLootProvider
{
    @Override
    protected void addTables()
    {
        this.addModLoot();
    }


    @Override
    public void modDropSelf(Block block)
    {
        this.dropSelf(block);
    }


    protected Iterable<Block> getKnownBlocks()
    {
        return this.getMobBlockList();
    }
}