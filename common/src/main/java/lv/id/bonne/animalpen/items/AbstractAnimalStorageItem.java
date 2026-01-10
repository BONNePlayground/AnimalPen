package lv.id.bonne.animalpen.items;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

import lv.id.bonne.animalpen.mixin.invokers.MobInvoker;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import lv.id.bonne.animalpen.util.AnimalPenVariantHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
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
    public void verifyTagAfterLoad(CompoundTag compoundTag)
    {
        super.verifyTagAfterLoad(compoundTag);

        if (!compoundTag.contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
        {
            return;
        }

        Tag variants = compoundTag.get(AnimalPenCompoundTags.TAG_VARIANTS);
        long count = compoundTag.getLong(AnimalPenCompoundTags.TAG_AMOUNT);

        CompoundTag animalData = new CompoundTag();
        animalData.putLong(AnimalPenCompoundTags.TAG_AMOUNT, count);

        compoundTag.remove(AnimalPenCompoundTags.TAG_VARIANTS);
        compoundTag.remove(AnimalPenCompoundTags.TAG_AMOUNT);

        CompoundTag animal = compoundTag.copy();
        CompoundTag cooldownData = new CompoundTag();

        animal.getAllKeys().forEach(key ->
        {
            if (key.endsWith("_cooldown"))
            {
                cooldownData.putLong(key, compoundTag.getLong(key));
            }

            compoundTag.remove(key);
        });

        animalData.put(AnimalPenCompoundTags.TAG_COOLDOWN, cooldownData);

        compoundTag.put(AnimalPenCompoundTags.TAG_VARIANTS, variants);
        compoundTag.put(AnimalPenCompoundTags.TAG_ANIMAL, animal);
        compoundTag.put(AnimalPenCompoundTags.TAG_ANIMAL_DATA, animalData);
    }


// ---------------------------------------------------------------------
// Section: Tooltip
// ---------------------------------------------------------------------


    @Override
    public void appendHoverText(ItemStack stack,
        @Nullable Level level,
        List<Component> tooltip,
        TooltipFlag flag)
    {
        super.appendHoverText(stack, level, tooltip, flag);

        if (!tooltip.isEmpty())
        {
            tooltip.add(Component.empty());
        }

        if (stack.hasTag())
        {
            CompoundTag animal = stack.getTag().getCompound(AnimalPenCompoundTags.TAG_ANIMAL);
            CompoundTag data = stack.getTag().getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA);

            if (animal.contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
            {
                tooltip.add(Component.translatable(this.tooltipKeyBase() + ".entity",
                    getEntityTranslationName(animal.getString(AnimalPenCompoundTags.TAG_ENTITY_ID))).
                    withStyle(ChatFormatting.GRAY));
            }

            if (data.contains(AnimalPenCompoundTags.TAG_AMOUNT))
            {
                tooltip.add(Component.translatable(this.tooltipKeyBase() + ".amount",
                    data.getLong(AnimalPenCompoundTags.TAG_AMOUNT)).
                    withStyle(ChatFormatting.GRAY));
            }

            if (stack.getTag().contains(AnimalPenCompoundTags.TAG_VARIANTS))
            {
                tooltip.add(Component.translatable(this.tooltipKeyBase() + ".variants",
                    stack.getTag().getList(AnimalPenCompoundTags.TAG_VARIANTS, Tag.TAG_COMPOUND).size()).
                    withStyle(ChatFormatting.GRAY));
            }

            if (animal.contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
            {
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable(this.tooltipKeyBase() + ".release").
                    withStyle(ChatFormatting.GRAY));
            }
        }

        if (!stack.hasTag() ||
            !stack.getTag().getCompound(AnimalPenCompoundTags.TAG_ANIMAL).
                contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
        {
            tooltip.add(Component.translatable(this.tooltipKeyBase() + ".tip").
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
        if (player.level.isClientSide() || !(target instanceof Mob mob))
        {
            return InteractionResult.FAIL;
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

        if ((mob instanceof OwnableEntity o && o.getOwnerUUID() != null) ||
            (mob instanceof AbstractHorse h && h.getOwnerUUID() != null))
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
            mob.dropLeash(true, true);
        }

        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains(AnimalPenCompoundTags.TAG_ANIMAL))
        {
            CompoundTag animalTag = new CompoundTag();
            mob.save(animalTag);

            animalTag.remove("UUID");
            animalTag.remove("Pos");

            tag.put(AnimalPenCompoundTags.TAG_ANIMAL, animalTag);
        }

        CompoundTag data = tag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA);
        data.putLong(AnimalPenCompoundTags.TAG_AMOUNT,
            data.getLong(AnimalPenCompoundTags.TAG_AMOUNT) + 1);

        tag.put(AnimalPenCompoundTags.TAG_ANIMAL_DATA, data);
        stack.setTag(tag);

        AnimalPenVariantHelper.storeAnimalVariant(stack, mob, player);

        mob.remove(Entity.RemovalReason.DISCARDED);
        player.setItemInHand(hand, stack);
    }


    private InteractionResult tryRelease(UseOnContext context)
    {
        ItemStack stack = context.getItemInHand();
        CompoundTag animal = stack.getOrCreateTag().getCompound(AnimalPenCompoundTags.TAG_ANIMAL).copy();

        if (!animal.contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
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

        animal.put("Pos", pos);
        animal.remove("UUID");

        EntityType.create(animal, level).
            map(entity -> (Mob) entity).
            ifPresent(mob ->
            {
                // Strip equipment from spawned mob
                for (EquipmentSlot slot : EquipmentSlot.values())
                {
                    mob.setItemSlot(slot, ItemStack.EMPTY);
                }

                level.addFreshEntity(mob);
                this.decrementStoredAmount(stack, context.getPlayer(), context.getHand());
            });

        return InteractionResult.SUCCESS;
    }


// ---------------------------------------------------------------------
// Section: Utilities
// ---------------------------------------------------------------------


    private void decrementStoredAmount(ItemStack stack, Player player, InteractionHand hand)
    {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag data = tag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA);

        long amount = data.getLong(AnimalPenCompoundTags.TAG_AMOUNT) - 1;

        if (amount <= 0)
        {
            // Clear item completely
            stack.setTag(new CompoundTag());
        }
        else
        {
            data.putLong(AnimalPenCompoundTags.TAG_AMOUNT, amount);
            tag.put(AnimalPenCompoundTags.TAG_ANIMAL_DATA, data);
            stack.setTag(tag);
        }

        player.setItemInHand(hand, stack);
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

            if (mob.getRandom().nextFloat() < ((MobInvoker) mob).callGetEquipmentDropChance(slot))
            {
                Block.popResource(mob.level, mob.blockPosition(), stack);
            }

            mob.setDropChance(slot, 0);
        }
    }


    private boolean matchEntity(ItemStack stack, LivingEntity entity)
    {
        CompoundTag tag = stack.getTag();

        if (tag == null)
        {
            return true;
        }

        CompoundTag animal = tag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL);

        if (!animal.contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
        {
            return true;
        }

        return new ResourceLocation(
            animal.getString(AnimalPenCompoundTags.TAG_ENTITY_ID)).
            equals(entity.getType().arch$registryName());
    }


    private void error(Player player, String suffix)
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


    private static Component getEntityTranslationName(String entityId)
    {
        return EntityType.byString(entityId).
            map(EntityType::getDescription).
            orElse(Component.literal(entityId));
    }
}