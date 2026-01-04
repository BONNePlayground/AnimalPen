//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.interaction.condition;


import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.value.Value;
import net.minecraft.nbt.CompoundTag;


/**
 * This enum stores all possible operator options for ConditionEntry.
 */
public enum Operator
{
    EQ("=")
        {
            @Override
            public boolean test(CompoundTag tag, ConditionEntry entry)
            {
                int value = this.requireInt(entry.value());
                return tag.getInt(entry.key()) == value;
            }
        },
    NE("!=")
        {
            @Override
            public boolean test(CompoundTag tag, ConditionEntry entry)
            {
                int value = this.requireInt(entry.value());
                return tag.getInt(entry.key()) != value;
            }
        },
    LT("<")
        {
            @Override
            public boolean test(CompoundTag tag, ConditionEntry entry)
            {
                int value = this.requireInt(entry.value());
                return tag.getInt(entry.key()) < value;
            }
        },
    LTE("<=")
        {
            @Override
            public boolean test(CompoundTag tag, ConditionEntry entry)
            {
                int value = this.requireInt(entry.value());
                return tag.getInt(entry.key()) <= value;
            }
        },
    GT(">")
        {
            @Override
            public boolean test(CompoundTag tag, ConditionEntry entry)
            {
                int value = this.requireInt(entry.value());
                return tag.getInt(entry.key()) > value;
            }
        },
    GTE(">=")
        {
            @Override
            public boolean test(CompoundTag tag, ConditionEntry entry)
            {
                int value = this.requireInt(entry.value());
                return tag.getInt(entry.key()) >= value;
            }
        },
    HAS("has")
        {
            @Override
            public boolean test(CompoundTag tag, ConditionEntry entry)
            {
                boolean value = this.requireBool(entry.value());
                return tag.contains(entry.key()) == value;
            }
        },
    MATCH("match")
        {
            @Override
            public boolean test(CompoundTag tag, ConditionEntry entry)
            {
                String value = this.requireString(entry.value());
                return tag.getString(entry.key()).equals(value);
            }
        };

    Operator(String token)
    {
        this.token = token;
    }


    public String token()
    {
        return this.token;
    }


    public abstract boolean test(CompoundTag tag, ConditionEntry entry);


    int requireInt(Value value)
    {
        try
        {
            return value.getAsInt();
        }
        catch (Exception e)
        {
            AnimalPen.LOGGER.error("Something went wrong with `" + this.token + "` operator requesting integer: " + e);
            return 0;
        }
    }


    boolean requireBool(Value value)
    {
        try
        {
            return value.getAsBoolean();
        }
        catch (Exception e)
        {
            AnimalPen.LOGGER.error("Something went wrong with `" + this.token + "` operator requesting boolean: " + e);
            return false;
        }
    }


    String requireString(Value value)
    {
        try
        {
            return value.getAsString();
        }
        catch (Exception e)
        {
            AnimalPen.LOGGER.error("Something went wrong with `" + this.token + "` operator requesting text: " + e);
            return "";
        }
    }


    private final String token;

    public static final Codec<Operator> CODEC =
        Codec.STRING.flatXmap(
            token -> {
                for (Operator op : values()) {
                    if (op.token.equals(token)) {
                        return DataResult.success(op);
                    }
                }
                return DataResult.error("Unknown operator: " + token);
            },
            op -> DataResult.success(op.token)
        );
}