package lv.id.bonne.animalpen.interaction.model;


import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Optional;

import lv.id.bonne.animalpen.interaction.condition.ConditionEntry;
import lv.id.bonne.animalpen.interaction.cooldown.CooldownEntry;
import lv.id.bonne.animalpen.interaction.function.FunctionKey;
import lv.id.bonne.animalpen.interaction.ingredient.CustomIngredient;
import lv.id.bonne.animalpen.interaction.textentry.TextEntry;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import lv.id.bonne.animalpen.util.CustomCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;


/**
 * This class stores interaction properties that can be done to the entity.
 *
 * @param id - the ID of interaction. Used for detecting pending `cooldown`
 * @param ingredient - the ingredient that performs interaction
 * @param consume - indicates if item will be consumed by action
 * @param damage - indicates if item will be damaged by action
 * @param conditions - the list of conditions for this interaction to be possible to act
 * @param lootTable - the loot-table from which loot will be generated when interaction is performed
 * @param dropLimit - the limit of items that will be dropped from given loot-table
 * @param perEntity - the indication if loot-table will be generated per-entity or just once.
 * @param even - the indication if operation will be performed on even number of entities or all
 * @param cooldown - the cooldown options that will be added after interaction is performed.
 * @param textLines - the description text that will be added by this interaction
 * @param runFunctions - the list of functions that be run when interaction is performed
 * @param finishFunctions - the list of functions that will be run after cooldown of interaction finishes
 * @param sound - the sound that will be played when interaction is performed
 * @param redstoneSignal - the bit index of redstone signal that is affected by current interaction
 */
