//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.data.provider.forge;


import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.function.Consumer;

import lv.id.bonne.animalpen.data.provider.ModAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;


public class ForgeModAdvancementProvider extends AdvancementProvider implements ModAdvancementProvider
{
    public ForgeModAdvancementProvider(DataGenerator generatorIn,
        ExistingFileHelper fileHelperIn)
    {
        super(generatorIn, fileHelperIn);
    }


    @Override
    protected void registerAdvancements(@NotNull Consumer<Advancement> consumer, @NotNull ExistingFileHelper fileHelper)
    {
        this.buildModAdvancements(consumer);
    }


    @Override
    public Advancement generatePlatformAdvancement(Consumer<Advancement> consumer,
        Advancement.Builder builder,
        ResourceLocation resourceLocation)
    {
        return builder.save(consumer, resourceLocation, this.fileHelper);
    }
}
