package lv.id.bonne.animalpen.fabric;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.commands.AnimalPenCommands;
import lv.id.bonne.animalpen.data.listener.AnimalInteractionReloadListener;
import lv.id.bonne.animalpen.events.CommonEvents;
import lv.id.bonne.animalpen.network.NetworkPackets;
import lv.id.bonne.animalpen.platform.Services;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.packs.PackType;


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
    }
}
