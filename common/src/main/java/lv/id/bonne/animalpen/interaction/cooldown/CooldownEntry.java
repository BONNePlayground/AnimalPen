package lv.id.bonne.animalpen.interaction.cooldown;


import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;

import net.minecraft.util.Mth;


/**
 * This class stores cooldown related information
 */
public interface CooldownEntry
{
    /**
     * This method calculates the cooldown value.
     *
     * @param animalCount The animal count.
     * @return The cooldown in ticks calculated by cooldown entry.
     */
    int calculateCooldown(long animalCount);


    /**
     * Cooldown that is calculated linearly based on animals in pen.
     *
     * @param base The base value from which calculations starts
     * @param delta The change of cooldown value
     * @param limit The max or min value of cooldown (based on delta sign)
     */
    record Linear(int base, int delta, int limit) implements CooldownEntry
    {
        @Override
        public int calculateCooldown(long animalCount)
        {
            long value = this.base + animalCount * this.delta;

            return this.delta >= 0 ?
                (int) Mth.clamp(value, this.base, this.limit) :
                (int) Mth.clamp(value, this.limit, this.base);
        }


        public static final Codec<Linear> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("base").forGetter(Linear::base),
                Codec.INT.fieldOf("delta").forGetter(Linear::delta),
                Codec.INT.fieldOf("limit").forGetter(Linear::limit)
            ).apply(instance, Linear::new));
    }


    /**
     * Cooldown that is static regardless of how many animals are in pen.
     *
     * @param base the cooldown value in ticks
     */
    record Static(int base) implements CooldownEntry
    {
        @Override
        public int calculateCooldown(long animalCount)
        {
            return this.base;
        }


        public static final Codec<Static> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("base").forGetter(Static::base)
            ).apply(instance, Static::new));
    }


    /**
     * Cooldown that is calculated randomly between min and max value.
     *
     * @param min The minimal cooldown value
     * @param max The maximal cooldown value
     */
    record Randomized(int min, int max) implements CooldownEntry
    {
        @Override
        public int calculateCooldown(long animalCount)
        {
            return this.min + Randomized.RANDOM.nextInt(this.max - this.min + 1);
        }


        public static final Random RANDOM = new Random();


        public static final Codec<Randomized> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("min").forGetter(Randomized::min),
                Codec.INT.fieldOf("max").forGetter(Randomized::max)
            ).apply(instance, Randomized::new));
    }


    Codec<CooldownEntry> CODEC = Codec.STRING.
        dispatch("type",
            entry ->
            {
                if (entry instanceof Linear)
                {
                    return "linear";
                }
                else if (entry instanceof Static)
                {
                    return "static";
                }
                else if (entry instanceof Randomized)
                {
                    return "random";
                }
                else
                {
                    // This should never happen
                    return entry.toString();
                }
            },
            type -> switch (type)
            {
                case "linear" -> Linear.CODEC;
                case "static" -> Static.CODEC;
                case "random" -> Randomized.CODEC;
                default -> Codec.EMPTY.codec().flatXmap(
                    empty -> DataResult.error("Unknown cooldown type: " + type),
                    entry -> DataResult.error("Unknown cooldown type: " + type)
                );
            });
}