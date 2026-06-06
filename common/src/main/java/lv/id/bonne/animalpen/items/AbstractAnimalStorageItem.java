package lv.id.bonne.animalpen.items;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Consumer;

import lv.id.bonne.animalpen.client.screens.VariantsConfigScreen;
import lv.id.bonne.animalpen.data.saveddata.IndividualPenStorage;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.items.component.StoredMob;
import lv.id.bonne.animalpen.items.component.StoredMobData;
import lv.id.bonne.animalpen.items.component.StoredMobVariantKey;
import lv.id.bonne.animalpen.items.component.StoredMobVariants;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenCriteriaTriggersRegistry;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import lv.id.bonne.animalpen.util.AnimalPenVariantHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;


/**
 * This is abstract animal storage item class that contains all common methods for items.
 */
public abstract class AbstractAnimalStorageItem extends Item
{
    protected AbstractAnimalStorageItem(Properties properties)
    {
        super(properties);
    }


// ---------------------------------------------------------------------
// Section: Abstract methods
// ---------------------------------------------------------------------


    /**
     * This method returns tooltip base text for item
     */
    protected abstract String tooltipKeyBase();


    /**
     * This method returns entity type key.
     */
    protected abstract TagKey<EntityType<?>> pickableTag();


    @Nullable
    protected abstract BlockEntityHandler blockEntityHandler();


// ---------------------------------------------------------------------
// Section: Migration
// ---------------------------------------------------------------------


    @Override
    public void verifyComponentsAfterLoad(ItemStack itemStack)
    {
        super.verifyComponentsAfterLoad(itemStack);

        // Minecraft 1.20.4 < upgrade to 1.20.5+
        if (itemStack.has(DataComponents.CUSTOM_DATA))
        {
            CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
            CompoundTag compoundTag = customData.getUnsafe();
            int variantAmount = 0;

            // Target animal variants
            if (compoundTag.contains(AnimalPenCompoundTags.TAG_VARIANTS))
            {
                ListTag nbtVariantList = compoundTag.getListOrEmpty(AnimalPenCompoundTags.TAG_VARIANTS);

                List<CompoundTag> variantList = new ArrayList<>(nbtVariantList.size());
                nbtVariantList.forEach(tag -> variantList.add((CompoundTag) tag));

                itemStack.set(AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get(),
                    StoredMobVariants.of(variantList));
                variantAmount = variantList.size();
            }

            // Target animal data
            if (compoundTag.contains(AnimalPenCompoundTags.TAG_ANIMAL_DATA))
            {
                CompoundTag animalData = compoundTag.getCompoundOrEmpty(AnimalPenCompoundTags.TAG_ANIMAL_DATA);
                long animalCount = animalData.getLongOr(AnimalPenCompoundTags.TAG_AMOUNT, 0L);
                CompoundTag cooldowns = animalData.getCompoundOrEmpty(AnimalPenCompoundTags.TAG_COOLDOWN);

                Map<String, Long> cooldownMap = new HashMap<>(cooldowns.size());
                cooldowns.entrySet().forEach(key ->
                    cooldownMap.put(key.getKey(), cooldowns.getLongOr(key.getKey(), 0L)));

                animalData.remove(AnimalPenCompoundTags.TAG_AMOUNT);
                animalData.remove(AnimalPenCompoundTags.TAG_COOLDOWN);

                Map<String, Integer> propertiesMap = new HashMap<>(animalData.size());
                animalData.keySet().forEach(key ->
                    propertiesMap.put(key, animalData.getIntOr(key, 0)));

                itemStack.set(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get(),
                    StoredMobData.of(animalCount, propertiesMap, new HashMap<>()));
            }

            // Target animal itself
            if (compoundTag.contains(AnimalPenCompoundTags.TAG_ANIMAL))
            {
                CompoundTag animal = compoundTag.getCompoundOrEmpty(AnimalPenCompoundTags.TAG_ANIMAL);
                String entityId = animal.getStringOr(AnimalPenCompoundTags.TAG_ENTITY_ID, "");

                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(entityId)).
                    ifPresent(entityType ->
                        itemStack.set(AnimalPenDataComponentRegistry.MOB_COMPONENT.get(),
                            StoredMob.of(entityType.value(), animal)));
            }

            Optional<int[]> optionalID = compoundTag.getIntArray(AnimalPenCompoundTags.TAG_STORAGE_ID);

            if (optionalID.isPresent())
            {
                itemStack.set(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get(),
                    StoredMobVariantKey.of(
                        UUIDUtil.uuidFromIntArray(optionalID.get()),
                        variantAmount));
            }

            itemStack.remove(DataComponents.CUSTOM_DATA);
        }

