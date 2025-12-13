package lv.id.bonne.animalpen.items;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Optional;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AquariumTileEntity;
import lv.id.bonne.animalpen.mixin.MobAccessor;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
    public void verifyTagAfterLoad(CompoundTag compoundTag)
    {
        super.verifyTagAfterLoad(compoundTag);

        if (compoundTag.contains(TAG_ENTITY_ID))
        {
            Tag variants = compoundTag.get(TAG_VARIANTS);
            long count = compoundTag.getLong(TAG_AMOUNT);
            CompoundTag animalData = new CompoundTag();
            animalData.putLong(TAG_AMOUNT, count);

            compoundTag.remove(TAG_VARIANTS);
            compoundTag.remove(TAG_AMOUNT);

            CompoundTag animal = compoundTag.copy();

            animal.getAllKeys().forEach(key ->
            {
                if (key.endsWith("_cooldown"))
                {
                    animalData.putLong(key, compoundTag.getLong(key));
                }

                compoundTag.remove(key);
            });

            compoundTag.put(TAG_VARIANTS, variants);
            compoundTag.put(TAG_ANIMAL, animal);
            compoundTag.put(TAG_ANIMAL_DATA, animalData);
        }
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

        if (itemStack.hasTag())
        {
            CompoundTag animal = itemStack.getTag().getCompound(TAG_ANIMAL);
            CompoundTag animalData = itemStack.getTag().getCompound(TAG_ANIMAL_DATA);

            if (animal.contains(TAG_ENTITY_ID))
            {
                list.add(new TranslatableComponent("item.animal_pen.water_animal_container.entity",
                    AnimalContainerItem.getEntityTranslationName(animal.getString(TAG_ENTITY_ID))).
                    withStyle(ChatFormatting.GRAY));
            }

            if (animalData.contains(TAG_AMOUNT))
            {
                list.add(new TranslatableComponent("item.animal_pen.water_animal_container.amount",
                    animalData.getLong(TAG_AMOUNT)).
                    withStyle(ChatFormatting.GRAY));
            }

            if (itemStack.getTag().contains(TAG_VARIANTS))
            {
                list.add(new TranslatableComponent("item.animal_pen.water_animal_container.variants",
                    itemStack.getTag().getList(TAG_VARIANTS, Tag.TAG_COMPOUND).size()).
                    withStyle(ChatFormatting.GRAY));
            }

            if (animal.contains(TAG_ENTITY_ID))
            {
                list.add(TextComponent.EMPTY);
                list.add(new TranslatableComponent("item.animal_pen.water_animal_container.release").
                    withStyle(ChatFormatting.GRAY));
            }
        }

        if (!itemStack.hasTag() ||
            !itemStack.getTag().getCompound(TAG_ANIMAL).contains(TAG_ENTITY_ID))
        {
            list.add(new TranslatableComponent("item.animal_pen.water_animal_container.tip").
                withStyle(ChatFormatting.GRAY));
        }
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
            player.displayClientMessage(new TranslatableComponent("item.animal_pen.water_animal_container.error.unknown").
                withStyle(ChatFormatting.DARK_RED), true);
            return interactionResult;
        }

        if (player.getLevel().isClientSide() || livingEntity instanceof Player || !livingEntity.isAlive())
        {
            // Only  server side
            return InteractionResult.FAIL;
        }

        if (AnimalPen.config().isBlocked(livingEntity.getType()))
        {
            player.displayClientMessage(new TranslatableComponent("item.animal_pen.animal_cage.error.blocked").
                withStyle(ChatFormatting.DARK_RED), true);
            // only living entities that are not babies
            return InteractionResult.FAIL;
        }

        if (!livingEntity.isAlive() || livingEntity.isBaby())
        {
            player.displayClientMessage(new TranslatableComponent("item.animal_pen.water_animal_container.error.baby").
                withStyle(ChatFormatting.DARK_RED), true);
            // only living entities that are not babies
            return InteractionResult.FAIL;
        }

        if (!(livingEntity instanceof WaterAnimal animal))
        {
            player.displayClientMessage(new TranslatableComponent("item.animal_pen.water_animal_container.error.not_water_animal").
                withStyle(ChatFormatting.DARK_RED), true);
            // only living entities that are not babies
            return InteractionResult.FAIL;
        }

        if (livingEntity instanceof OwnableEntity ownableEntity && ownableEntity.getOwnerUUID() != null)
        {
            player.displayClientMessage(new TranslatableComponent("item.animal_pen.water_animal_container.error.tame").
                withStyle(ChatFormatting.DARK_RED), true);
            // cannot add into jar tamed animals
            return InteractionResult.FAIL;
        }

        if (animal.isLeashed())
        {
            player.displayClientMessage(new TranslatableComponent("item.animal_pen.water_animal_container.error.leashed").
                withStyle(ChatFormatting.DARK_RED), true);
            // cannot add into jar tamed animals
            return InteractionResult.FAIL;
        }

        if (animal instanceof Saddleable saddleable && saddleable.isSaddled())
        {
            player.displayClientMessage(new TranslatableComponent("item.animal_pen.water_animal_container.error.saddled").
                withStyle(ChatFormatting.DARK_RED), true);
            // cannot add into jar tamed animals
            return InteractionResult.FAIL;
        }

        if (!this.matchEntity(itemStack, livingEntity))
        {
            player.displayClientMessage(new TranslatableComponent("item.animal_pen.water_animal_container.error.wrong").
                withStyle(ChatFormatting.DARK_RED), true);
            // Different entities cannot be merged.
            return InteractionResult.FAIL;
        }

        CompoundTag itemTag = itemStack.getOrCreateTag();

        // Eject all passengers
        animal.ejectPassengers();

        // Drop equipment on entity.
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            if (animal.hasItemInSlot(slot))
            {
                ItemStack itemBySlot = animal.getItemBySlot(slot);

                if (animal.getRandom().nextFloat() < ((MobAccessor) animal).callGetEquipmentDropChance(slot))
                {
                    Block.popResource(animal.getLevel(), animal.blockPosition(), itemBySlot);
                }

                animal.setDropChance(slot, 0);
            }
        }

        if (!itemTag.contains(TAG_ANIMAL))
        {
            CompoundTag animalTag = new CompoundTag();
            animal.save(animalTag);
            itemTag.put(TAG_ANIMAL, animalTag);
        }

        CompoundTag animalData = itemTag.getCompound(TAG_ANIMAL_DATA);
        long maxCount = AnimalPen.config().getMaximalAnimalCount();

        if (maxCount > 0 && animalData.getLong(TAG_AMOUNT) + 1 > maxCount)
        {
            return InteractionResult.FAIL;
        }

        animalData.putLong(TAG_AMOUNT, animalData.getLong(TAG_AMOUNT) + 1);
        itemTag.put(TAG_ANIMAL_DATA, animalData);

        itemStack.setTag(itemTag);

        // Manage variants
        AnimalContainerItem.storeAnimalVariant(itemStack, animal, player);

        player.setItemInHand(interactionHand, itemStack);
        livingEntity.remove(Entity.RemovalReason.DISCARDED);

        if (AnimalPen.config().isIncreaseStatistics())
        {
            player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        }

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
        Player player = useOnContext.getPlayer();

        if (player != null &&
            blockEntity instanceof AquariumTileEntity tileEntity &&
            tileEntity.processContainer(player, useOnContext.getHand()))
        {
            return InteractionResult.SUCCESS;
        }

        if (player != null &&
            player.isCrouching() &&
            useOnContext.getClickedFace() == Direction.UP)
        {
            // Try to release animal.
            ItemStack itemInHand = useOnContext.getItemInHand();
            CompoundTag itemTag = itemInHand.getOrCreateTag().getCompound(TAG_ANIMAL).copy();

            if (!itemTag.contains(TAG_ENTITY_ID))
            {
                // Empty
                return super.useOn(useOnContext);
            }

            ServerLevel level = (ServerLevel) useOnContext.getLevel();

            ListTag pos = new ListTag();
            pos.add(DoubleTag.valueOf(useOnContext.getClickedPos().getX() + 0.5));
            pos.add(DoubleTag.valueOf(useOnContext.getClickedPos().getY() + 1));
            pos.add(DoubleTag.valueOf(useOnContext.getClickedPos().getZ() + 0.5));

            itemTag.put("Pos", pos);
            itemTag.remove("UUID");

            EntityType.create(itemTag, level).
                map(entity -> (WaterAnimal) entity).
                ifPresent(clone ->
                {
                    for (EquipmentSlot slot : EquipmentSlot.values())
                    {
                        // Remove all equipment from spawned entity.
                        clone.setItemSlot(slot, ItemStack.EMPTY);
                    }

                    level.addFreshEntity(clone);

                    CompoundTag inHandTag = itemInHand.getOrCreateTag();
                    CompoundTag animalData = inHandTag.getCompound(TAG_ANIMAL_DATA);

                    long amount = animalData.getLong(TAG_AMOUNT);
                    animalData.putLong(TAG_AMOUNT, amount - 1);

                    if (amount - 1 <= 0)
                    {
                        // Clear item tag.
                        itemInHand.setTag(new CompoundTag());
                    }
                    else
                    {
                        inHandTag.put(TAG_ANIMAL_DATA, animalData);
                        itemInHand.setTag(inHandTag);
                    }

                    player.setItemInHand(useOnContext.getHand(), itemInHand);
                });
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

        if (itemTag == null || !itemTag.getCompound(TAG_ANIMAL).contains(TAG_ENTITY_ID))
        {
            // Empty cage.
            return true;
        }

        String entityType = itemTag.getCompound(TAG_ANIMAL).getString(TAG_ENTITY_ID);

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


    /**
     * This method returns Optional list-tag or animal variants in given item-stack
     * @param itemStack The item stack that need to be checked.
     * @return Optional list of tags for animal variants.
     */
    public static Optional<ListTag> getAnimalVariants(ItemStack itemStack)
    {
        if (itemStack.getOrCreateTag().contains(TAG_VARIANTS))
        {
            return Optional.of(itemStack.getOrCreateTag().getList(TAG_VARIANTS,
                Tag.TAG_COMPOUND));
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
    public static boolean storeAnimalVariant(ItemStack itemStack, LivingEntity animal, @Nullable Player player)
    {
        if (AnimalPen.config().getMaxStoredVariants() <= 0)
        {
            return false;
        }

        CompoundTag itemTag = itemStack.getOrCreateTag();

        if (!itemTag.getCompound(TAG_ANIMAL).contains(TAG_ENTITY_ID))
        {
            return false;
        }

        ListTag variantList = itemTag.getList(TAG_VARIANTS, Tag.TAG_COMPOUND);

        if (variantList.size() + 1 > AnimalPen.config().getMaxStoredVariants())
        {
            if (player != null)
            {
                player.displayClientMessage(
                    new TranslatableComponent("item.animal_pen.water_animal_container.error.too_many_variants").
                        withStyle(ChatFormatting.DARK_RED), true);
            }

            return false;
        }

        CompoundTag variant = new CompoundTag();
        animal.save(variant);
        variantList.add(variant);

        itemTag.put(TAG_VARIANTS, variantList);
        itemStack.setTag(itemTag);

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
        if (AnimalPen.config().getMaxStoredVariants() <= 0)
        {
            return true;
        }

        CompoundTag itemTag = mainItem.getOrCreateTag();
        CompoundTag redundantTag = redundantItem.getOrCreateTag();

        if (!itemTag.getCompound(TAG_ANIMAL).contains(TAG_ENTITY_ID) ||
            !redundantTag.getCompound(TAG_ANIMAL).contains(TAG_ENTITY_ID))
        {
            return false;
        }

        ListTag variantList = itemTag.getList(TAG_VARIANTS, Tag.TAG_COMPOUND);
        ListTag redundantList = redundantTag.getList(TAG_VARIANTS, Tag.TAG_COMPOUND);

        if (variantList.size() + redundantList.size() > AnimalPen.config().getMaxStoredVariants())
        {
            if (player != null)
            {
                player.displayClientMessage(
                    new TranslatableComponent("item.animal_pen.water_animal_container.error.too_many_variants").
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
        if (AnimalPen.config().getMaxStoredVariants() <= 0)
        {
            return;
        }

        CompoundTag itemTag = mainItem.getOrCreateTag();
        CompoundTag redundantTag = redundantItem.getOrCreateTag();

        if (!itemTag.getCompound(TAG_ANIMAL).contains(TAG_ENTITY_ID) ||
            !redundantTag.getCompound(TAG_ANIMAL).contains(TAG_ENTITY_ID))
        {
            return;
        }

        ListTag variantList = itemTag.getList(TAG_VARIANTS, Tag.TAG_COMPOUND);
        ListTag redundantList = redundantTag.getList(TAG_VARIANTS, Tag.TAG_COMPOUND);

        for (Tag tag : redundantList)
        {
            if (variantList.size() + 1 > AnimalPen.config().getMaxStoredVariants())
            {
                if (player != null)
                {
                    player.displayClientMessage(
                        new TranslatableComponent("item.animal_pen.water_animal_container.error.too_many_variants").
                            withStyle(ChatFormatting.DARK_RED), true);
                }

                break;
            }

            variantList.add(tag);
        }

        itemTag.put(TAG_VARIANTS, variantList);
        mainItem.setTag(itemTag);
    }


    public static long getAnimalCount(ItemStack itemStack)
    {
        if (!itemStack.is(AnimalPensItemRegistry.ANIMAL_CONTAINER.get()))
        {
            // not animal cage
            return 0L;
        }

        if (!itemStack.hasTag() || !itemStack.getTag().contains(TAG_ANIMAL_DATA, Tag.TAG_COMPOUND))
        {
            return 0L;
        }

        return itemStack.getTagElement(TAG_ANIMAL_DATA).getLong(TAG_AMOUNT);
    }


    public static boolean setAnimalCount(ItemStack itemStack, long change)
    {
        if (!itemStack.is(AnimalPensItemRegistry.ANIMAL_CONTAINER.get()))
        {
            // not animal cage
            return false;
        }

        if (!itemStack.hasTag() || !itemStack.getTag().contains(TAG_ANIMAL_DATA, Tag.TAG_COMPOUND))
        {
            return false;
        }

        long animalCount = itemStack.getTagElement(TAG_ANIMAL_DATA).getLong(TAG_AMOUNT);

        if (change < 0 && animalCount + change < 0)
        {
            return false;
        }

        long maxCount = AnimalPen.config().getMaximalAnimalCount();

        if (maxCount > 0 && animalCount + change > maxCount)
        {
            return false;
        }

        itemStack.getTagElement(TAG_ANIMAL_DATA).putLong(TAG_AMOUNT, animalCount + change);

        return true;
    }


    public static final String TAG_ENTITY_ID = "id";

    public static final String TAG_VARIANTS = "animal_variants";

    public static final String TAG_AMOUNT = "animal_count";

    public static final String TAG_ANIMAL = "animal";

    public static final String TAG_ANIMAL_DATA = "animal_data";
}
