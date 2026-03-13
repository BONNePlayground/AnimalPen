//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import java.util.List;

import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.util.ItemTransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;


/**
 * This function drops requested loot table items.
 */
public class DropRequestedLoot implements EntityFunction.ProcessEntityFunction
{
    @Override
    public boolean processFunction(ServerLevel serverLevel,
        Mob mob,
        ItemStack componentHolder,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        if (dataValue == null)
        {
            // Do not know loot table.
            return false;
        }

        Identifier lootTableResource = Identifier.tryParse(dataValue.getAsString());

        if (lootTableResource == null)
        {
            return false;
        }

        ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableResource);

        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableKey);

        LootParams context = new LootParams.Builder(serverLevel).
            withParameter(LootContextParams.THIS_ENTITY, mob).
            withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).
            create(LootContextParamSets.GIFT);

        List<ItemStack> lootItems = lootTable.getRandomItems(context);

        if (lootItems.isEmpty())
        {
            // Nothing to drop
            return false;
        }

        lootItems.forEach(itemStack -> ItemTransferUtil.insertBellowOrDrop(serverLevel,
            itemStack,
            blockPos,
            blockPos.above()));

        return true;
    }
}
