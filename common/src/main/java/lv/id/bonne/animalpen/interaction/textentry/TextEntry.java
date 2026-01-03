package lv.id.bonne.animalpen.interaction.textentry;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.Collections;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.ingredient.CustomIngredient;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;


/**
 * This record stores information about text lines that should be displayed for interaction
 *
 * @param shortMessage - The text line above block entity
 * @param longMessage - The text line in extended menu
 * @param mainItem - The item displayed in %1$s slot
 * @param resultItem - The item displayed in %2$s slot
 * @param visibility - The indication when text entry is displayed
 * @param parameters - The list of parameters displayed in slots %[3+]$s
 */
public record TextEntry(
    String shortMessage,
    String longMessage,
    CustomIngredient mainItem,
    CustomIngredient resultItem,
    TextEntryVisibility visibility,
    String... parameters)
{
    @Nullable
    public Pair<ItemStack[], Component> animalPenGetLine(CustomIngredient item,
        CompoundTag mobNBT,
        int tick,
        boolean shortLine,
        long cooldown)
    {
        if (shortLine && this.shortMessage.isBlank() || !shortLine && this.longMessage.isBlank())
        {
            return null;
        }

        ChatFormatting color = switch (this.visibility())
        {
            case READY -> ChatFormatting.GREEN;
            case COOLDOWN -> ChatFormatting.WHITE;
            case NOT_MATCH -> ChatFormatting.GOLD;
            case ON_MATCH -> ChatFormatting.GOLD;
        };

        CompoundTag animalData = mobNBT.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA);

        Object[] variables = new Object[2 + this.parameters.length];
        variables[0] = new TextComponent("\uE000");
        variables[1] = new TextComponent("\uE001");

        for (int i = 0; i < this.parameters.length; i++)
        {
            String parameter = this.parameters[i];
            String format = parameter.length() <= 2 ? parameter : parameter.substring(1, parameter.length() - 1);

            if ("[cooldown]".equals(parameter))
            {
                variables[2 + i] = LocalTime.of(0, 0, 0).
                    plusSeconds(cooldown / 20).format(TextEntry.DATE_FORMATTER);
            }
            else if (animalData.contains(format))
            {
                Tag tag = animalData.get(format);

                if (tag != null)
                {
                    // tag as string
                    variables[2 + i] = tag.getAsString();
                }
                else
                {
                    // Empty tex line
                    variables[2 + i] = "";
                }
            }
            else if (format.equals(parameter))
            {
                // Text is same as format, assume not a placeholder.
                variables[2 + i] = parameter;
            }
            else
            {
                // Empty line if parameter does not exist in data yet.
                variables[2 + i] = "";
            }
        }

        Component translatedComponent =
            new TranslatableComponent(shortLine ? this.shortMessage() : this.longMessage(), variables).
                withStyle(color);

        ItemStack[] resultPair = new ItemStack[2];
        resultPair[0] =
            this.getItemIcon(this.mainItem().isEmpty() ? item.getItems() : this.mainItem().getItems(), tick);
        resultPair[1] = this.getItemIcon(this.resultItem().getItems(), tick);

        return Pair.of(resultPair, translatedComponent);
    }


    private ItemStack getItemIcon(ItemStack[] items, int tick)
    {
        return switch (items.length)
        {
            case 0 -> ItemStack.EMPTY;
            case 1 -> items[0];
            default ->
            {
                int size = items.length;
                int index = (tick / 100) % size;

                yield items[index];
            }
        };
    }


    public static TextEntry ready(String longMessage, CustomIngredient resultItem)
    {
        return new TextEntry("display.animal_pen.ready",
            longMessage,
            CustomIngredient.EMPTY,
            resultItem,
            TextEntryVisibility.READY);
    }


    public static TextEntry cooldown(String longMessage, CustomIngredient resultItem)
    {
        return cooldown(longMessage, CustomIngredient.EMPTY, resultItem);
    }


    public static TextEntry cooldown(String longMessage, CustomIngredient mainItem, CustomIngredient resultItem)
    {
        return new TextEntry("display.animal_pen.cooldown",
            longMessage,
            mainItem,
            resultItem,
            TextEntryVisibility.COOLDOWN,
            "[cooldown]");
    }

    public static final Codec<TextEntry> CODEC =
        RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("short_text", "").forGetter(TextEntry::shortMessage),
                    Codec.STRING.optionalFieldOf("long_text", "").forGetter(TextEntry::longMessage),
                    CustomIngredient.CODEC.optionalFieldOf("main_item", CustomIngredient.EMPTY)
                        .forGetter(TextEntry::mainItem),
                    CustomIngredient.CODEC.optionalFieldOf("result_item", CustomIngredient.EMPTY)
                        .forGetter(TextEntry::resultItem),
                    TextEntryVisibility.CODEC.fieldOf("visibility").forGetter(TextEntry::visibility),
                    Codec.STRING.listOf().optionalFieldOf("parameters", Collections.emptyList()).
                        xmap(list -> list.toArray(new String[0]), Arrays::asList).
                        forGetter(TextEntry::parameters)).
                apply(instance, TextEntry::new));


    public static DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().
        appendValue(MINUTE_OF_HOUR, 2).
        optionalStart().
        appendLiteral(':').
        appendValue(SECOND_OF_MINUTE, 2).
        toFormatter();
}