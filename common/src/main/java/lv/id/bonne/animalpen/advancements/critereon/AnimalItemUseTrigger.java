package lv.id.bonne.animalpen.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.Optional;


public class AnimalItemUseTrigger extends SimpleCriterionTrigger<AnimalItemUseTrigger.TriggerInstance>
{
    @Override
    @NotNull
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }


    public void trigger(ServerPlayer player, Entity caughtEntity, ItemStack catchingItem, boolean release)
    {
        this.trigger(player, (instance) -> instance.matches(player, caughtEntity, catchingItem, release));
    }


    public record TriggerInstance(Optional<Holder<LootItemCondition>> player,
                                  Optional<Holder<LootItemCondition>> entity,
                                  Optional<ItemPredicate> item,
                                  boolean release) implements SimpleCriterionTrigger.SimpleInstance
    {
        private static final Codec<Holder<LootItemCondition>> CONDITION_CODEC =
            RegistryCodecs.holder(Registries.PREDICATE, LootItemCondition.DIRECT_CODEC);

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance ->
                instance.group(
                        CONDITION_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        CONDITION_CODEC.optionalFieldOf("entity").forGetter(TriggerInstance::entity),
                        ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item),
                        Codec.BOOL.fieldOf("release").forGetter(TriggerInstance::release)
                    ).
                    apply(instance, TriggerInstance::new));


        public boolean matches(ServerPlayer player, Entity caughtEntity, ItemStack catchingItem, boolean release)
        {
            if (this.release != release)
            {
                return false;
            }

            if (this.entity.isPresent() &&
                !this.entity.get().value().test(EntityPredicate.createContext(player, caughtEntity)))
            {
                return false;
            }

            return this.item.isEmpty() || this.item.get().test(catchingItem);
        }


        private static Holder<LootItemCondition> wrap(EntityPredicate.Builder builder)
        {
            return Holder.direct(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, builder).build());
        }


        public static Criterion<TriggerInstance> releaseAnimal()
        {
            return AnimalPenCriteriaTriggersRegistry.ANIMAL_ITEM_USE_TRIGGER.get().createCriterion(
                new TriggerInstance(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    true
                ));
        }


        public static Criterion<TriggerInstance> caughtAnimalWithItem(HolderLookup.Provider provider, EntityType<?> entityType, Item item)
        {
            return AnimalPenCriteriaTriggersRegistry.ANIMAL_ITEM_USE_TRIGGER.get().createCriterion(
                new TriggerInstance(
                    Optional.empty(),
                    Optional.of(wrap(EntityPredicate.Builder.entity().of(provider.lookupOrThrow(Registries.ENTITY_TYPE), entityType))),
                    Optional.of(ItemPredicate.Builder.item().of(provider.lookupOrThrow(Registries.ITEM), item).build()),
                    false
                ));
        }


        public static Criterion<TriggerInstance> caughtWithItem(HolderLookup.Provider provider, Item item)
        {
            return AnimalPenCriteriaTriggersRegistry.ANIMAL_ITEM_USE_TRIGGER.get().createCriterion(
                new TriggerInstance(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(ItemPredicate.Builder.item().of(provider.lookupOrThrow(Registries.ITEM), item).build()),
                    false
                ));
        }
    }
}