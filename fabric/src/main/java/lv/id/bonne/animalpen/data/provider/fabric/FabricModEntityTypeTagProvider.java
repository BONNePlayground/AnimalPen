package lv.id.bonne.animalpen.data.provider.fabric;


import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.helper.SimpleTagAppender;
import lv.id.bonne.animalpen.data.provider.ModEntityTypeTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;


public class FabricModEntityTypeTagProvider extends FabricTagsProvider.EntityTypeTagsProvider
    implements ModEntityTypeTagsProvider
{

    public FabricModEntityTypeTagProvider(FabricPackOutput output,
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
        TagAppender<EntityType<?>> builder = this.tag(tag);

        return new SimpleTagAppender<>()
        {
            @Override
            public SimpleTagAppender<EntityType<?>> add(EntityType<?> value)
            {
                builder.add(value.builtInRegistryHolder().key());
                return this;
            }
        };
    }
}