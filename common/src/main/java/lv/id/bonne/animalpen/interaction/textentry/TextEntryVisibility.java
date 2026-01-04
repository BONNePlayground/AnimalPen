package lv.id.bonne.animalpen.interaction.textentry;


import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;


/**
 * This enum stores all visibility modes for Text Entry
 */
public enum TextEntryVisibility
{
    /**
     * Displayed only when ready
     */
    READY,
    /**
     * Displayed only when under cooldown
     */
    COOLDOWN,
    /**
     * Displayed on every match
     */
    ON_MATCH,
    /**
     * Displayed when not matched
     */
    NOT_MATCH;

    public static final Codec<TextEntryVisibility> CODEC =
        Codec.STRING.comapFlatMap(
            str -> {
                try {
                    return DataResult.success(TextEntryVisibility.valueOf(str.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "Unknown TextEntryVisibility: " + str);
                }
            },
            vis -> vis.name().toLowerCase()
        );
}