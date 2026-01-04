package lv.id.bonne.animalpen.data.provider.forge;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.helper.SimpleTagAppender;
import lv.id.bonne.animalpen.data.provider.ModEntityTypeTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;


public class ForgeModEntityTypeTagProvider extends EntityTypeTagsProvider implements ModEntityTypeTagsProvider
{
    public ForgeModEntityTypeTagProvider(PackOutput arg,
        CompletableFuture<HolderLookup.Provider> completableFuture,
        String modId,
        @Nullable ExistingFileHelper existingFileHelper)
    {
        super(arg, completableFuture, modId, existingFileHelper);
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
                builder.add(value);
                return this;
            }
        };
    }
}