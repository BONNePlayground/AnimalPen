package lv.id.bonne.animalpen.data.provider.fabric;


import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.provider.ModBlockLootProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;


public class FabricBlockLootProvider extends FabricBlockLootTableProvider implements ModBlockLootProvider
{
    protected FabricBlockLootProvider(FabricDataOutput dataOutput,
        CompletableFuture<HolderLookup.Provider> registriesFuture)
    {
        super(dataOutput, registriesFuture);
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
}