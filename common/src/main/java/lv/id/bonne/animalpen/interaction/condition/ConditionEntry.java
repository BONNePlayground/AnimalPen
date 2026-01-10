package lv.id.bonne.animalpen.interaction.condition;


import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import lv.id.bonne.animalpen.interaction.value.BoolValue;
import lv.id.bonne.animalpen.interaction.value.IntValue;
import lv.id.bonne.animalpen.interaction.value.TagValue;
import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;


/**
 * This interface allows to define conditions that need to be matched for interaction to operate.
 */
public interface ConditionEntry
{
    /**
     * This method checks if given tag fulfills required conditions.
     * @param tag The tag that contains all data.
     * @return {@code true} if condition is matched, {@code false} otherwise.
     */
    boolean matchCondition(CompoundTag tag);


    /**
     * This condition uses `animal` to get `key` and apply `operator` with given `value`
     */
    public record MobCondition(String key, Operator operator, Value value) implements ConditionEntry
    {
        @Override
        public boolean matchCondition(CompoundTag tag)
        {
            if (!tag.contains(AnimalPenCompoundTags.TAG_ANIMAL, Tag.TAG_COMPOUND))
            {
                return false;
            }

            // Operator has checks if key exists. Need to use contains.
            Value dataValue = this.operator == Operator.HAS ?
                new BoolValue(tag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL).contains(this.key)) :
                new TagValue(tag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL).get(this.key));

            return operator.test(dataValue, this.value);
        }


        public static final Codec<MobCondition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("key").forGetter(MobCondition::key),
            Operator.CODEC.fieldOf("operator").forGetter(MobCondition::operator),
            Value.CODEC.fieldOf("value").forGetter(MobCondition::value)
        ).apply(inst, MobCondition::new));
    }


    /**
     * This condition uses `animal_count` to apply `operator` with given `value`
     */
    public record AmountCondition(Operator operator, int value) implements ConditionEntry
    {
        @Override
        public boolean matchCondition(CompoundTag tag)
        {
            if (!tag.contains(AnimalPenCompoundTags.TAG_ANIMAL_DATA, Tag.TAG_COMPOUND))
            {
                return false;
            }

            long animalCount = tag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA).
                getLong(AnimalPenCompoundTags.TAG_AMOUNT);

            return operator.test(new IntValue((int) animalCount), new IntValue(this.value));
        }


        public static final Codec<AmountCondition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Operator.CODEC.fieldOf("operator").forGetter(AmountCondition::operator),
            Codec.INT.fieldOf("value").forGetter(AmountCondition::value)
        ).apply(inst, AmountCondition::new));
    }


    /**
     * This condition uses `animal_data` to get `key` and apply `operator` with given `value`
     */
    public record PropertiesCondition(String key, Operator operator, Value value) implements ConditionEntry
    {
        @Override
        public boolean matchCondition(CompoundTag tag)
        {
            if (!tag.contains(AnimalPenCompoundTags.TAG_ANIMAL_DATA, Tag.TAG_COMPOUND))
            {
                return false;
            }

            Value dataValue = new TagValue(tag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA).get(this.key));

            return operator.test(dataValue, this.value);
        }


        public static final Codec<PropertiesCondition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("key").forGetter(PropertiesCondition::key),
            Operator.CODEC.fieldOf("operator").forGetter(PropertiesCondition::operator),
            Value.CODEC.fieldOf("value").forGetter(PropertiesCondition::value)
        ).apply(inst, PropertiesCondition::new));
    }


    public static final Codec<ConditionEntry> CODEC = Codec.STRING.
        dispatch("type",
            entry ->
            {
                if (entry instanceof AmountCondition)
                {
                    return "amount";
                }
                else if (entry instanceof MobCondition)
                {
                    return "mob";
                }
                else if (entry instanceof PropertiesCondition)
                {
                    return "data";
                }
                else
                {
                    // This should never happen
                    return entry.toString();
                }
            },
            type -> switch (type)
            {
                case "amount" -> AmountCondition.CODEC;
                case "mob" -> MobCondition.CODEC;
                case "data" -> PropertiesCondition.CODEC;
                default -> Codec.EMPTY.codec().flatXmap(
                    empty -> DataResult.error("Unknown cooldown type: " + type),
                    entry -> DataResult.error("Unknown cooldown type: " + type)
                );
            });
}