package lv.id.bonne.animalpen.interaction.value;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.nbt.*;


/**
 * This allows to parse NBT tag value from data into correct type for comparison.
 * It is not saved into JSON files and is used only internally for comparison.
 */
public record TagValue(@Nullable Tag value) implements Value
{
    @Override
    public int getAsInt()
    {
        if (this.value == null || this.value.getType() != IntTag.TYPE)
        {
            AnimalPen.sendDebug("Tag cannot be converted into integer, as value is not set.");
            return 0;
        }

        return ((IntTag) this.value).getAsInt();
    }


    @Override
    public long getAsLong()
    {
        if (this.value == null || this.value.getType() != LongTag.TYPE)
        {
            AnimalPen.sendDebug("Tag cannot be converted into long, as value is not set.");
            return 0;
        }

        return ((LongTag) this.value).getAsLong();
    }


    @Override
    public boolean getAsBoolean()
    {
        if (this.value == null || this.value.getType() != ByteTag.TYPE)
        {
            AnimalPen.sendDebug("Tag cannot be converted into boolean, as value is not set.");
            return false;
        }

        return this.value == ByteTag.ONE;
    }


    @Override
    @NotNull
    public String getAsString()
    {
        return this.value == null ? "" : this.value.getAsString();
    }
}