package lv.id.bonne.animalpen.interaction.value;


import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;


/**
 * This interface allows to properly create IntValue, BooleanValue or StringValue for different objects for mod.
 */
public interface Value
{
    /**
     * @return instance value as integer
     */
    default int getAsInt()
    {
        throw new IllegalStateException("Cannot convert object '" + this + "' to int");
    }


    /**
     * @return instance value as boolean
     */
    default boolean getAsBoolean()
    {
        throw new IllegalStateException("Cannot convert object '" + this + "' to bool");
    }


    /**
     * @return instance value as string
     */
    default String getAsString()
    {
        throw new IllegalStateException("Cannot convert object '" + this + "' to string");
    }


    Codec<Value> CODEC =
        Codec.either(IntValue.CODEC, Codec.either(BoolValue.CODEC, StringValue.CODEC)).
            xmap(either -> either.map(v -> v,
                    e2 -> e2.map(v -> v, v -> (Value) v)),
                value ->
                {
                    if (value instanceof IntValue i)
                    {
                        return Either.left(i);
                    }

                    if (value instanceof BoolValue b)
                    {
                        return Either.right(Either.left(b));
                    }

                    if (value instanceof StringValue s)
                    {
                        return Either.right(Either.right(s));
                    }

                    throw new IllegalStateException("Unknown Value: " + value);
                }
            );
}