public record AnimalInteraction(@NotNull String id,
                                @Nullable CustomIngredient ingredient, boolean consume, int damage,
                                @NotNull List<ConditionEntry> conditions, @Nullable ResourceLocation lootTable,
                                int dropLimit,
                                boolean perEntity, boolean even, @Nullable CooldownEntry cooldown,
                                @NotNull List<TextEntry> textLines,
                                @NotNull List<FunctionKey> runFunctions, @NotNull List<FunctionKey> finishFunctions,
                                @Nullable ResourceLocation sound,
                                int redstoneSignal)
{
    /**
     * This method checks if this interaction matches all conditions defined for it to operate.
     *
     * @param mobNBT The mobNBT tag
     * @return {@code true} if all conditions allows to run interaction, {@code false} otherwise
     */
    public boolean matchAllConditions(CompoundTag mobNBT)
    {
        return this.conditions().stream().allMatch(condition -> condition.matchCondition(mobNBT));
    }


    /**
     * This method normalizes mob count if `even` is toggled.
     *
     * @param mobCount the input animal count
     * @return normalized animal count value
     */
    public int normalizeMobCount(int mobCount)
    {
        if (this.even && (mobCount & 1) == 1)
        {
            return mobCount - 1;
        }

        return mobCount;
    }


    /**
     * This method applies cooldown into mobNBT.
     *
     * @param mobNBT The NBT data where information is stored
     * @param mobCount The mob count for cooldown calculations
     * @return {@code true} if cooldown was applied, {@code false} otherwise
     */
    public boolean applyCooldown(CompoundTag mobNBT, int mobCount)
    {
        if (this.cooldown == null)
        {
            return false;
        }

        CompoundTag animalData = mobNBT.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA);
        CompoundTag cooldownNBT = animalData.getCompound(AnimalPenCompoundTags.TAG_COOLDOWN);
        cooldownNBT.putInt(this.id, this.cooldown.calculateCooldown(mobCount));

        // Need to put if it was missing before.
        animalData.put(AnimalPenCompoundTags.TAG_COOLDOWN, cooldownNBT);
        mobNBT.put(AnimalPenCompoundTags.TAG_ANIMAL_DATA, animalData);

        return true;
    }


    /**
     * This method triggers all functions invoked by player entity.
     *
     * @param player The player who triggers interaction
     * @param interactionHand The interaction hand
     * @param consumedItem The consumed item
     * @param consumedAmount The amount of consumed items
     * @param mob The mob that is interacted with
     * @param mobNBT The NBT data of mob
     * @param position The position of interaction
     * @return {@code true} if functions modified data, {@code false} otherwise.
     */
    public boolean triggerFunctions(@NotNull ServerPlayer player,
        InteractionHand interactionHand,
        ItemStack consumedItem,
        int consumedAmount,
        @NotNull Mob mob,
        @NotNull CompoundTag mobNBT,
        @NotNull BlockPos position)
    {
        boolean dataUpdate = false;

        for (FunctionKey function : this.runFunctions)
        {
            dataUpdate |= function.id().interactPlayer(player,
                interactionHand,
                consumedItem,
                consumedAmount,
                mob,
                mobNBT,
                position,
                function.key(),
                function.value());
        }

        return dataUpdate;
    }


    /**
     * This method triggers all functions invoked by dispenser type block.
     *
     * @param level The level where interaction is triggered
     * @param container The container of block which interacted
     * @param consumedItem The consumed item
     * @param consumedAmount The amount of consumed items
     * @param mob The mob that is interacted with
     * @param mobNBT The NBT data of mob
     * @param position The position of interaction
     * @return {@code true} if functions modified data, {@code false} otherwise.
     */
    public boolean triggerFunctions(ServerLevel level,
        Container container,
        ItemStack consumedItem,
        int consumedAmount,
        Mob mob,
        CompoundTag mobNBT,
        BlockPos position)
    {
        boolean dataUpdate = false;

        for (FunctionKey function : this.runFunctions)
        {
            dataUpdate |= function.id().interactDispenser(level,
                container,
                consumedItem,
                consumedAmount,
                mob,
                mobNBT,
                position,
                function.key(),
                function.value());
        }

        return dataUpdate;
    }


    /**
     * This codec allows to read AnimalInteraction from JSON.
     */
    public static final Codec<AnimalInteraction> CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(a -> a.id),
            CustomIngredient.CODEC.optionalFieldOf("items", CustomIngredient.EMPTY)
                .forGetter(a -> a.ingredient),
            Codec.BOOL.optionalFieldOf("consume", false)
                .forGetter(a -> a.consume),
            Codec.INT.optionalFieldOf("damage", 0)
                .forGetter(a -> a.damage),
            CustomCodec.strictOptionalListField("conditions", ConditionEntry.CODEC)
                .forGetter(a -> a.conditions),
            ResourceLocation.CODEC.optionalFieldOf("loot_table")
                .forGetter(a -> Optional.ofNullable(a.lootTable)),
            Codec.INT.optionalFieldOf("drop_limit", Integer.MAX_VALUE)
                .forGetter(a -> a.dropLimit),
            Codec.BOOL.optionalFieldOf("per_entity", false)
                .forGetter(a -> a.perEntity),
            Codec.BOOL.optionalFieldOf("even_entity_count", false)
                .forGetter(a -> a.even),
            CooldownEntry.CODEC.optionalFieldOf("cooldown")
                .forGetter(a -> Optional.ofNullable(a.cooldown)),
            CustomCodec.strictOptionalListField("text_lines", TextEntry.CODEC)
                .forGetter(a -> a.textLines),
            CustomCodec.strictOptionalListField("run_functions", FunctionKey.CODEC)
                .forGetter(a -> a.runFunctions),
            CustomCodec.strictOptionalListField("finish_functions", FunctionKey.CODEC)
                .forGetter(a -> a.finishFunctions),
            ResourceLocation.CODEC.optionalFieldOf("sound")
                .forGetter(a -> Optional.ofNullable(a.sound)),
            Codec.INT.optionalFieldOf("redstone_signal", 0)
                .forGetter(a -> a.redstoneSignal)
        ).apply(instance, (id, items, consume, damage, conditions,
            loot, dropLimit, perEntity, even,
            cooldown, textLines, startFunctions,
            endFunctions, sound, redstone) ->
            new AnimalInteraction(
                id,
                items,
                consume,
                damage,
                conditions,
                loot.orElse(null),
                dropLimit,
                perEntity,
                even,
                cooldown.orElse(null),
                textLines,
                startFunctions,
                endFunctions,
                sound.orElse(null),
                redstone
            )
        ));
}
