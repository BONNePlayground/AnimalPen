package lv.id.bonne.animalpen.interaction.value;


import com.mojang.serialization.Codec;


public record BoolValue(boolean value) implements Value
{
    @Override
    public int getAsInt()
    {
        return this.value() ? 1 : 0;
    }


    @Override
    public boolean getAsBoolean()
    {
        return this.value();
    }


    @Override
    public String getAsString()
    {
        return String.valueOf(this.value());
    }


    public static final Codec<BoolValue> CODEC =
        Codec.BOOL.xmap(BoolValue::new, BoolValue::value);
}