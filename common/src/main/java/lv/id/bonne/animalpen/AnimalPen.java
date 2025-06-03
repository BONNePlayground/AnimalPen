package lv.id.bonne.animalpen;


import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.ReloadListenerRegistry;
import lv.id.bonne.animalpen.blocks.behaviour.UseToolsBehaviour;
import lv.id.bonne.animalpen.commands.AnimalPenCommands;
import lv.id.bonne.animalpen.config.ConfigurationManager;
import lv.id.bonne.animalpen.listeners.AnimalFoodReloadListener;
import lv.id.bonne.animalpen.mixin.accessors.DispenserBlockAccessor;
import lv.id.bonne.animalpen.network.packets.AnimalFoodRegistryData;
import lv.id.bonne.animalpen.network.packets.RemoveDisplayAnimalData;
import lv.id.bonne.animalpen.network.packets.UpdateAnimalSizeData;
import lv.id.bonne.animalpen.network.packets.UpdateDisplayAnimalData;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensCreativeTabRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;

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

        AnimalPen.CONFIG_MANAGER.readConfig();

        CommandRegistrationEvent.EVENT.register(
            (dispatcher, selection) -> AnimalPenCommands.register(dispatcher));

        // Dispenser interaction
        DispenserBlock.registerBehavior(Items.SHEARS,
            new UseToolsBehaviour(DispenserBlockAccessor.getDispenserRegistry().get(Items.SHEARS)));
        DispenserBlock.registerBehavior(Items.GLASS_BOTTLE,
            new UseToolsBehaviour(DispenserBlockAccessor.getDispenserRegistry().get(Items.GLASS_BOTTLE)));
        DispenserBlock.registerBehavior(Items.BUCKET,
            new UseToolsBehaviour(DispenserBlockAccessor.getDispenserRegistry().get(Items.BUCKET)));
        DispenserBlock.registerBehavior(Items.BOWL,
            new UseToolsBehaviour(DispenserBlockAccessor.getDispenserRegistry().get(Items.BOWL)));

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
            UpdateAnimalSizeData.ID,
            UpdateAnimalSizeData::handle);

        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            AnimalFoodRegistryData.ID,
            AnimalFoodRegistryData::handle);

        // register the listener
        ReloadListenerRegistry.register(PackType.SERVER_DATA,
            new AnimalFoodReloadListener(),
            new ResourceLocation(MOD_ID, "animal_foods"));

        PlayerEvent.PLAYER_JOIN.register(player ->
            NetworkManager.sendToPlayer(player,
                AnimalFoodRegistryData.ID,
                AnimalFoodRegistryData.encode()));
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
