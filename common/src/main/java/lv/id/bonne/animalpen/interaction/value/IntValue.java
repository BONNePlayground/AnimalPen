package lv.id.bonne.animalpen.interaction.value;


import com.mojang.serialization.Codec;


public record IntValue(int value) implements Value
{
    @Override
    public int getAsInt()
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


    public static final Codec<IntValue> CODEC =
        Codec.INT.xmap(IntValue::new, IntValue::value);
}