package lv.id.bonne.animalpen.interaction.value;


import com.mojang.serialization.Codec;


public record LongValue(long value) implements Value
{
    @Override
    public int getAsInt()
    {
        return (int) this.value();
    }


    @Override
    public long getAsLong()
    {
        return this.value();
    }


    @Override
    public boolean getAsBoolean()
    {
        return this.value() != 0;
    }


    @Override
    public String getAsString()
    {
        return String.valueOf(this.value());
    }


    public static final Codec<LongValue> CODEC =
        Codec.LONG.xmap(LongValue::new, LongValue::value);
}