package lv.id.bonne.animalpen.data.provider;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import net.minecraft.world.level.block.Block;


public interface ModBlockLootProvider
{
    void modDropSelf(Block block);


    default void addModLoot()
    {
        this.getMobBlockList().forEach(this::modDropSelf);
    }


    default Iterable<Block> getMobBlockList()
    {
        List<Block> blockList = new ArrayList<>(AnimalPenBlockRegistry.ANIMAL_PENS.size() + 2);

        blockList.add(AnimalPenBlockRegistry.AQUARIUM.get());
        blockList.add(AnimalPenBlockRegistry.AVIARY.get());

        for (Supplier<Block> supplier : AnimalPenBlockRegistry.ANIMAL_PENS.values())
        {
            blockList.add(supplier.get());
        }

        return blockList;
    }
}