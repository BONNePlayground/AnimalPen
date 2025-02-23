package lv.id.bonne.animalpen;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import lv.id.bonne.animalpen.commands.AnimalPenCommands;
import lv.id.bonne.animalpen.config.ConfigurationManager;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensCreativeTabRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

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
            (dispatcher, var2, var3) -> AnimalPenCommands.register(dispatcher));
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
