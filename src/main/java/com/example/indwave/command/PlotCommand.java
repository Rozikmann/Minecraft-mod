package com.example.indwave.command;

import com.example.indwave.IndwavePlotAdapter;
import com.example.indwave.plot.PlotId;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "indwave")
public class PlotCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("plot")
                .executes(PlotCommand::showMenu)
                .then(Commands.literal("generate")
                        .executes(PlotCommand::generatePlot))
                .then(Commands.literal("info")
                        .executes(PlotCommand::showInfo))
                .then(Commands.literal("home")
                        .executes(PlotCommand::teleportHome))
        );
    }

    private static int showMenu(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.literal("§6=== Управление участком ==="));
        context.getSource().sendSystemMessage(Component.literal("§e/plot generate §7— сгенерировать новый участок"));
        context.getSource().sendSystemMessage(Component.literal("§e/plot info §7— информация об участке"));
        context.getSource().sendSystemMessage(Component.literal("§e/plot home §7— телепорт на свой участок"));
        return 1;
    }

    private static int generatePlot(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        int gridStep = 512;
        int currentGx = player.getBlockX() / gridStep;
        int currentGz = player.getBlockZ() / gridStep;

        for (int offset = 1; offset < 20; offset++) {
            int newGx = currentGx + offset;
            int newGz = currentGz;

            int tx = newGx * gridStep + 128;
            int tz = newGz * gridStep + 128;

            player.teleportTo(player.serverLevel(), tx, 66, tz, player.getYRot(), player.getXRot());
            player.sendSystemMessage(Component.literal("§aСгенерирован новый участок. Телепорт..."));
            return 1;
        }

        player.sendSystemMessage(Component.literal("§cНе удалось найти свободное место."));
        return 0;
    }

    private static int showInfo(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        var grid = IndwavePlotAdapter.getInstance().grid;
        PlotId id = grid.plotAt(player.getBlockX(), player.getBlockZ()).orElse(null);

        if (id == null) {
            player.sendSystemMessage(Component.literal("§cВы не на участке."));
            return 0;
        }

        player.sendSystemMessage(Component.literal("§6Участок: §f" + id.gx() + ", " + id.gz()));
        return 1;
    }

    private static int teleportHome(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        player.teleportTo(player.serverLevel(), 128, 66, 128, player.getYRot(), player.getXRot());
        player.sendSystemMessage(Component.literal("§aТелепорт на участок выполнен."));
        return 1;
    }
}