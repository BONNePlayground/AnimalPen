package lv.id.bonne.animalpen.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;


public class AnimalPenCommands
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal("animal_pen").
            requires(stack -> stack.hasPermission(1));

        LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("reload").
            executes(ctx ->
            {
                AnimalPen.CONFIG_MANAGER.reloadConfig();

                ctx.getSource().sendSuccess(new TextComponent("Config file reloaded."), true);

                return 1;
            });

        LiteralArgumentBuilder<CommandSourceStack> reset = Commands.literal("reset").
            executes(ctx ->
            {
                AnimalPen.CONFIG_MANAGER.generateConfig();
                ctx.getSource().sendSuccess(new TextComponent("Config file reset."), true);
                return 1;
            });

        dispatcher.register(baseLiteral.then(reset).then(reload));
    }
}
