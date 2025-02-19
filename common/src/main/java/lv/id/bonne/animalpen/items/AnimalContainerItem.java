package lv.id.bonne.animalpen.items;


import org.jetbrains.annotations.NotNull;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AquariumTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;


/**
 * This is a main item that allows to pick up water animals.
 */
public class AnimalContainerItem extends Item
{
    public AnimalContainerItem(Properties properties)
    {
        super(properties);
    }


    @Override
    public void appendHoverText(ItemStack itemStack,
        TooltipContext tooltipContext,
        List<Component> list,
        TooltipFlag tooltipFlag)
    {
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);

        if (!list.isEmpty())
        {
            // Add emtpy line
            list.add(Component.empty());
        }

        if (itemStack.has(DataComponents.ENTITY_DATA))
        {
            list.add(Component.translatable("item.animal_pen.water_animal_container.entity",
                AnimalContainerItem.getEntityTranslationName(itemStack.get(DataComponents.ENTITY_DATA).copyTag().getString(TAG_ENTITY_ID))).
                withStyle(ChatFormatting.GRAY));
        }

        if (itemStack.has(DataComponents.ENTITY_DATA))
        {
            list.add(Component.translatable("item.animal_pen.water_animal_container.amount",
                    itemStack.get(DataComponents.ENTITY_DATA).copyTag().getLong(TAG_AMOUNT)).
                withStyle(ChatFormatting.GRAY));
        }

        if (!itemStack.has(DataComponents.ENTITY_DATA))
        {
            list.add(Component.translatable("item.animal_pen.water_animal_container.tip").
                withStyle(ChatFormatting.GRAY));
        }

        list.add(Component.translatable("item.animal_pen.water_animal_container.warning").
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
            player.displayClientMessage(Component.translatable("item.animal_pen.water_animal_container.error.unknown").
                withStyle(ChatFormatting.DARK_RED), true);
            return interactionResult;
        }

        if (player.level().isClientSide() || livingEntity instanceof Player || !livingEntity.isAlive())
        {
            // Only  server side
            return InteractionResult.FAIL;
        }

        if (AnimalPen.CONFIG_MANAGER.getConfiguration().isBlocked(livingEntity.getType()))
        {
            player.displayClientMessage(Component.translatable("item.animal_pen.animal_cage.error.blocked").
                withStyle(ChatFormatting.DARK_RED), true);
            // only living entities that are not babies
            return InteractionResult.FAIL;
        }

        if (!livingEntity.isAlive() || livingEntity.isBaby())
        {
            player.displayClientMessage(Component.translatable("item.animal_pen.water_animal_container.error.baby").
                withStyle(ChatFormatting.DARK_RED), true);
            // only living entities that are not babies
            return InteractionResult.FAIL;
        }

        if (!(livingEntity instanceof WaterAnimal animal))
        {
            player.displayClientMessage(Component.translatable("item.animal_pen.water_animal_container.error.not_water_animal").
                withStyle(ChatFormatting.DARK_RED), true);
            // only living entities that are not babies
            return InteractionResult.FAIL;
        }

        if (!this.matchEntity(itemStack, livingEntity))
        {
            player.displayClientMessage(Component.translatable("item.animal_pen.water_animal_container.error.wrong").
                withStyle(ChatFormatting.DARK_RED), true);
            // Different entities cannot be merged.
            return InteractionResult.FAIL;
        }

        CompoundTag itemTag;

        if (!itemStack.has(DataComponents.ENTITY_DATA))
        {
            itemTag = new CompoundTag();
            animal.save(itemTag);
        }
        else
        {
            itemTag = itemStack.get(DataComponents.ENTITY_DATA).copyTag();
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

        itemStack.set(DataComponents.ENTITY_DATA, CustomData.of(itemTag));
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

        if (!(blockEntity instanceof AquariumTileEntity tileEntity))
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
        if (!itemStack.has(DataComponents.ENTITY_DATA))
        {
            // Empty cage.
            return true;
        }

        String entityType = itemStack.get(DataComponents.ENTITY_DATA).copyTag().getString(TAG_ENTITY_ID);

        return ResourceLocation.bySeparator(entityType, ':').equals(entity.getType().arch$registryName());
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
            return Component.translatable(entityId);
        }
    }


    public static final String TAG_ENTITY_ID = "id";

    public static final String TAG_AMOUNT = "animal_count";
}
