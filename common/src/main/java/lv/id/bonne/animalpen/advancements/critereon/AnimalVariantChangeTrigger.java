package lv.id.bonne.animalpen.advancements.critereon;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;

import lv.id.bonne.animalpen.registries.AnimalPenCriteriaTriggersRegistry;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;


public class AnimalVariantChangeTrigger extends SimpleCriterionTrigger<AnimalVariantChangeTrigger.TriggerInstance>
{
    @Override
    @NotNull
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }


    public void trigger(ServerPlayer player)
    {
        this.trigger(player, (instance) -> true);
    }


    public record TriggerInstance(Optional<Holder<LootItemCondition>> player) implements SimpleCriterionTrigger.SimpleInstance
    {
        private static final Codec<Holder<LootItemCondition>> CONDITION_CODEC =
            RegistryCodecs.holder(Registries.PREDICATE, LootItemCondition.DIRECT_CODEC);

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance ->
                instance.group(CONDITION_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player)).
                    apply(instance, TriggerInstance::new));


        public static Criterion<TriggerInstance> changeVariant()
        {
            return AnimalPenCriteriaTriggersRegistry.ANIMAL_VARIANT_CHANGE_TRIGGER.get().createCriterion(
                new TriggerInstance(Optional.empty()));
        }
    }
}