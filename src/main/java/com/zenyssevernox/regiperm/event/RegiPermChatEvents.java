package com.zenyssevernox.regiperm.event;

import com.zenyssevernox.regiperm.data.RegiPermPlayerData;
import com.zenyssevernox.regiperm.data.RegiPermState;
import com.zenyssevernox.regiperm.data.Regiment;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class RegiPermChatEvents {

    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            MinecraftServer server = sender.getServer();

            RegiPermState state = RegiPermState.getServerState(server);
            RegiPermPlayerData senderData = state.getPlayerData(sender.getUuid());

            if (!senderData.isRegimentChat()) {
                return true;
            }

            Regiment senderRegiment = senderData.getRegiment();

            if (senderRegiment == Regiment.NONE) {
                sender.sendMessage(Text.literal("You are not in a regiment, so regiment chat was disabled."), false);
                senderData.setRegimentChat(false);
                state.markDirty();
                return false;
            }

            String rawMessage = message.getContent().getString();

            Text regimentMessage = Text.literal("[Regiment] ")
                    .formatted(senderRegiment.getColor())
                    .append(Text.literal(sender.getName().getString() + ": ")
                            .formatted(senderRegiment.getColor()))
                    .append(Text.literal(rawMessage));

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                RegiPermPlayerData receiverData = state.getPlayerData(player.getUuid());

                if (receiverData.getRegiment() == senderRegiment) {
                    player.sendMessage(regimentMessage, false);
                }
            }

            return false;
        });
    }
}