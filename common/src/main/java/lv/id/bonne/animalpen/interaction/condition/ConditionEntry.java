package lv.id.bonne.animalpen.interaction.condition;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.value.*;
import lv.id.bonne.animalpen.items.component.StoredMob;
import lv.id.bonne.animalpen.items.component.StoredMobData;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.util.StringRepresentable;


/**
 * This interface allows to define conditions that need to be matched for interaction to operate.
 */
public sealed interface ConditionEntry permits
    ConditionEntry.MobCondition,
    ConditionEntry.AmountCondition,
    ConditionEntry.PropertiesCondition
{
    /**
     * This method checks if given tag fulfills required conditions.
     *
     * @param dataHolder The tag that contains all data.
     * @return {@code true} if condition is matched, {@code false} otherwise.
     */
    boolean matchCondition(DataComponentHolder dataHolder);


    /**
     * This condition uses `animal` to get `key` and apply `operator` with given `value`
     */
    public record MobCondition(String key, Operator operator, Value value) implements ConditionEntry
    {
        @Override
        public boolean matchCondition(DataComponentHolder dataHolder)
        {
            if (!dataHolder.has(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()))
            {
                return false;
            }

            StoredMob storedMob = dataHolder.get(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());

            // Operator has checks if key exists. Need to use contains.
            Value dataValue = this.operator == Operator.HAS ?
                new BoolValue(storedMob.tag().contains(this.key)) :
                new TagValue(storedMob.tag().get(this.key));

            return operator.test(dataValue, this.value);
        }


        public static final MapCodec<MobCondition> CODEC =
            RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.STRING.fieldOf("key").forGetter(MobCondition::key),
                Operator.CODEC.fieldOf("operator").forGetter(MobCondition::operator),
                Value.CODEC.fieldOf("value").forGetter(MobCondition::value)
            ).apply(inst, MobCondition::new));
    }


    /**
     * This condition uses `animal_count` to apply `operator` with given `value`
     */
    public record AmountCondition(Operator operator, long value) implements ConditionEntry
    {
        @Override
        public boolean matchCondition(DataComponentHolder dataHolder)
        {
            if (!dataHolder.has(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get()))
            {
                return false;
            }

            StoredMobData storedMobData = dataHolder.get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());

            long maximalAnimalCount = AnimalPen.config().getMaximalAnimalCountNormalized();
            long currentValue = this.value > 0 ? this.value : Long.MAX_VALUE - 1;

            return operator.test(new LongValue(storedMobData.animalCount()),
                new LongValue(Math.min(currentValue, maximalAnimalCount)));
        }


        public static final MapCodec<AmountCondition> CODEC =
            RecordCodecBuilder.mapCodec(inst -> inst.group(
                Operator.CODEC.fieldOf("operator").forGetter(AmountCondition::operator),
                Codec.LONG.fieldOf("value").forGetter(AmountCondition::value)
            ).apply(inst, AmountCondition::new));
    }


    /**
     * This condition uses `animal_data` to get `key` and apply `operator` with given `value`
     */
    public record PropertiesCondition(String key, Operator operator, Value value) implements ConditionEntry
    {
        @Override
        public boolean matchCondition(DataComponentHolder dataHolder)
        {
            if (!dataHolder.has(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get()))
            {
                return false;
            }

            StoredMobData storedMobData = dataHolder.get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());

            return operator.test(new IntValue(storedMobData.properties().getOrDefault(key, 0)), this.value);
        }


        public static final MapCodec<PropertiesCondition> CODEC =
            RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.STRING.fieldOf("key").forGetter(PropertiesCondition::key),
                Operator.CODEC.fieldOf("operator").forGetter(PropertiesCondition::operator),
                Value.CODEC.fieldOf("value").forGetter(PropertiesCondition::value)
            ).apply(inst, PropertiesCondition::new));
    }


    enum ConditionType implements StringRepresentable
    {
        AMOUNT("amount"),
        MOB("mob"),
        PROPERTIES("data");

        ConditionType(String name)
        {
            this.name = name;
        }


        @Override
        @NotNull
        public String getSerializedName()
        {
            return this.name;
        }


        public static final Codec<ConditionEntry.ConditionType> CODEC =
            StringRepresentable.fromEnum(ConditionEntry.ConditionType::values);

        private final String name;
    }


    public static final Codec<ConditionEntry> CODEC =
        ConditionEntry.ConditionType.CODEC.dispatch(
            "type",
            entry -> switch (entry) {
                case ConditionEntry.AmountCondition ignored -> ConditionEntry.ConditionType.AMOUNT;
                case ConditionEntry.MobCondition ignored -> ConditionEntry.ConditionType.MOB;
                case ConditionEntry.PropertiesCondition ignored -> ConditionEntry.ConditionType.PROPERTIES;
            },
            type -> switch (type) {
                case AMOUNT -> ConditionEntry.AmountCondition.CODEC;
                case MOB -> ConditionEntry.MobCondition.CODEC;
                case PROPERTIES -> ConditionEntry.PropertiesCondition.CODEC;
            }
        );
}