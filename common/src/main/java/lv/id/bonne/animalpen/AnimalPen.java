package lv.id.bonne.animalpen;


import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.util.List;
import java.util.Map;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import lv.id.bonne.animalpen.blocks.behaviour.UseToolsBehaviour;
import lv.id.bonne.animalpen.commands.AnimalPenCommands;
import lv.id.bonne.animalpen.config.Configuration;
import lv.id.bonne.animalpen.config.ConfigurationManager;
import lv.id.bonne.animalpen.data.listener.AnimalInteractionReloadListener;
import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.mixin.accessors.DispenserBlockAccessor;
import lv.id.bonne.animalpen.network.packets.*;
import lv.id.bonne.animalpen.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;


public final class AnimalPen
{
    public static void init()
    {
        // Write common init code here.
        AnimalPenDataComponentRegistry.register();
        AnimalPensCreativeTabRegistry.register();
        AnimalPenBlockRegistry.register();
        AnimalPensItemRegistry.register();
        AnimalPenTileEntityRegistry.register();
        AnimalPenFunctionRegistry.register();
        AnimalPenCriteriaTriggersRegistry.register();

        AnimalPen.CONFIG_MANAGER.readConfig();

        CommandRegistrationEvent.EVENT.register(
            (dispatcher, registry, selection) -> AnimalPenCommands.register(dispatcher));

        // Dispenser interaction
        DispenserBlock.registerBehavior(Items.SHEARS,
            new UseToolsBehaviour(DispenserBlockAccessor.getDispenserRegistry().get(Items.SHEARS)));

        DispenserBlock.registerBehavior(Items.GLASS_BOTTLE,
            new UseToolsBehaviour(DispenserBlockAccessor.getDispenserRegistry().get(Items.GLASS_BOTTLE)));
        DispenserBlock.registerBehavior(Items.BUCKET,
            new UseToolsBehaviour(DispenserBlockAccessor.getDispenserRegistry().get(Items.BUCKET)));
        DispenserBlock.registerBehavior(Items.BOWL,
            new UseToolsBehaviour(DispenserBlockAccessor.getDispenserRegistry().get(Items.BOWL)));
        DispenserBlock.registerBehavior(Items.WATER_BUCKET,
            new UseToolsBehaviour(DispenserBlockAccessor.getDispenserRegistry().get(Items.WATER_BUCKET)));
        DispenserBlock.registerBehavior(Items.BRUSH,
            new UseToolsBehaviour(DispenserBlockAccessor.getDispenserRegistry().get(Items.BRUSH)));

        // Networking

        NetworkManager.registerReceiver(NetworkManager.Side.C2S,
            UpdateDisplayAnimalData.ID,
            UpdateDisplayAnimalData.STREAM_CODEC,
            UpdateDisplayAnimalData::handle);

        NetworkManager.registerReceiver(NetworkManager.Side.C2S,
            RemoveDisplayAnimalData.ID,
            RemoveDisplayAnimalData.STREAM_CODEC,
            RemoveDisplayAnimalData::handle);

        NetworkManager.registerReceiver(NetworkManager.Side.C2S,
            UpdateConfigurationData.ID,
            UpdateConfigurationData.STREAM_CODEC,
            UpdateConfigurationData::handle);

        // register the listener
        ReloadListenerRegistry.register(PackType.SERVER_DATA,
            new AnimalInteractionReloadListener(),
            AnimalPen.resourceOf("animal_interactions"));

        EnvExecutor.runInEnv(Env.SERVER, () -> AnimalPen::initializeServer);
    }


    private static void initializeServer()
    {
        NetworkManager.registerS2CPayloadType(AnimalInteractionSyncStartPacket.ID, AnimalInteractionSyncStartPacket.STREAM_CODEC);
        NetworkManager.registerS2CPayloadType(AnimalInteractionSyncEntityPacket.ID, AnimalInteractionSyncEntityPacket.STREAM_CODEC);
        NetworkManager.registerS2CPayloadType(AnimalInteractionSyncEndPacket.ID, AnimalInteractionSyncEndPacket.STREAM_CODEC);
        NetworkManager.registerS2CPayloadType(UpdateVariantScreenData.ID, UpdateVariantScreenData.STREAM_CODEC);

        PlayerEvent.PLAYER_JOIN.register(player ->
        {
            Map<ResourceKey<EntityType<?>>, List<AnimalInteraction>> data = AnimalPenInteractionRegistry.getAll();

            NetworkManager.sendToPlayer(player,
                new AnimalInteractionSyncStartPacket(data.size()));

            for (var entry : data.entrySet())
            {
                NetworkManager.sendToPlayer(player,
                    new AnimalInteractionSyncEntityPacket(entry.getKey(), entry.getValue()));
            }

            NetworkManager.sendToPlayer(player, new AnimalInteractionSyncEndPacket());
        });
    }


    public static Configuration config()
    {
        return CONFIG_MANAGER.getConfiguration();
    }


    public static void sendDebug(String message)
    {
        if (AnimalPen.config().isDebug())
        {
            AnimalPen.LOGGER.debug(message);
        }
    }


    public static ResourceLocation resourceOf(String key)
    {
        return new ResourceLocation(AnimalPen.MOD_ID, key);
    }

    public static final String MOD_ID = "animal_pen";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ConfigurationManager CONFIG_MANAGER = new ConfigurationManager();
}
