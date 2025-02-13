//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.implementations;


import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.*;
import java.util.List;
import java.util.Map;

import dev.architectury.registry.registries.Registries;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.mixin.accessors.MobInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


@Mixin(Axolotl.class)
public abstract class AnimalPenAxolotl extends AnimalPenAnimal
{
    @Intrinsic(displace = false)
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);
        tag.putInt("stored_food", this.storedFood);
    }


    @Intrinsic(displace = false)
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        if (tag.contains("stored_food", Tag.TAG_INT))
        {
            this.storedFood = tag.getInt("stored_food");
        }
    }


    @Intrinsic(displace = false)
    @Override
    public boolean animalPen$animalPenInteract(Player player, InteractionHand hand, BlockPos position)
    {
        ItemStack itemStack = player.getItemInHand(hand);

        if (this.isFood(itemStack))
        {
            if (player.getLevel().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            this.storedFood++;

            if (!player.getAbilities().instabuild)
            {
                player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
            }

            if (this.foodCooldown > 0 || this.storedFood < 2)
            {
                return false;
            }

            long maxCount = AnimalPen.CONFIG_MANAGER.getConfiguration().getMaximalAnimalCount();

            if (maxCount > 0 && this.animalCount >= maxCount)
            {
                return false;
            }

            int stackSize = (int) Math.min(this.animalCount, this.storedFood);

            if (stackSize < 2)
            {
                // Cannot feed 1 animal only for breeding.
                return false;
            }

            stackSize = (int) Math.min((maxCount - this.animalCount) * 2, stackSize);

            this.animalCount += stackSize / 2;
            this.storedFood -= stackSize;

            if (player.getLevel() instanceof ServerLevel level)
            {
                level.sendParticles(
                    ParticleTypes.HEART,
                    position.getX() + 0.5f,
                    position.getY() + 1.5,
                    position.getZ() + 0.5f,
                    5,
                    0.2, 0.2, 0.2,
                    0.05);
            }

            player.getLevel().playSound(null,
                position,
                ((Animal) (Object) this).getEatingSound(itemStack),
                SoundSource.NEUTRAL,
                1.0F,
                Mth.randomBetween(player.getLevel().random, 0.8F, 1.2F));

            SoundEvent soundEvent = ((MobInvoker) this).invokeGetAmbientSound();

            if (soundEvent != null)
            {
                player.getLevel().playSound(null,
                    position,
                    soundEvent,
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F);
            }

            this.foodCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                ((Animal) (Object) this).getType(),
                Items.APPLE,
                this.animalCount);

            return true;
        }

        return false;
    }


    @Intrinsic(displace = false)
    @Override
    public List<Pair<ItemStack, Component>> animalPen$animalPenGetLines(int tick)
    {
        List<Pair<ItemStack, Component>> lines = super.animalPen$animalPenGetLines(tick);

        if (AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
            ((Animal) (Object) this).getType(),
            Items.APPLE,
            this.animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component =
            new TranslatableComponent("display.animal_pen.stored_food", this.storedFood);

        List<ItemStack> food = this.animalPen$getFood();
        ItemStack foodItem;

        if (food.isEmpty())
        {
            // No food item for this entity.
            return lines;
        }
        else if (food.size() == 1)
        {
            foodItem = food.get(0);
        }
        else
        {
            int size = food.size();
            int index = (tick / 100) % size;

            foodItem = food.get(index);
        }

        lines.add(Pair.of(foodItem, component));

        return lines;
    }


    @Intrinsic(displace = false)
    @Override
    public List<ItemStack> animalPen$getFood()
    {
        if (ANIMAL_PEN$FOOD_LIST == null)
        {
            ANIMAL_PEN$FOOD_LIST = Registries.get(AnimalPen.MOD_ID).
                get(Registry.ITEM_REGISTRY).entrySet().stream().
                map(Map.Entry::getValue).
                map(Item::getDefaultInstance).
                filter(stack -> stack.is(ItemTags.AXOLOTL_TEMPT_ITEMS)).
                toList();
        }

        return ANIMAL_PEN$FOOD_LIST;
    }


    @Unique
    private int storedFood = 0;

    @Unique
    private static List<ItemStack> ANIMAL_PEN$FOOD_LIST = null;
}
