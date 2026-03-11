package lv.id.bonne.animalpen.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpen.registries.AnimalPenCriteriaTriggersRegistry;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;


public class AnimalItemUseTrigger extends SimpleCriterionTrigger<AnimalItemUseTrigger.TriggerInstance>
{
    @Override
    @NotNull
    protected TriggerInstance createInstance(JsonObject json,
        Optional<ContextAwarePredicate> player,
        DeserializationContext context)
    {
        Optional<ContextAwarePredicate> entity = EntityPredicate.fromJson(json, "entity", context);
        Optional<ItemPredicate> item = ItemPredicate.fromJson(json.get("item"));
        return new TriggerInstance(player, entity, item, json.get("release").getAsBoolean());
    }


    public void trigger(ServerPlayer player, Entity caughtEntity, ItemStack catchingItem, boolean release)
    {
        this.trigger(player, (instance) -> instance.matches(player, caughtEntity, catchingItem, release));
    }


    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        public TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<ContextAwarePredicate> entity,
            Optional<ItemPredicate> item,
            boolean release)
        {
            super(player);
            this.entity = entity;
            this.item = item;
            this.release = release;
        }


        public boolean matches(ServerPlayer player, Entity caughtEntity, ItemStack catchingItem, boolean release)
        {
            if (this.release != release)
            {
                return false;
            }

            if (this.entity.isPresent() &&
                !this.entity.get().matches(EntityPredicate.createContext(player, caughtEntity)))
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
            this.entity.ifPresent(e -> json.add("entity", e.toJson()));
            this.item.ifPresent(i -> json.add("item", i.serializeToJson()));
            json.add("release", new JsonPrimitive(this.release));
            return json;
        }


        public static Criterion<TriggerInstance> releaseAnimal()
        {
            return AnimalPenCriteriaTriggersRegistry.ANIMAL_ITEM_USE_TRIGGER.createCriterion(
                new TriggerInstance(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    true
                ));
        }


        public static Criterion<TriggerInstance> caughtAnimalWithItem(EntityType<?> entityType, Item item)
        {
            return AnimalPenCriteriaTriggersRegistry.ANIMAL_ITEM_USE_TRIGGER.createCriterion(
                new TriggerInstance(
                    Optional.empty(),
                    Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityType).build())),
                    Optional.of(ItemPredicate.Builder.item().of(item).build()),
                    false
                ));
        }


        public static Criterion<TriggerInstance> caughtWithItem(Item item)
        {
            return AnimalPenCriteriaTriggersRegistry.ANIMAL_ITEM_USE_TRIGGER.createCriterion(
                new TriggerInstance(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(ItemPredicate.Builder.item().of(item).build()),
                    false
                ));
        }


        private final Optional<ContextAwarePredicate> entity;

        private final Optional<ItemPredicate> item;

        private final boolean release;
    }
}