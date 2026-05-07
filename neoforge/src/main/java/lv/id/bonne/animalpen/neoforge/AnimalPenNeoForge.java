package lv.id.bonne.animalpen.neoforge;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.commands.AnimalPenCommands;
import lv.id.bonne.animalpen.data.listener.AnimalInteractionReloadListener;
import lv.id.bonne.animalpen.events.CommonEvents;
import lv.id.bonne.animalpen.network.NetworkPackets;
import lv.id.bonne.animalpen.platform.Services;
import lv.id.bonne.animalpen.platform.neoforge.NetworkHandlerImpl;
import lv.id.bonne.animalpen.platform.neoforge.RegistryHelperImpl;
import lv.id.bonne.animalpen.registries.AnimalPenRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;


@Mod(AnimalPen.MOD_ID)
@EventBusSubscriber(modid = AnimalPen.MOD_ID)
public final class AnimalPenNeoForge
{
    /**
     * Constructs the mod instance.
     * Initializes helper services and runs core mod setup logic.
     * @param eventBus The mod's event bus.
     */
    public AnimalPenNeoForge(IEventBus eventBus)
    {
        RegistryHelperImpl.init(eventBus);
        // Run our common setup.
        AnimalPen.init();
    }


    /**
     * Registers entity functions for the mod.
     * @param event The NewRegistryEvent context.
     */
    @SubscribeEvent
    public static void register(NewRegistryEvent event)
    {
        event.register(AnimalPenRegistry.ENTITY_FUNCTIONS);
    }


    /**
     * Sets up network handling by registering packets.
     * @param event The RegisterPayloadHandlersEvent context.
     */
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event)
    {
        NetworkHandlerImpl net = (NetworkHandlerImpl) Services.NETWORK;
        net.init(event.registrar(AnimalPen.MOD_ID));
        NetworkPackets.register(net);
    }


    /**
     * Handles logic when a player logs into the game.
     * @param e The PlayerEvent.PlayerLoggedInEvent.
     */
    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent e)
    {
        CommonEvents.onPlayerJoin(e.getEntity());
    }


    /**
     * Registers all mod commands.
     * @param event The RegisterCommandsEvent context.
     */
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
        AnimalPenCommands.register(event.getDispatcher());
    }


    /**
     * Adds a reload listener for animal interactions when the server restarts.
     * @param event The AddServerReloadListenersEvent context.
     */
    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event)
    {
        event.addListener(
            AnimalPen.resourceOf("animal_interactions"),
            new AnimalInteractionReloadListener());
    }
}