        // Animal Pens 1.6 to 2.0 upgrade
        if (itemStack.has(AnimalPenDataComponentRegistry.ENTITY_VARIANTS.get()))
        {
            CustomData customData = itemStack.get(AnimalPenDataComponentRegistry.ENTITY_VARIANTS.get());
            ListTag nbtVariants = customData.getUnsafe().getListOrEmpty("animal_variants");
            List<CompoundTag> variantList = new ArrayList<>(nbtVariants.size());
            nbtVariants.forEach(tag -> variantList.add((CompoundTag) tag));

            itemStack.set(AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get(),
                StoredMobVariants.of(variantList));

            itemStack.remove(AnimalPenDataComponentRegistry.ENTITY_VARIANTS.get());
        }

        // Move from entity data to my custom mob storage.
        if (itemStack.has(DataComponents.ENTITY_DATA))
        {
            CustomData customData = itemStack.get(DataComponents.ENTITY_DATA);
            CompoundTag compoundTag = customData.getUnsafe();

            // Target animal count as other data is lost (cooldowns are not worth the effort)
            if (compoundTag.contains(AnimalPenCompoundTags.TAG_AMOUNT))
            {
                long animalCount = compoundTag.getLongOr(AnimalPenCompoundTags.TAG_AMOUNT, 0);
                Map<String, Long> cooldownMap = new HashMap<>(0);
                compoundTag.remove(AnimalPenCompoundTags.TAG_AMOUNT);
                Map<String, Integer> propertiesMap = new HashMap<>(0);
                itemStack.set(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get(),
                    StoredMobData.of(animalCount, propertiesMap, cooldownMap));
            }

            // Target animal itself
            if (compoundTag.contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
            {
                String entityId = compoundTag.getStringOr(AnimalPenCompoundTags.TAG_ENTITY_ID, "");
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(entityId)).
                    ifPresent(entityType ->
                        itemStack.set(AnimalPenDataComponentRegistry.MOB_COMPONENT.get(),
                            StoredMob.of(entityType.value(), compoundTag)));
            }

            itemStack.remove(DataComponents.ENTITY_DATA);
        }
    }


// ---------------------------------------------------------------------
// Section: Tooltip
// ---------------------------------------------------------------------


    @Override
    public void appendHoverText(ItemStack stack,
        TooltipContext tooltipContext,
        TooltipDisplay tooltipDisplay,
        Consumer<Component> tooltip,
        TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, tooltipContext, tooltipDisplay, tooltip, tooltipFlag);

        if (stack.has(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()) &&
            stack.has(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get()))
        {
            StoredMob storedMob = stack.get(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());
            StoredMobData storedMobData = stack.get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());

            tooltip.accept(Component.translatable(this.tooltipKeyBase() + ".entity",
                    storedMob.entityType().getDescription()).
                withStyle(ChatFormatting.GRAY));

            tooltip.accept(Component.translatable(this.tooltipKeyBase() + ".amount",
                    storedMobData.animalCount()).
                withStyle(ChatFormatting.GRAY));

            if (stack.has(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get()))
            {
                StoredMobVariantKey variantKey =
                    stack.get(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());

                tooltip.accept(Component.translatable(this.tooltipKeyBase() + ".variants",
                        variantKey.amount()).
                    withStyle(ChatFormatting.GRAY));
            }

            tooltip.accept(Component.empty());
            tooltip.accept(Component.translatable(this.tooltipKeyBase() + ".release").
                withStyle(ChatFormatting.GRAY));
        }

        if (!stack.has(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()))
        {
            tooltip.accept(Component.translatable(this.tooltipKeyBase() + ".tip").
                withStyle(ChatFormatting.GRAY));
        }
    }


