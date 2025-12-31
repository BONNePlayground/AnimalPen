package lv.id.bonne.animalpen.data.provider.fabric;


import lv.id.bonne.animalpen.data.helper.SimpleTagAppender;
import lv.id.bonne.animalpen.data.provider.ModEntityTypeTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;


public class FabricModEntityTypeTagProvider extends FabricTagProvider.EntityTypeTagProvider
    implements ModEntityTypeTagsProvider
{
    public FabricModEntityTypeTagProvider(FabricDataGenerator dataGenerator)
    {
        super(dataGenerator);
    }


    @Override
    protected void generateTags()
    {
        this.addModTags();
    }


    @Override
    public SimpleTagAppender<EntityType<?>> modTag(TagKey<EntityType<?>> tag)
    {
        var builder = this.tag(tag);

        return new SimpleTagAppender<>()
        {
            @Override
            public SimpleTagAppender<EntityType<?>> add(EntityType<?> value)
            {
                builder.add(value);
                return this;
            }
        };
    }
}