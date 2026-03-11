package lv.id.bonne.animalpen.advancements.critereon;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;

import lv.id.bonne.animalpen.registries.AnimalPenCriteriaTriggersRegistry;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;


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


    public record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance ->
                instance.group(
                    ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").
                        forGetter(TriggerInstance::player)
                    ).
                    apply(instance, TriggerInstance::new));


        public static Criterion<TriggerInstance> changeVariant()
        {
            return AnimalPenCriteriaTriggersRegistry.ANIMAL_VARIANT_CHANGE_TRIGGER.createCriterion(
                new TriggerInstance(Optional.empty()));
        }
    }
}