package lv.id.bonne.animalpen.fabric;


import java.util.EnumMap;
import java.util.function.Supplier;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.commands.AnimalPenCommands;
import lv.id.bonne.animalpen.data.listener.AnimalInteractionReloadListener;
import lv.id.bonne.animalpen.events.CommonEvents;
import lv.id.bonne.animalpen.network.NetworkPackets;
import lv.id.bonne.animalpen.platform.Services;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.BlockTransformerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.server.packs.PackType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;


public final class AnimalPenFabric implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        // Run our common setup.
        AnimalPen.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
        {
            AnimalPenCommands.register(dispatcher);
        });

        NetworkPackets.register(Services.NETWORK);

        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
            AnimalPen.resourceOf("animal_interactions"),
            new AnimalInteractionReloadListener());

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            CommonEvents.onPlayerJoin(handler.player));

        BlockTransformerEvents.MODIFY.register((key, transforms, source, registries) ->
        {
            if (key != BlockTransformers.AXE)
            {
                return;
            }

            EnumMap<WeatheringCopper.WeatherState, Supplier<Block>> copper = AnimalPenBlockRegistry.COPPER_AVIARIES;
            EnumMap<WeatheringCopper.WeatherState, Supplier<Block>> waxed = AnimalPenBlockRegistry.WAXED_COPPER_AVIARIES;

            WeatheringCopper.WeatherState[] states = WeatheringCopper.WeatherState.values();

            for (int i = 1; i < states.length; i++)
            {
                Supplier<Block> from = copper.get(states[i]);
                Supplier<Block> to = copper.get(states[i - 1]);

                if (from == null || to == null)
                {
                    continue;
                }

                transforms.add(
                    BlockTransformer.BlockTransformData.builder(
                            BlockPredicate.matchesBlocks(from.get()),
                            to.get()
                        ).
                        sound(Holder.direct(SoundEvents.AXE_SCRAPE.value())).
                        particle(BlockTransformer.TransformParticle.SCRAPE).
                        build()
                );
            }

            for (WeatheringCopper.WeatherState state : states)
            {
                Supplier<Block> from = waxed.get(state);
                Supplier<Block> to = copper.get(state);

                if (from == null || to == null)
                {
                    continue;
                }

                transforms.add(
                    BlockTransformer.BlockTransformData.builder(
                            BlockPredicate.matchesBlocks(from.get()),
                            to.get()
                        ).
                        sound(Holder.direct(SoundEvents.AXE_WAX_OFF.value())).
                        particle(BlockTransformer.TransformParticle.WAX_OFF).
                        build()
                );
            }
        });
    }
}
