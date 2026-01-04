package lv.id.bonne.animalpen;


import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.util.List;
import java.util.Map;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkChannel;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.ReloadListenerRegistry;
import lv.id.bonne.animalpen.blocks.behaviour.UseToolsBehaviour;
import lv.id.bonne.animalpen.commands.AnimalPenCommands;
import lv.id.bonne.animalpen.config.Configuration;
import lv.id.bonne.animalpen.config.ConfigurationManager;
import lv.id.bonne.animalpen.data.listener.AnimalInteractionReloadListener;
import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.mixin.accessors.DispenserBlockAccessor;
import lv.id.bonne.animalpen.network.packets.*;
import lv.id.bonne.animalpen.registries.*;
import net.minecraft.core.Registry;
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
        AnimalPensCreativeTabRegistry.register();
        AnimalPenBlockRegistry.register();
        AnimalPensItemRegistry.register();
        AnimalPenTileEntityRegistry.register();
        AnimalPenFunctionRegistry.register();

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

        // Networking

        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            UpdateDisplayAnimalData.ID,
            UpdateDisplayAnimalData::handle);

        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            RemoveDisplayAnimalData.ID,
            RemoveDisplayAnimalData::handle);

        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            UpdateConfigurationData.ID,
            UpdateConfigurationData::handle);

        // Register into separate channel, as S2C crashes on fabric servers.
        CHANNEL.register(AnimalInteractionSyncStartPacket.class,
            AnimalInteractionSyncStartPacket::encode,
            AnimalInteractionSyncStartPacket::decode,
            AnimalInteractionSyncStartPacket::handle);
        CHANNEL.register(AnimalInteractionSyncEntityPacket.class,
            AnimalInteractionSyncEntityPacket::encode,
            AnimalInteractionSyncEntityPacket::decode,
            AnimalInteractionSyncEntityPacket::handle);
        CHANNEL.register(AnimalInteractionSyncEndPacket.class,
            AnimalInteractionSyncEndPacket::encode,
            AnimalInteractionSyncEndPacket::decode,
            AnimalInteractionSyncEndPacket::handle);

        CHANNEL.register(UpdateVariantScreenData.class,
            UpdateVariantScreenData::encode,
            UpdateVariantScreenData::decode,
            UpdateVariantScreenData::handle);

        // register the listener
        ReloadListenerRegistry.register(PackType.SERVER_DATA,
            new AnimalInteractionReloadListener(),
            AnimalPen.resourceOf("animal_interactions"));

        PlayerEvent.PLAYER_JOIN.register(player ->
        {
            Map<EntityType<?>, List<AnimalInteraction>> data = AnimalPenInteractionRegistry.getAll();

            CHANNEL.sendToPlayer(player,
                new AnimalInteractionSyncStartPacket(data.size()));

            for (var entry : data.entrySet())
            {
                ResourceLocation entityId = Registry.ENTITY_TYPE.getKey(entry.getKey());

                CHANNEL.sendToPlayer(player,
                    new AnimalInteractionSyncEntityPacket(entityId, entry.getValue()));
            }

            CHANNEL.sendToPlayer(player, new AnimalInteractionSyncEndPacket());
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

    public static final NetworkChannel CHANNEL =
        NetworkChannel.create(AnimalPen.resourceOf("network"));
}