// ---------------------------------------------------------------------
// Section: Interaction
// ---------------------------------------------------------------------


    @Override
    @NotNull
    public InteractionResult interactLivingEntity(ItemStack stack,
        @NotNull Player player,
        LivingEntity target,
        InteractionHand hand)
    {
        if (super.interactLivingEntity(stack, player, target, hand) == InteractionResult.FAIL)
        {
            this.error(player, ".error.unknown");
            return InteractionResult.FAIL;
        }

        if (player.level().isClientSide() || !(target instanceof Mob mob))
        {
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!target.isAlive() || target.isBaby())
        {
            this.error(player, ".error.baby");
            return InteractionResult.FAIL;
        }

        if (!target.getType().is(this.pickableTag()))
        {
            this.error(player, ".error.not_allowed");
            return InteractionResult.FAIL;
        }

        if (mob instanceof OwnableEntity o && o.getOwnerReference() != null)
        {
            this.error(player, ".error.tame");
            return InteractionResult.FAIL;
        }

        if ((mob instanceof AbstractChestedHorse h && h.hasChest()))
        {
            this.error(player, ".error.chested");
            return InteractionResult.FAIL;
        }

        if (!this.matchEntity(stack, mob))
        {
            this.error(player, ".error.wrong");
            return InteractionResult.FAIL;
        }

        this.captureMob(stack, player, mob, hand);

        return InteractionResult.SUCCESS;
    }


    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext context)
    {
        if (context.getLevel().isClientSide())
        {
            return super.useOn(context);
        }

        Player player = context.getPlayer();

        if (player == null)
        {
            return InteractionResult.FAIL;
        }

        // 1. Try to pass interaction to a block entity (pen / aquarium)
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());

        BlockEntityHandler handler = this.blockEntityHandler();

        if (handler != null && handler.handle(blockEntity, player, context.getHand()))
        {
            return InteractionResult.SUCCESS;
        }

        // 2. Sneak + top click = release entity
        if (player.isCrouching())
        {
            return this.tryRelease(context);
        }

        return super.useOn(context);
    }


    private void captureMob(ItemStack stack, Player player, Mob mob, InteractionHand hand)
    {
        mob.ejectPassengers();

        if (mob.isPassenger())
        {
            // Eject before processing
            mob.stopRiding();
        }

        // Drop items
        this.dropEquipment(mob);

        if (mob.isLeashed())
        {
            // Drop leash
            mob.dropLeash();
        }

        if (!stack.has(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()))
        {
            CompoundTag animalTag = AnimalPenVariantHelper.saveMob(mob);
            stack.set(AnimalPenDataComponentRegistry.MOB_COMPONENT.get(),
                StoredMob.of(mob.getType(), animalTag));
        }

        stack.update(
            AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get(),
            StoredMobData.of(0, new HashMap<>(), new HashMap<>()),
            data -> StoredMobData.of(
                data.animalCount() + 1,
                data.properties(),
                data.cooldowns())
        );

        AnimalPenVariantHelper.storeAnimalVariant(stack, mob, player, message -> this.error(player, message));

        mob.remove(Entity.RemovalReason.DISCARDED);
        player.setItemInHand(hand, stack);

        AnimalPenCriteriaTriggersRegistry.ANIMAL_ITEM_USE_TRIGGER.get().trigger((ServerPlayer) player,
            mob,
            stack,
            false);
    }


    private InteractionResult tryRelease(UseOnContext context)
    {
        ItemStack stack = context.getItemInHand();

        if (!stack.has(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()))
        {
            // Empty container
            return InteractionResult.PASS;
        }

        ServerLevel level = (ServerLevel) context.getLevel();

        ListTag pos = new ListTag();
        BlockPos releaseBlock = context.getClickedPos().relative(context.getClickedFace());

        pos.add(DoubleTag.valueOf(releaseBlock.getX() + 0.5));
        pos.add(DoubleTag.valueOf(releaseBlock.getY()));
        pos.add(DoubleTag.valueOf(releaseBlock.getZ() + 0.5));

        StoredMob storedMob = stack.get(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());
        CompoundTag animalTag = storedMob.tag();

        animalTag.put("Pos", pos);
        animalTag.remove("UUID");

        Entity mobEntity = storedMob.entityType().create(level, EntitySpawnReason.SPAWN_ITEM_USE);

        if (!(mobEntity instanceof Mob mob))
        {
            AnimalPen.sendDebug("Failed to spawn mob in given world.");
            return InteractionResult.SUCCESS;
        }

        AnimalPenVariantHelper.loadMob(mob, animalTag);

        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            mob.setItemSlot(slot, ItemStack.EMPTY);
        }

        level.addFreshEntity(mob);
        this.decrementStoredAmount(stack, level);

        AnimalPenCriteriaTriggersRegistry.ANIMAL_ITEM_USE_TRIGGER.get().trigger((ServerPlayer) context.getPlayer(),
            mob,
            stack,
            true);

        return InteractionResult.SUCCESS;
    }


