package lv.id.bonne.animalpen.data.provider.fabric;


import lv.id.bonne.animalpen.data.helper.SimpleItemTagAppender;
import lv.id.bonne.animalpen.data.provider.ModItemTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;


public class FabricModItemTagProvider extends FabricTagProvider.ItemTagProvider implements ModItemTagsProvider
{
    public FabricModItemTagProvider(FabricDataGenerator dataGenerator,
        FabricTagProvider.BlockTagProvider blockTagProvider)
    {
        super(dataGenerator, blockTagProvider);
    }


    @Override
    protected void generateTags()
    {
        this.addModTags();
    }


    @Override
    public SimpleItemTagAppender modTag(TagKey<Item> tag)
    {
        var builder = this.tag(tag);

        return new SimpleItemTagAppender() {

            @Override
            public SimpleItemTagAppender add(Item value) {
                builder.add(value);
                return this;
            }

            @Override
            public SimpleItemTagAppender copy(TagKey<Block> blockTag) {
                FabricModItemTagProvider.this.copy(blockTag, tag);
                return this;
            }

            @Override
            public SimpleItemTagAppender optionalTag(ResourceLocation other) {
                builder.addOptionalTag(other);
                return this;
            }
        };
    }
}