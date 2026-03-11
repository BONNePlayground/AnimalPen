package lv.id.bonne.animalpen.advancements.critereon;


import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import lv.id.bonne.animalpen.registries.AnimalPenCriteriaTriggersRegistry;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;


public class AnimalVariantChangeTrigger extends SimpleCriterionTrigger<AnimalVariantChangeTrigger.TriggerInstance>
{
    @Override
    @NotNull
    protected TriggerInstance createInstance(JsonObject json,
        Optional<ContextAwarePredicate> player,
        DeserializationContext context)
    {
        return new TriggerInstance(player);
    }


    public void trigger(ServerPlayer player)
    {
        this.trigger(player, (instance) -> true);
    }


    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        public TriggerInstance(Optional<ContextAwarePredicate> player)
        {
            super(player);
        }


        @Override
        @NotNull
        public JsonObject serializeToJson()
        {
            return super.serializeToJson();
        }


        public static Criterion<TriggerInstance> changeVariant()
        {
            return AnimalPenCriteriaTriggersRegistry.ANIMAL_VARIANT_CHANGE_TRIGGER.createCriterion(
                new TriggerInstance(Optional.empty()));
        }
    }
}