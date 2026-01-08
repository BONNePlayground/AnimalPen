package lv.id.bonne.animalpen.processing.function.wrapper;


import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.Nullable;

import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.registries.AnimalPenFunctionRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;


/**
 * This is wrapper for forge around EntityFunction interface.
 */
public final class EntityFunctionEntry
{
    public EntityFunctionEntry(EntityFunction function)
    {
        this.function = function;
    }


    public EntityFunction function()
    {
        return function;
    }


    public boolean interactPlayer(
        ServerPlayer player,
        InteractionHand hand,
        ItemStack item,
        int amount,
        Mob mob,
        ItemStack componentHolder,
        BlockPos pos,
        @Nullable String dataKey,
        @Nullable Value dataValue)
    {
        return function.interactPlayer(
            player, hand, item, amount,
            mob, componentHolder, pos,
            dataKey, dataValue
        );
    }


    public boolean interactDispenser(
        ServerLevel level,
        Container inventory,
        ItemStack item,
        int amount,
        Mob mob,
        ItemStack componentHolder,
        BlockPos pos,
        @Nullable String dataKey,
        @Nullable Value dataValue)
    {
        return function.interactDispenser(
            level, inventory, item, amount,
            mob, componentHolder, pos,
            dataKey, dataValue
        );
    }


    public boolean processFunction(
        ServerLevel level,
        Mob mob,
        ItemStack componentHolder,
        BlockPos pos,
        @Nullable String dataKey,
        @Nullable Value dataValue)
    {
        return function.processFunction(
            level, mob, componentHolder, pos,
            dataKey, dataValue
        );
    }


    private final EntityFunction function;


    public static final Codec<EntityFunctionEntry> CODEC = ResourceLocation.CODEC.flatXmap(
        id ->
        {
            EntityFunctionEntry entry = AnimalPenFunctionRegistry.ENTITY_FUNCTIONS.get(id);
            return entry != null ? DataResult.success(entry) : DataResult.error(() -> "Unknown entity_function: " + id);
        },
        entry ->
        {
            ResourceLocation id = AnimalPenFunctionRegistry.ENTITY_FUNCTIONS.getId(entry);
            return id != null ? DataResult.success(id) : DataResult.error(() -> "Unregistered entity_function: " + entry);
        }
    );
}