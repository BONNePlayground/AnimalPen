package lv.id.bonne.animalpen.data.provider.neoforge;


import org.jetbrains.annotations.NotNull;
import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.helper.SimpleTagAppender;
import lv.id.bonne.animalpen.data.provider.ModEntityTypeTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;


public class NeoForgeModEntityTypeTagProvider extends EntityTypeTagsProvider implements ModEntityTypeTagsProvider
{
    public NeoForgeModEntityTypeTagProvider(PackOutput arg,
        CompletableFuture<HolderLookup.Provider> completableFuture,
        String modId)
    {
        super(arg, completableFuture, modId);
    }


    @Override
    protected void addTags(@NotNull HolderLookup.Provider arg)
    {
        this.addModTags();
    }


    @Override
    public SimpleTagAppender<EntityType<?>> modTag(TagKey<EntityType<?>> tag)
    {
        var builder = this.tag(tag);

        return new SimpleTagAppender<>() {

            @Override
            public SimpleTagAppender<EntityType<?>> add(EntityType<?> value) {
                builder.add(value.builtInRegistryHolder().key());
                return this;
            }
        };
    }
}