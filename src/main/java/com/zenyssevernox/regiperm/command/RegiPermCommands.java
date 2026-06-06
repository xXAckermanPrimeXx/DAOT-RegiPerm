package com.zenyssevernox.regiperm.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class RegiPermCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerRegiPermCommand(dispatcher);
        });
    }

    private static void registerRegiPermCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("regiperm")
                        .executes(context -> {
                            context.getSource().sendFeedback(
                                    () -> net.minecraft.text.Text.literal("RegiPerm is working!"),
                                    false
                            );
                            return 1;
                        })
        );
    }
}