// ---------------------------------------------------------------------
// Section: Utilities
// ---------------------------------------------------------------------


    @Override
    public void onDestroyed(ItemEntity itemEntity)
    {
        StoredMobVariantKey storedKey =
            itemEntity.getItem().get(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());

        if (storedKey != null && itemEntity.level() instanceof ServerLevel serverLevel)
        {
            IndividualPenStorage.delete(serverLevel, storedKey.key());
        }

        super.onDestroyed(itemEntity);
    }


    private void decrementStoredAmount(ItemStack stack, ServerLevel level)
    {
        StoredMobData data = stack.get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());

        if (data == null || data.animalCount() <= 1)
        {
            stack.remove(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());
            stack.remove(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());
            stack.remove(AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get());
            StoredMobVariantKey removedKey = stack.remove(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());

            // Remove deep storage from it
            if (removedKey != null && level instanceof ServerLevel serverLevel)
            {
                IndividualPenStorage.delete(serverLevel, removedKey.key());
            }
        }
        else
        {
            stack.set(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get(),
                StoredMobData.of(data.animalCount() - 1, data.properties(), data.cooldowns()));
        }
    }


    private void dropEquipment(Mob mob)
    {
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            if (!mob.hasItemInSlot(slot))
            {
                continue;
            }

            ItemStack stack = mob.getItemBySlot(slot);

            if (mob.getRandom().nextFloat() < mob.getDropChances().byEquipment(slot))
            {
                Block.popResource(mob.level(), mob.blockPosition(), stack);
            }

            mob.setDropChance(slot, 0);
        }
    }


    private boolean matchEntity(ItemStack stack, LivingEntity entity)
    {
        if (!stack.has(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()))
        {
            return true;
        }

        return entity.getType() ==
            stack.get(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()).entityType();
    }


    public void error(Player player, String suffix)
    {
        player.displayClientMessage(
            Component.translatable(tooltipKeyBase() + suffix).
                withStyle(ChatFormatting.DARK_RED),
            true);
    }


    @FunctionalInterface
    protected interface BlockEntityHandler
    {
        boolean handle(BlockEntity be, Player player, InteractionHand hand);
    }
}