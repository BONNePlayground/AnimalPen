package lv.id.bonne.animalpen.advancements.critereon;


import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;


public class AnimalVariantChangeTrigger extends SimpleCriterionTrigger<AnimalVariantChangeTrigger.TriggerInstance>
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
        return new TriggerInstance(player);
    }


    public void trigger(ServerPlayer player)
    {
        this.trigger(player, (instance) -> true);
    }


    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        public TriggerInstance(ContextAwarePredicate player)
        {
            super(ID, player);
        }


        @Override
        @NotNull
        public JsonObject serializeToJson(SerializationContext context)
        {
            return super.serializeToJson(context);
        }


        public static TriggerInstance changeVariant()
        {
            return new TriggerInstance(ContextAwarePredicate.ANY);
        }
    }

    private static final ResourceLocation ID = AnimalPen.resourceOf("change_variant");
}