package lv.id.bonne.animalpen.interaction.cooldown;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import java.util.Random;

import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;


/**
 * This class stores cooldown related information
 */
public sealed interface CooldownEntry permits
    CooldownEntry.Linear,
    CooldownEntry.Randomized,
    CooldownEntry.Static
{
    /**
     * This method calculates the cooldown value.
     *
     * @param animalCount The animal count.
     * @return The cooldown in ticks calculated by cooldown entry.
     */
    long calculateCooldown(long animalCount);


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
        public long calculateCooldown(long animalCount)
        {
            long value = this.base + animalCount * this.delta;

            return this.delta >= 0 ?
                (int) Mth.clamp(value, this.base, this.limit) :
                (int) Mth.clamp(value, this.limit, this.base);
        }


        public static final MapCodec<Linear> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
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
        public long calculateCooldown(long animalCount)
        {
            return this.base;
        }


        public static final MapCodec<Static> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
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
        public long calculateCooldown(long animalCount)
        {
            return this.min + Randomized.RANDOM.nextInt(this.max - this.min + 1);
        }


        public static final Random RANDOM = new Random();


        public static final MapCodec<Randomized> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("min").forGetter(Randomized::min),
                Codec.INT.fieldOf("max").forGetter(Randomized::max)
            ).apply(instance, Randomized::new));
    }


    enum CooldownType implements StringRepresentable
    {
        LINEAR("linear"),
        STATIC("static"),
        RANDOM("random");

        CooldownType(String name)
        {
            this.name = name;
        }


        @Override
        @NotNull
        public String getSerializedName()
        {
            return this.name;
        }


        public static final Codec<CooldownEntry.CooldownType> CODEC =
            StringRepresentable.fromEnum(CooldownEntry.CooldownType::values);

        private final String name;
    }


    public static final Codec<CooldownEntry> CODEC =
        CooldownEntry.CooldownType.CODEC.dispatch(
            "type",
            entry -> switch (entry) {
                case CooldownEntry.Linear l -> CooldownType.LINEAR;
                case CooldownEntry.Static s -> CooldownType.STATIC;
                case CooldownEntry.Randomized r -> CooldownType.RANDOM;
            },
            type -> switch (type) {
                case LINEAR -> CooldownEntry.Linear.CODEC;
                case STATIC -> CooldownEntry.Static.CODEC;
                case RANDOM -> CooldownEntry.Randomized.CODEC;
            }
        );
}