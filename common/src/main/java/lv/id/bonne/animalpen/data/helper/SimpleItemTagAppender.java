package lv.id.bonne.animalpen.data.helper;


import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;


/**
 * This interface allows to add optional tags and copy tags from block tag.
 */
public interface SimpleItemTagAppender extends SimpleTagAppender<Item>
{

    SimpleItemTagAppender copy(TagKey<Block> blockTag);


    SimpleItemTagAppender optionalTag(TagKey<Item> optionalTag);


    SimpleItemTagAppender tag(TagKey<Item> itemTag);
}