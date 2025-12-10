package lv.id.bonne.animalpen.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionCheck;

import static net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER;


public class AnimalPenCommands
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal("animal_pen").
            requires(Commands.hasPermission(LEVEL_GAMEMASTER));

        LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("reload").
            executes(ctx ->
            {
                AnimalPen.CONFIG_MANAGER.reloadConfig();

                ctx.getSource().sendSuccess(() -> Component.literal("Config file reloaded."), true);

                return 1;
            });

        LiteralArgumentBuilder<CommandSourceStack> reset = Commands.literal("reset").
            executes(ctx ->
            {
                AnimalPen.CONFIG_MANAGER.generateConfig();
                ctx.getSource().sendSuccess(() -> Component.literal("Config file reset."), true);
                return 1;
            });

        dispatcher.register(baseLiteral.then(reset).then(reload));
    }


    private static final PermissionCheck LEVEL_GAMEMASTER = new PermissionCheck.Require(COMMANDS_GAMEMASTER);
}
