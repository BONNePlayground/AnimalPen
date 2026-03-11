package lv.id.bonne.animalpen.integration.jei.recipe;


import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;


public record ItemInfoRecipe(ItemStack item, Component description, ItemStack spawnEgg)
{
}