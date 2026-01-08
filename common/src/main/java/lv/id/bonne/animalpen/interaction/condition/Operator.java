//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.interaction.condition;


import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import lv.id.bonne.animalpen.interaction.value.Value;


/**
 * This enum stores all possible operator options for ConditionEntry.
 */
public enum Operator
{
    EQ("=")
        {
            @Override
            public boolean test(Value left, Value right)
            {
                return left.getAsInt() == right.getAsInt();
            }
        },
    NE("!=")
        {
            @Override
            public boolean test(Value left, Value right)
            {
                return left.getAsInt() != right.getAsInt();
            }
        },
    LT("<")
        {
            @Override
            public boolean test(Value left, Value right)
            {
                return left.getAsInt() < right.getAsInt();
            }
        },
    LTE("<=")
        {
            @Override
            public boolean test(Value left, Value right)
            {
                return left.getAsInt() <= right.getAsInt();
            }
        },
    GT(">")
        {
            @Override
            public boolean test(Value left, Value right)
            {
                return left.getAsInt() > right.getAsInt();
            }
        },
    GTE(">=")
        {
            @Override
            public boolean test(Value left, Value right)
            {
                return left.getAsInt() >= right.getAsInt();
            }
        },
    MATCH("match")
        {
            @Override
            public boolean test(Value left, Value right)
            {
                return left.getAsString().equals(right.getAsString());
            }
        },
    HAS("has")
        {
            @Override
            public boolean test(Value left, Value right)
            {
                return left.getAsBoolean() == right.getAsBoolean();
            }
        };


    Operator(String token)
    {
        this.token = token;
    }


    public abstract boolean test(Value left, Value right);


    private final String token;


    public static final Codec<Operator> CODEC =
        Codec.STRING.flatXmap(
            token ->
            {
                for (Operator op : values())
                {
                    if (op.token.equals(token))
                    {
                        return DataResult.success(op);
                    }
                }
                return DataResult.error("Unknown operator: " + token);
            },
            op -> DataResult.success(op.token)
        );
}