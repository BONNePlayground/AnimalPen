package lv.id.bonne.animalpen.advancements.critereon;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.NotNull;
import java.util.Optional;

import lv.id.bonne.animalpen.registries.AnimalPenCriteriaTriggersRegistry;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;


public class AnimalInteractTrigger extends SimpleCriterionTrigger<AnimalInteractTrigger.TriggerInstance>
{
    @Override
    @NotNull
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }


    public void trigger(ServerPlayer player, Entity entity, ItemStack stack)
    {
        LootContext ctx = EntityPredicate.createContext(player, entity);
        this.trigger(player, instance -> instance.matches(ctx, stack));
    }


    public record TriggerInstance(Optional<ContextAwarePredicate> player,
                                  Optional<ContextAwarePredicate> entity,
                                  Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance ->
                instance.group(
                    ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").
                        forGetter(TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "entity").
                            forGetter(TriggerInstance::entity),
                    ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item").
                        forGetter(TriggerInstance::item)
                    ).
                    apply(instance, TriggerInstance::new));


        public boolean matches(LootContext entityContext, ItemStack catchingItem)
        {
            if (this.entity.isPresent() && !this.entity.get().matches(entityContext))
            {
                return false;
            }

            return this.item.isEmpty() || this.item.get().matches(catchingItem);
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
    }
}