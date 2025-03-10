package lv.id.bonne.animalpen.items;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Optional;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
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

        DataComponentMap dataComponents = itemStack.getComponents();

        if (itemStack.has(DataComponents.ENTITY_DATA))
        {
            list.add(Component.translatable("item.animal_pen.animal_cage.entity",
                AnimalCageItem.getEntityTranslationName(itemStack.get(DataComponents.ENTITY_DATA).copyTag().getString(TAG_ENTITY_ID))).
                withStyle(ChatFormatting.GRAY));
        }

        if (itemStack.has(DataComponents.ENTITY_DATA))
        {
            list.add(Component.translatable("item.animal_pen.animal_cage.amount",
                    itemStack.get(DataComponents.ENTITY_DATA).copyTag().getLong(TAG_AMOUNT)).
                withStyle(ChatFormatting.GRAY));
        }

        if (!itemStack.has(DataComponents.ENTITY_DATA))
        {
            list.add(Component.translatable("item.animal_pen.animal_cage.tip").
                withStyle(ChatFormatting.GRAY));
        }

        list.add(Component.translatable("item.animal_pen.animal_cage.warning").
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
            player.displayClientMessage(Component.translatable("item.animal_pen.animal_cage.error.unknown").
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
            player.displayClientMessage(Component.translatable("item.animal_pen.animal_cage.error.baby").
                withStyle(ChatFormatting.DARK_RED), true);
            // only living entities that are not babies
            return InteractionResult.FAIL;
        }

        if (!(livingEntity instanceof Animal animal))
        {
            player.displayClientMessage(Component.translatable("item.animal_pen.animal_cage.error.not_animal").
                withStyle(ChatFormatting.DARK_RED), true);
            // only living entities that are not babies
            return InteractionResult.FAIL;
        }

        if (livingEntity instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame() ||
            livingEntity instanceof AbstractHorse horse && horse.isTamed())
        {
            player.displayClientMessage(Component.translatable("item.animal_pen.animal_cage.error.tame").
                withStyle(ChatFormatting.DARK_RED), true);
            // cannot add into jar tamed animals
            return InteractionResult.FAIL;
        }

        if (!this.matchEntity(itemStack, livingEntity))
        {
            player.displayClientMessage(Component.translatable("item.animal_pen.animal_cage.error.wrong").
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

        // Manage variants
        AnimalCageItem.storeAnimalVariant(itemStack, animal, player);
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
        if (!itemStack.has(DataComponents.ENTITY_DATA))
        {
            // Empty cage.
            return true;
        }

        String entityType = itemStack.get(DataComponents.ENTITY_DATA).copyTag().getString(TAG_ENTITY_ID);

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
            return Component.translatable(entityId);
        }
    }


    /**
     * This method returns Optional list-tag or animal variants in given item-stack
     * @param itemStack The item stack that need to be checked.
     * @return Optional list of tags for animal variants.
     */
    public static Optional<ListTag> getAnimalVariants(ItemStack itemStack)
    {
        if (!itemStack.has(DataComponents.ENTITY_DATA))
        {
            return Optional.empty();
        }

        CompoundTag tag = itemStack.get(DataComponents.ENTITY_DATA).copyTag();

        if (tag.contains(TAG_VARIANTS))
        {
            return Optional.of(tag.getList(TAG_VARIANTS, Tag.TAG_COMPOUND));
        }
        else
        {
            return Optional.empty();
        }
    }


    /**
     * This method stores given animal as a variant in given item stack.
     * @param itemStack The storage place.
     * @param animal The animal that need to be stored
     * @param player Player that should receive message is it fails to add variant.
     * @return {@code true} if variant was added, {@code false} otherwise
     */
    public static boolean storeAnimalVariant(ItemStack itemStack, Animal animal, @Nullable Player player)
    {
        if (AnimalPen.CONFIG_MANAGER.getConfiguration().getMaxStoredVariants() <= 0)
        {
            return false;
        }

        if (!itemStack.has(DataComponents.ENTITY_DATA))
        {
            return false;
        }

        CompoundTag itemTag = itemStack.get(DataComponents.ENTITY_DATA).copyTag();

        if (!itemTag.contains(TAG_ENTITY_ID))
        {
            return false;
        }

        ListTag variantList = itemTag.getList(TAG_VARIANTS, Tag.TAG_COMPOUND);

        if (variantList.size() + 1 > AnimalPen.CONFIG_MANAGER.getConfiguration().getMaxStoredVariants())
        {
            if (player != null)
            {
                player.displayClientMessage(
                    Component.translatable("item.animal_pen.animal_cage.error.too_many_variants").
                        withStyle(ChatFormatting.DARK_RED), true);
            }

            return false;
        }

        CompoundTag variant = new CompoundTag();
        animal.save(variant);
        variantList.add(variant);

        itemTag.put(TAG_VARIANTS, variantList);
        itemStack.set(DataComponents.ENTITY_DATA, CustomData.of(itemTag));

        return true;
    }


    /**
     * This method returns if animal variants can be merged into main item variants.
     * @param mainItem The item stack that should contain all variants
     * @param redundantItem The item stack that donates their variants
     * @param player A player instance
     * @return {@code true} if all variants can be added, {@code false} otherwise.
     */
    public static boolean canMergeAnimalVariants(ItemStack mainItem, ItemStack redundantItem, @Nullable Player player)
    {
        if (AnimalPen.CONFIG_MANAGER.getConfiguration().getMaxStoredVariants() <= 0)
        {
            return true;
        }

        if (!mainItem.has(DataComponents.ENTITY_DATA) || !redundantItem.has(DataComponents.ENTITY_DATA))
        {
            return false;
        }

        CompoundTag itemTag = mainItem.get(DataComponents.ENTITY_DATA).copyTag();
        CompoundTag redundantTag = redundantItem.get(DataComponents.ENTITY_DATA).copyTag();

        if (!itemTag.contains(TAG_ENTITY_ID) || !redundantTag.contains(TAG_ENTITY_ID))
        {
            return false;
        }

        ListTag variantList = itemTag.getList(TAG_VARIANTS, Tag.TAG_COMPOUND);
        ListTag redundantList = redundantTag.getList(TAG_VARIANTS, Tag.TAG_COMPOUND);

        if (variantList.size() + redundantList.size() > AnimalPen.CONFIG_MANAGER.getConfiguration().getMaxStoredVariants())
        {
            if (player != null)
            {
                player.displayClientMessage(
                    Component.translatable("item.animal_pen.animal_cage.error.too_many_variants").
                        withStyle(ChatFormatting.DARK_RED), true);
            }

            return false;
        }

        return true;
    }


    /**
     * This method merges redundant item entity variants into main item stack.
     * @param mainItem The item stack that should contain all variants
     * @param redundantItem The item stack that donates their variants
     * @param player A player instance
     */
    public static void mergeAnimalVariants(ItemStack mainItem, ItemStack redundantItem, @Nullable Player player)
    {
        if (AnimalPen.CONFIG_MANAGER.getConfiguration().getMaxStoredVariants() <= 0)
        {
            return;
        }

        if (!mainItem.has(DataComponents.ENTITY_DATA) || !redundantItem.has(DataComponents.ENTITY_DATA))
        {
            return;
        }

        CompoundTag itemTag = mainItem.get(DataComponents.ENTITY_DATA).copyTag();
        CompoundTag redundantTag = redundantItem.get(DataComponents.ENTITY_DATA).copyTag();

        if (!itemTag.contains(TAG_ENTITY_ID) || !redundantTag.contains(TAG_ENTITY_ID))
        {
            return;
        }

        ListTag variantList = itemTag.getList(TAG_VARIANTS, Tag.TAG_COMPOUND);
        ListTag redundantList = redundantTag.getList(TAG_VARIANTS, Tag.TAG_COMPOUND);

        for (Tag tag : redundantList)
        {
            if (variantList.size() + 1 > AnimalPen.CONFIG_MANAGER.getConfiguration().getMaxStoredVariants())
            {
                if (player != null)
                {
                    player.displayClientMessage(
                        Component.translatable("item.animal_pen.animal_cage.error.too_many_variants").
                            withStyle(ChatFormatting.DARK_RED), true);
                }

                break;
            }

            variantList.add(tag);
        }

        itemTag.put(TAG_VARIANTS, variantList);
        mainItem.set(DataComponents.ENTITY_DATA, CustomData.of(itemTag));
    }


    public static final String TAG_ENTITY_ID = "id";

    public static final String TAG_VARIANTS = "animal_variants";

    public static final String TAG_AMOUNT = "animal_count";
}
