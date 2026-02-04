package lv.id.bonne.animalpen.integrations.fabric;


import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;


public record ItemInfoRecipe(ItemStack item, Component description, List<ItemStack> spawnEggs)
{
}