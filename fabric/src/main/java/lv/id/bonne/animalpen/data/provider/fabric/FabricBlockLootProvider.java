package lv.id.bonne.animalpen.data.provider.fabric;


import java.util.function.BiConsumer;

import lv.id.bonne.animalpen.data.provider.ModBlockLootProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;


public class FabricBlockLootProvider extends FabricBlockLootTableProvider implements ModBlockLootProvider
{
    protected FabricBlockLootProvider(FabricDataOutput dataOutput)
    {
        super(dataOutput);
    }


    @Override
    public void modDropSelf(Block block)
    {
        this.dropSelf(block);
    }


    @Override
    public void generate()
    {
        this.addModLoot();
    }


    @Override
    public void accept(BiConsumer<ResourceLocation, LootTable.Builder> resourceLocationBuilderBiConsumer)
    {

    }
}