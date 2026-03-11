package lv.id.bonne.animalpen.advancements.critereon;


import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


public class AnimalItemUseTrigger extends SimpleCriterionTrigger<AnimalItemUseTrigger.TriggerInstance>
{
    @Override
    @NotNull
    public ResourceLocation getId()
    {
        return ID;
    }


    @Override
    @NotNull
    protected TriggerInstance createInstance(JsonObject json,
        ContextAwarePredicate player,
        DeserializationContext context)
    {
        ContextAwarePredicate entity = EntityPredicate.fromJson(json, "entity", context);
        ItemPredicate item = ItemPredicate.fromJson(json.get("item"));
        return new TriggerInstance(player, entity, item, json.get("release").getAsBoolean());
    }


    public void trigger(ServerPlayer player, Entity caughtEntity, ItemStack catchingItem, boolean release)
    {
        this.trigger(player, (instance) -> instance.matches(player, caughtEntity, catchingItem, release));
    }


    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        public TriggerInstance(ContextAwarePredicate player, ContextAwarePredicate entity, ItemPredicate item, boolean release)
        {
            super(ID, player);
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

            if (!this.entity.matches(EntityPredicate.createContext(player, caughtEntity)))
            {
                return false;
            }

            return this.item.matches(catchingItem);
        }


        @Override
        @NotNull
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject json = super.serializeToJson(context);
            json.add("entity", this.entity.toJson(context));
            json.add("item", this.item.serializeToJson());
            json.add("release", new JsonPrimitive(this.release));
            return json;
        }


        public static TriggerInstance releaseAnimal()
        {
            return new TriggerInstance(
                ContextAwarePredicate.ANY,
                ContextAwarePredicate.ANY,
                ItemPredicate.ANY,
                true
            );
        }


        public static TriggerInstance caughtAnimalWithItem(EntityType<?> entity, Item item)
        {
            return new TriggerInstance(
                ContextAwarePredicate.ANY,
                EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entity).build()),
                ItemPredicate.Builder.item().of(item).build(),
                false
            );
        }

        public static TriggerInstance caughtWithItem(Item item)
        {
            return new TriggerInstance(
                ContextAwarePredicate.ANY,
                ContextAwarePredicate.ANY,
                ItemPredicate.Builder.item().of(item).build(),
                false
            );
        }

        private final ContextAwarePredicate entity;

        private final ItemPredicate item;

        private final boolean release;
    }

    private static final ResourceLocation ID = AnimalPen.resourceOf("animal_caught");
}