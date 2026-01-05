package lv.id.bonne.animalpen.interaction.condition;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import lv.id.bonne.animalpen.interaction.value.BoolValue;
import lv.id.bonne.animalpen.interaction.value.IntValue;
import lv.id.bonne.animalpen.interaction.value.StringValue;
import lv.id.bonne.animalpen.interaction.value.Value;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;


/**
 * This class stores conditions entry.
 *
 * @param source - The source tag from item-stack tag.
 * @param operator - The performed operation
 * @param key - The tag key
 * @param value - The value compared against.
 */
public record ConditionEntry(
    String source,
    Operator operator,
    String key,
    Value value)
{
    public boolean matchCondition(CompoundTag tag)
    {
        if (!tag.contains(this.source(), Tag.TAG_COMPOUND))
        {
            return false;
        }

        CompoundTag sourceTag = tag.getCompound(this.source());

        return this.operator.test(sourceTag, this);
    }


    public static ConditionEntry of(String source, Operator operator, String key, int value)
    {
        return new ConditionEntry(source, operator, key, new IntValue(value));
    }


    public static ConditionEntry of(String source, Operator operator, String key, String value)
    {
        return new ConditionEntry(source, operator, key, new StringValue(value));
    }


    public static ConditionEntry of(String source, Operator operator, String key, boolean value)
    {
        return new ConditionEntry(source, operator, key, new BoolValue(value));
    }


    public static final Codec<ConditionEntry> CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("source").forGetter(ConditionEntry::source),
            Operator.CODEC.fieldOf("operator").forGetter(ConditionEntry::operator),
            Codec.STRING.fieldOf("key").forGetter(ConditionEntry::key),
            Value.CODEC.fieldOf("value").forGetter(ConditionEntry::value)
        ).apply(instance, ConditionEntry::new));
}