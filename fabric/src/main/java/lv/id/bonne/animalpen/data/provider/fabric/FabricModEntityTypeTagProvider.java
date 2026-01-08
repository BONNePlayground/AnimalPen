package lv.id.bonne.animalpen.data.provider.fabric;


import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.helper.SimpleTagAppender;
import lv.id.bonne.animalpen.data.provider.ModEntityTypeTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;


public class FabricModEntityTypeTagProvider extends FabricTagProvider.EntityTypeTagProvider
    implements ModEntityTypeTagsProvider
{

    public FabricModEntityTypeTagProvider(FabricDataOutput output,
        CompletableFuture<HolderLookup.Provider> completableFuture)
    {
        super(output, completableFuture);
    }


    @Override
    protected void addTags(HolderLookup.Provider provider)
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
                BuiltInRegistries.ENTITY_TYPE.getResourceKey(value).
                    ifPresent(builder::add);
                return this;
            }
        };
    }
}