package lv.id.bonne.animalpen.interaction.function;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

import lv.id.bonne.animalpen.interaction.value.BoolValue;
import lv.id.bonne.animalpen.interaction.value.IntValue;
import lv.id.bonne.animalpen.interaction.value.StringValue;
import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.processing.function.wrapper.EntityFunctionEntry;


/**
 * This record stores related information for function to be called.
 *
 * @param id - the id value of function
 * @param key - the NBT key for value change.
 * @param value - the related value for function.
 */
public record FunctionKey(@NotNull EntityFunctionEntry id, @Nullable String key, @Nullable Value value)
{
    public static FunctionKey of(EntityFunctionEntry id)
    {
        return new FunctionKey(id, null, null);
    }


    public static FunctionKey of(EntityFunctionEntry id, String key)
    {
        return new FunctionKey(id, key, null);
    }


    public static FunctionKey of(EntityFunctionEntry id, String key, int value)
    {
        return new FunctionKey(id, key, new IntValue(value));
    }


    public static FunctionKey of(EntityFunctionEntry id, String key, boolean value)
    {
        return new FunctionKey(id, key, new BoolValue(value));
    }


    public static FunctionKey of(EntityFunctionEntry id, String key, String value)
    {
        return new FunctionKey(id, key, new StringValue(value));
    }


    public static final Codec<FunctionKey> CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            EntityFunctionEntry.CODEC.fieldOf("id").forGetter(FunctionKey::id),
            Codec.STRING.optionalFieldOf("key").forGetter(f -> Optional.ofNullable(f.key)),
            Value.CODEC.optionalFieldOf("value").forGetter(f -> Optional.ofNullable(f.value))
        ).apply(instance, (id, key, value) -> new FunctionKey(id, key.orElse(null), value.orElse(null))));
}