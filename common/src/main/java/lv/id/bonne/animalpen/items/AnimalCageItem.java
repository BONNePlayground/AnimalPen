package lv.id.bonne.animalpen.items;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


/**
 * This is a main item that allows to pick up animals.
 */
public class AnimalCageItem extends Item
{
    public AnimalCageItem(Properties properties)
    {
        super(properties);
    }


    @Override
    public void appendHoverText(ItemStack itemStack,
        @Nullable Level level,
        List<Component> list,
        TooltipFlag tooltipFlag)
    {
        super.appendHoverText(itemStack, level, list, tooltipFlag);

        if (!list.isEmpty())
        {
            // Add emtpy line
            list.add(TextComponent.EMPTY);
        }

        if (itemStack.hasTag() &&
            itemStack.getTag().contains(TAG_ENTITY_ID))
        {
            list.add(new TranslatableComponent("item.animal_pen.animal_cage.entity",
                AnimalCageItem.getEntityTranslationName(itemStack.getTag().getString(TAG_ENTITY_ID))).
                withStyle(ChatFormatting.GRAY));
        }

        if (itemStack.hasTag() && itemStack.getTag().contains(TAG_AMOUNT))
        {
            list.add(new TranslatableComponent("item.animal_pen.animal_cage.amount",
                itemStack.getTag().getLong(TAG_AMOUNT)).
                withStyle(ChatFormatting.GRAY));
        }

        if (!itemStack.hasTag() ||
            !itemStack.getTag().contains(TAG_ENTITY_ID))
        {
            list.add(new TranslatableComponent("item.animal_pen.animal_cage.tip").
                withStyle(ChatFormatting.GRAY));
        }

        list.add(new TranslatableComponent("item.animal_pen.animal_cage.warning").
            withStyle(ChatFormatting.GRAY));
    }


    /**
     * This method attempts to merge entities into the itemStack if they match the required criteria.
     *
     * @param itemStack       The {@link ItemStack} used for interaction.
     * @param player          The {@link Player} performing the interaction.
     * @param livingEntity    The {@link LivingEntity} being interacted with.
     * @param interactionHand The {@link InteractionHand} used for the interaction.
     * @return {@link InteractionResult#SUCCESS} if the entity is successfully merged into the item,
     *         {@link InteractionResult#FAIL} otherwise.
     */
    @Override
    @NotNull
    public InteractionResult interactLivingEntity(ItemStack itemStack,
        Player player,
        LivingEntity livingEntity,
        InteractionHand interactionHand)
    {
        InteractionResult interactionResult =
            super.interactLivingEntity(itemStack, player, livingEntity, interactionHand);

        if (interactionResult == InteractionResult.FAIL)
        {
            player.displayClientMessage(new TranslatableComponent("item.animal_pen.animal_cage.error.unknown").
                withStyle(ChatFormatting.DARK_RED), true);
            return interactionResult;
        }

        if (player.getLevel().isClientSide() || livingEntity instanceof Player || !livingEntity.isAlive())
        {
            // Only  server side
            return InteractionResult.FAIL;
        }

        if (AnimalPen.CONFIG_MANAGER.getConfiguration().isBlocked(livingEntity.getType()))
        {
            player.displayClientMessage(new TranslatableComponent("item.animal_pen.animal_cage.error.blocked").
                withStyle(ChatFormatting.DARK_RED), true);
            // only living entities that are not babies
            return InteractionResult.FAIL;
        }

        if (!livingEntity.isAlive() || livingEntity.isBaby())
        {
            player.displayClientMessage(new TranslatableComponent("item.animal_pen.animal_cage.error.baby").
                withStyle(ChatFormatting.DARK_RED), true);
            // only living entities that are not babies
            return InteractionResult.FAIL;
        }

        if (!(livingEntity instanceof Animal animal))
        {
            player.displayClientMessage(new TranslatableComponent("item.animal_pen.animal_cage.error.not_animal").
                withStyle(ChatFormatting.DARK_RED), true);
            // only living entities that are not babies
            return InteractionResult.FAIL;
        }

        if (livingEntity instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame() ||
            livingEntity instanceof AbstractHorse horse && horse.isTamed())
        {
            player.displayClientMessage(new TranslatableComponent("item.animal_pen.animal_cage.error.tame").
                withStyle(ChatFormatting.DARK_RED), true);
            // cannot add into jar tamed animals
            return InteractionResult.FAIL;
        }

        if (!this.matchEntity(itemStack, livingEntity))
        {
            player.displayClientMessage(new TranslatableComponent("item.animal_pen.animal_cage.error.wrong").
                withStyle(ChatFormatting.DARK_RED), true);
            // Different entities cannot be merged.
            return InteractionResult.FAIL;
        }

        CompoundTag itemTag = itemStack.getOrCreateTag();

        if (!itemTag.contains(TAG_ENTITY_ID))
        {
            animal.save(itemTag);
        }

        if (itemTag.contains(TAG_AMOUNT))
        {
            long maxCount = AnimalPen.CONFIG_MANAGER.getConfiguration().getMaximalAnimalCount();

            if (maxCount > 0 && itemTag.getLong(TAG_AMOUNT) + 1 > maxCount)
            {
                return InteractionResult.FAIL;
            }

            itemTag.putLong(TAG_AMOUNT, itemTag.getLong(TAG_AMOUNT) + 1);
        }

        itemStack.setTag(itemTag);
        player.setItemInHand(interactionHand, itemStack);
        livingEntity.remove(Entity.RemovalReason.DISCARDED);

        return InteractionResult.SUCCESS;
    }


    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext useOnContext)
    {
        if (useOnContext.getLevel().isClientSide())
        {
            return super.useOn(useOnContext);
        }

        BlockEntity blockEntity = useOnContext.getLevel().getBlockEntity(useOnContext.getClickedPos());

        if (!(blockEntity instanceof AnimalPenTileEntity tileEntity))
        {
            return super.useOn(useOnContext);
        }

        if (useOnContext.getPlayer() != null &&
            tileEntity.processContainer(useOnContext.getPlayer(), useOnContext.getHand()))
        {
            return InteractionResult.SUCCESS;
        }

        return super.useOn(useOnContext);
    }


    /**
     * This method returns if given entity matches entity that is stored inside given item stack.
     * @param itemStack The {@link ItemStack} used for interaction.
     * @param entity    The {@link LivingEntity} being interacted with.
     * @return {@code true} if entity matches stored entity, {@code false} otherwise.
     */
    private boolean matchEntity(ItemStack itemStack, LivingEntity entity)
    {
        CompoundTag itemTag = itemStack.getTag();

        if (itemTag == null || !itemTag.contains(TAG_ENTITY_ID))
        {
            // Empty cage.
            return true;
        }

        String entityType = itemTag.getString(TAG_ENTITY_ID);

        return new ResourceLocation(entityType).equals(entity.getType().arch$registryName());
    }


    /**
     * This method returns translated entity name.
     * @param entityId Entity ID.
     * @return Component that contains translated entity name.
     */
    private static Component getEntityTranslationName(String entityId)
    {
        EntityType<?> entityType = EntityType.byString(entityId).orElse(null);

        if (entityType != null)
        {
            // Returns a translatable component
            return entityType.getDescription();
        }
        else
        {
            // Fallback to raw ID if not found
            return new TextComponent(entityId);
        }
    }


    public static final String TAG_ENTITY_ID = "id";

    public static final String TAG_AMOUNT = "animal_count";
}
