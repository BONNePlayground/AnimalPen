//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.interaction.condition;


import com.mojang.serialization.Codec;

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
        if (value == null)
        {
            throw new IllegalStateException("Expected int value, got null");
        }

        return value.getAsInt();
    }


    boolean requireBool(Value value)
    {
        if (value == null)
        {
            throw new IllegalStateException("Expected boolean value, got null");
        }

        return value.getAsBoolean();
    }


    String requireString(Value value)
    {
        if (value == null)
        {
            throw new IllegalStateException("Expected string value, got null");
        }

        return value.getAsString();
    }


    public static Operator fromToken(String token)
    {
        for (Operator op : values())
        {
            if (op.token.equals(token))
            {
                return op;
            }
        }

        throw new IllegalArgumentException("Unknown operator: " + token);
    }

    private final String token;

    public static final Codec<Operator> CODEC =
        Codec.STRING.xmap(
            Operator::fromToken,
            Operator::token
        );
}