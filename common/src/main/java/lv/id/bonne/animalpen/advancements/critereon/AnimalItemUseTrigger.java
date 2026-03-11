package lv.id.bonne.animalpen.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpen.registries.AnimalPenCriteriaTriggersRegistry;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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


    public record TriggerInstance(Optional<ContextAwarePredicate> player,
                                  Optional<ContextAwarePredicate> entity,
                                  Optional<ItemPredicate> item,
                                  boolean release) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance ->
                instance.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").
                            forGetter(TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "entity").
                            forGetter(TriggerInstance::entity),
                        ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item").
                            forGetter(TriggerInstance::item),
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
                !this.entity.get().matches(EntityPredicate.createContext(player, caughtEntity)))
            {
                return false;
            }

            return this.item.isEmpty() || this.item.get().matches(catchingItem);
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
    }
}