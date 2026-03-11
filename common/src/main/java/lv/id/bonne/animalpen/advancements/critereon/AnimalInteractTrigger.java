package lv.id.bonne.animalpen.advancements.critereon;


import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


public class AnimalInteractTrigger extends SimpleCriterionTrigger<AnimalInteractTrigger.TriggerInstance>
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
        EntityPredicate.Composite player,
        DeserializationContext context)
    {
        EntityPredicate.Composite entity = EntityPredicate.Composite.fromJson(json, "entity", context);
        ItemPredicate item = ItemPredicate.fromJson(json.get("item"));
        return new TriggerInstance(player, entity, item);
    }


    public void trigger(ServerPlayer player, Entity caughtEntity, ItemStack catchingItem)
    {
        this.trigger(player, (instance) -> instance.matches(player, caughtEntity, catchingItem));
    }


    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        public TriggerInstance(EntityPredicate.Composite player, EntityPredicate.Composite entity, ItemPredicate item)
        {
            super(ID, player);
            this.entity = entity;
            this.item = item;
        }


        public boolean matches(ServerPlayer player, Entity caughtEntity, ItemStack catchingItem)
        {
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
            return json;
        }


        public static TriggerInstance interactAnimal(EntityType<?> entity)
        {
            return new TriggerInstance(
                EntityPredicate.Composite.ANY,
                EntityPredicate.Composite.wrap(EntityPredicate.Builder.entity().of(entity).build()),
                ItemPredicate.ANY
            );
        }


        public static TriggerInstance interactAnimalWithItem(EntityType<?> entity, Item... item)
        {
            return new TriggerInstance(
                EntityPredicate.Composite.ANY,
                EntityPredicate.Composite.wrap(EntityPredicate.Builder.entity().of(entity).build()),
                ItemPredicate.Builder.item().of(item).build()
            );
        }

        public static TriggerInstance interactAnimalWithItem(EntityType<?> entity, TagKey<Item> itemTag)
        {
            return new TriggerInstance(
                EntityPredicate.Composite.ANY,
                EntityPredicate.Composite.wrap(EntityPredicate.Builder.entity().of(entity).build()),
                ItemPredicate.Builder.item().of(itemTag).build()
            );
        }


        private final EntityPredicate.Composite entity;

        private final ItemPredicate item;
    }

    private static final ResourceLocation ID = AnimalPen.resourceOf("animal_interact");
}