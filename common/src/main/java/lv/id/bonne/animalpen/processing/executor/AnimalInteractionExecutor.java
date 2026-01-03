package lv.id.bonne.animalpen.processing.executor;


import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;


/**
 * This interface allows easier managing of interactions
 */
public interface AnimalInteractionExecutor
{
    int countAvailable(ItemStack item);


    void consume(ItemStack item, int amount);


    void give(ItemStack item);


    void drop(ItemStack item);


    void damageItem(ItemStack item, int amount);


    ItemStack giveFirst(ItemStack consumedItem, ItemStack lootItem);


    boolean triggerFunctions(AnimalInteraction interaction,
        ItemStack consumedItem,
        int consumedAmount,
        Mob animal,
        CompoundTag mobNBT,
        BlockPos blockPos);


    void triggerItemUse(Mob animal, ItemStack itemInHand, int consumedAmount);
}