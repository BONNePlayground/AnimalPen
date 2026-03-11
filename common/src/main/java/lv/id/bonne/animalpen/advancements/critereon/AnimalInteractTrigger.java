package lv.id.bonne.animalpen.advancements.critereon;


import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import lv.id.bonne.animalpen.registries.AnimalPenCriteriaTriggersRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;


public class AnimalInteractTrigger extends SimpleCriterionTrigger<AnimalInteractTrigger.TriggerInstance>
{
    @Override
    @NotNull
    protected TriggerInstance createInstance(JsonObject json,
        Optional<ContextAwarePredicate> player,
        DeserializationContext context)
    {
        Optional<ContextAwarePredicate> entity = EntityPredicate.fromJson(json, "entity", context);
        Optional<ItemPredicate> item = ItemPredicate.fromJson(json.get("item"));
        return new TriggerInstance(player, entity, item);
    }


    public void trigger(ServerPlayer player, Entity entity, ItemStack stack)
    {
        LootContext ctx = EntityPredicate.createContext(player, entity);

        this.trigger(player, instance -> instance.matches(ctx, stack));
    }


    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        public TriggerInstance(Optional<ContextAwarePredicate> player,
            Optional<ContextAwarePredicate> entity,
            Optional<ItemPredicate> item)
        {
            super(player);
            this.entity = entity;
            this.item = item;
        }

        public boolean matches(LootContext entityContext, ItemStack catchingItem)
        {
            if (this.entity.isPresent() && !this.entity.get().matches(entityContext))
            {
                return false;
            }

            return this.item.isEmpty() || this.item.get().matches(catchingItem);
        }


        @Override
        @NotNull
        public JsonObject serializeToJson()
        {
            JsonObject json = super.serializeToJson();
            this.entity.ifPresent(entity -> json.add("entity", entity.toJson()));
            this.item.ifPresent(item -> json.add("item", item.serializeToJson()));
            return json;
        }


        public static Criterion<TriggerInstance> interactAnimal(EntityType<?> entity)
        {
            return AnimalPenCriteriaTriggersRegistry.ANIMAL_INTERACT_TRIGGER.createCriterion(new TriggerInstance(
                Optional.empty(),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entity).build())),
                Optional.empty()
            ));
        }


        public static Criterion<TriggerInstance> interactAnimalWithItem(EntityType<?> entity, Item... item)
        {
            return AnimalPenCriteriaTriggersRegistry.ANIMAL_INTERACT_TRIGGER.createCriterion(new TriggerInstance(
                Optional.empty(),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entity).build())),
                Optional.of(ItemPredicate.Builder.item().of(item).build())
            ));
        }


        public static Criterion<TriggerInstance> interactAnimalWithItem(EntityType<?> entity, TagKey<Item> itemTag)
        {
            return AnimalPenCriteriaTriggersRegistry.ANIMAL_INTERACT_TRIGGER.createCriterion(new TriggerInstance(
                Optional.empty(),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entity).build())),
                Optional.of(ItemPredicate.Builder.item().of(itemTag).build())
            ));
        }


        private final Optional<ContextAwarePredicate> entity;

        private final Optional<ItemPredicate> item;
    }
}