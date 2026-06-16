package lv.id.bonne.animalpen.interaction.value;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.nbt.Tag;


/**
 * This allows to parse NBT tag value from data into correct type for comparison.
 * It is not saved into JSON files and is used only internally for comparison.
 */
public record TagValue(@Nullable Tag value) implements Value
{
    @Override
    public int getAsInt()
    {
        if (this.value == null || this.value.asInt().isEmpty())
        {
            AnimalPen.sendDebug("Tag cannot be converted into integer, as value is not set.");
            return 0;
        }

        return this.value.asInt().orElse(0);
    }


    @Override
    public long getAsLong()
    {
        if (this.value == null || this.value.asLong().isEmpty())
        {
            AnimalPen.sendDebug("Tag cannot be converted into long, as value is not set.");
            return 0;
        }

        return this.value.asLong().orElse(0L);
    }


    @Override
    public boolean getAsBoolean()
    {
        if (this.value == null || this.value.asBoolean().isEmpty())
        {
            AnimalPen.sendDebug("Tag cannot be converted into boolean, as value is not set.");
            return false;
        }

        return this.value.asBoolean().orElse(false);
    }


    @Override
    @NotNull
    public String getAsString()
    {
        return this.value == null ? "" : this.value.asString().orElse("");
    }
}