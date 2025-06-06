package lv.id.bonne.animalpen;


import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import lv.id.bonne.animalpen.blocks.behaviour.UseToolsBehaviour;
import lv.id.bonne.animalpen.commands.AnimalPenCommands;
import lv.id.bonne.animalpen.config.ConfigurationManager;
import lv.id.bonne.animalpen.listeners.AnimalFoodReloadListener;
import lv.id.bonne.animalpen.mixin.accessors.DispenserBlockAccessor;
import lv.id.bonne.animalpen.network.packets.*;
import lv.id.bonne.animalpen.registries.*;
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
        AnimalPenDataComponentRegistry.register();

        AnimalPen.CONFIG_MANAGER.readConfig();

        CommandRegistrationEvent.EVENT.register(
            (dispatcher, var2, var3) -> AnimalPenCommands.register(dispatcher));

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
            UpdateAnimalSizeData.ID,
            UpdateAnimalSizeData.STREAM_CODEC,
            UpdateAnimalSizeData::handle);

        // register the listener
        ReloadListenerRegistry.register(PackType.SERVER_DATA,
            new AnimalFoodReloadListener(),
            ResourceLocation.tryBuild(MOD_ID, "animal_foods"));

        EnvExecutor.runInEnv(Env.SERVER, () -> AnimalPen::initializeServer);
    }


    private static void initializeServer()
    {
        NetworkManager.registerS2CPayloadType(AnimalFoodRegistryData.ID, AnimalFoodRegistryData.STREAM_CODEC);
        NetworkManager.registerS2CPayloadType(UpdateVariantScreenData.ID, UpdateVariantScreenData.STREAM_CODEC);

        PlayerEvent.PLAYER_JOIN.register(player ->
            NetworkManager.sendToPlayer(player,
                new AnimalFoodRegistryData(AnimalPenFoodRegistry.getAll())));
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
