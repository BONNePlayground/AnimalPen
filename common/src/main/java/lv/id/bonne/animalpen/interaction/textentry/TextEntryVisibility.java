package lv.id.bonne.animalpen.interaction.textentry;


import com.mojang.serialization.Codec;


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
        Codec.STRING.xmap(
            str -> TextEntryVisibility.valueOf(str.toUpperCase()),
            vis -> vis.name().toLowerCase()
        );
}