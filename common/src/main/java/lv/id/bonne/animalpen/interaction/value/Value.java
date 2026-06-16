package lv.id.bonne.animalpen.interaction.value;


import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;


/**
 * This interface allows to properly create IntValue, BooleanValue or StringValue for different objects for mod.
 */
public interface Value
{
    /**
     * @return instance value as integer
     */
    default int getAsInt() throws IllegalStateException, NumberFormatException
    {
        throw new IllegalStateException("Cannot convert object '" + this + "' to int");
    }


    /**
     * @return instance value as long
     */
    default long getAsLong() throws IllegalStateException, NumberFormatException
    {
        return this.getAsInt();
    }


    /**
     * @return instance value as boolean
     */
    default boolean getAsBoolean() throws IllegalStateException
    {
        throw new IllegalStateException("Cannot convert object '" + this + "' to bool");
    }


    /**
     * @return instance value as string
     */
    default String getAsString() throws IllegalStateException
    {
        throw new IllegalStateException("Cannot convert object '" + this + "' to string");
    }


    Codec<Value> CODEC =
        Codec.either(LongValue.CODEC, Codec.either(IntValue.CODEC, Codec.either(BoolValue.CODEC, StringValue.CODEC))).
            flatXmap(
            either -> DataResult.success(either.map(v -> v,
                e1 -> e1.map(v -> v,
                    e2 -> e2.map(v -> v, v -> (Value) v)))),
            value ->
            {
                if (value instanceof LongValue l)
                {
                    return DataResult.success(Either.left(l));
                }

                if (value instanceof IntValue i)
                {
                    return DataResult.success(Either.right(Either.left(i)));
                }

                if (value instanceof BoolValue b)
                {
                    return DataResult.success(Either.right(Either.right(Either.left(b))));
                }

                if (value instanceof StringValue s)
                {
                    return DataResult.success(Either.right(Either.right(Either.right(s))));
                }

                return DataResult.error("Unknown Value type: " + value);
            }
        );
}
