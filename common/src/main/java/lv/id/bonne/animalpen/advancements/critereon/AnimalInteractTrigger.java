package lv.id.bonne.animalpen.advancements.critereon;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.NotNull;
import java.util.Optional;

import lv.id.bonne.animalpen.registries.AnimalPenCriteriaTriggersRegistry;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;


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


    public record TriggerInstance(Optional<Holder<LootItemCondition>> player,
                                  Optional<Holder<LootItemCondition>> entity,
                                  Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance
    {
        private static final Codec<Holder<LootItemCondition>> CONDITION_CODEC =
            RegistryCodecs.holder(Registries.PREDICATE, LootItemCondition.DIRECT_CODEC);

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance ->
                instance.group(
                        CONDITION_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        CONDITION_CODEC.optionalFieldOf("entity").forGetter(TriggerInstance::entity),
                        ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item)
                    ).
                    apply(instance, TriggerInstance::new));


        public boolean matches(LootContext entityContext, ItemStack catchingItem)
        {
            if (this.entity.isPresent() && !this.entity.get().value().test(entityContext))
            {
                return false;
            }

            return this.item.isEmpty() || this.item.get().test(catchingItem);
        }


        private static Holder<LootItemCondition> wrap(EntityPredicate.Builder builder)
        {
            return Holder.direct(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, builder).build());
        }


        public static Criterion<TriggerInstance> interactAnimal(HolderLookup.Provider provider, EntityType<?> entity)
        {
            return AnimalPenCriteriaTriggersRegistry.ANIMAL_INTERACT_TRIGGER.get().createCriterion(new TriggerInstance(
                Optional.empty(),
                Optional.of(wrap(EntityPredicate.Builder.entity().of(provider.lookupOrThrow(Registries.ENTITY_TYPE), entity))),
                Optional.empty()
            ));
        }


        public static Criterion<TriggerInstance> interactAnimalWithItem(HolderLookup.Provider provider, EntityType<?> entity, Item... item)
        {
            return AnimalPenCriteriaTriggersRegistry.ANIMAL_INTERACT_TRIGGER.get().createCriterion(new TriggerInstance(
                Optional.empty(),
                Optional.of(wrap(EntityPredicate.Builder.entity().of(provider.lookupOrThrow(Registries.ENTITY_TYPE), entity))),
                Optional.of(ItemPredicate.Builder.item().of(provider.lookupOrThrow(Registries.ITEM), item).build())
            ));
        }


        public static Criterion<TriggerInstance> interactAnimalWithItem(HolderLookup.Provider provider, EntityType<?> entity, TagKey<Item> itemTag)
        {
            return AnimalPenCriteriaTriggersRegistry.ANIMAL_INTERACT_TRIGGER.get().createCriterion(new TriggerInstance(
                Optional.empty(),
                Optional.of(wrap(EntityPredicate.Builder.entity().of(provider.lookupOrThrow(Registries.ENTITY_TYPE), entity))),
                Optional.of(ItemPredicate.Builder.item().of(provider.lookupOrThrow(Registries.ITEM), itemTag).build())
            ));
        }
    }
}