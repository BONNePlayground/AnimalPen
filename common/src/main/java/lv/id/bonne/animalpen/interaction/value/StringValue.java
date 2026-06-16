package lv.id.bonne.animalpen.interaction.value;


import com.mojang.serialization.Codec;


public record StringValue(String value) implements Value
{
    @Override
    public int getAsInt()
    {
        return Integer.parseInt(this.value());
    }


    @Override
    public long getAsLong()
    {
        return Long.parseLong(this.value());
    }


    @Override
    public boolean getAsBoolean()
    {
        return Boolean.getBoolean(this.value());
    }


    @Override
    public String getAsString()
    {
        return this.value();
    }


    public static final Codec<StringValue> CODEC =
        Codec.STRING.xmap(StringValue::new, StringValue::value);
}