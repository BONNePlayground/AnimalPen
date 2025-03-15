package lv.id.bonne.animalpen;


import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.ReloadListenerRegistry;
import lv.id.bonne.animalpen.commands.AnimalPenCommands;
import lv.id.bonne.animalpen.config.ConfigurationManager;
import lv.id.bonne.animalpen.listeners.AnimalFoodReloadListener;
import lv.id.bonne.animalpen.network.packets.RemoveDisplayAnimalData;
import lv.id.bonne.animalpen.network.packets.UpdateAnimalSizeData;
import lv.id.bonne.animalpen.network.packets.UpdateDisplayAnimalData;
import lv.id.bonne.animalpen.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;


public final class AnimalPen
{
    public static void init()
    {
        // Write common init code here.
        AnimalPensCreativeTabRegistry.register();
        AnimalPenBlockRegistry.register();
        AnimalPensItemRegistry.register();
        AnimalPenTileEntityRegistry.register();
        AnimalPenDataComponentRegistry.register();

        AnimalPen.CONFIG_MANAGER.readConfig();

        CommandRegistrationEvent.EVENT.register(
            (dispatcher, var2, var3) -> AnimalPenCommands.register(dispatcher));

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
            UpdateAnimalSizeData.ID,
            UpdateAnimalSizeData.STREAM_CODEC,
            UpdateAnimalSizeData::handle);

        // register the listener
        ReloadListenerRegistry.register(PackType.SERVER_DATA,
            new AnimalFoodReloadListener(),
            new ResourceLocation(MOD_ID, "animal_foods"));
    }


    public static final String MOD_ID = "animal_pen";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ConfigurationManager CONFIG_MANAGER = new ConfigurationManager();

    public static DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().
        appendValue(MINUTE_OF_HOUR, 2).
        optionalStart().
        appendLiteral(':').
        appendValue(SECOND_OF_MINUTE, 2).
        toFormatter();
}
