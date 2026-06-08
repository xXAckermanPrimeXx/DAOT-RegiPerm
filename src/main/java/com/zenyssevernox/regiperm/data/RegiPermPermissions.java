package com.zenyssevernox.regiperm.data;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class RegiPermPermissions {

    public static boolean isOp(ServerCommandSource source) {
        return source.hasPermissionLevel(2);
    }

    public static boolean canUseStaffCommands(ServerCommandSource source) {
        if (isOp(source)) {
            return true;
        }

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            return false;
        }

        RegiPermState state = RegiPermState.getServerState(source.getServer());
        RegiPermPlayerData data = state.getPlayerData(player.getUuid());

        return data.isLoreTeam()
                || data.isEldiaGeneral()
                || data.getRank() == Rank.COMMANDER
                || data.getRank() == Rank.SECTION_COMMANDER;
    }

    public static boolean canUseOpOnlyCommands(ServerCommandSource source) {
        return isOp(source);
    }

    public static boolean canUseAllRegimentCommands(ServerCommandSource source) {
        if (isOp(source)) {
            return true;
        }

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            return false;
        }

        RegiPermState state = RegiPermState.getServerState(source.getServer());
        RegiPermPlayerData data = state.getPlayerData(player.getUuid());

        return data.isLoreTeam()
                || data.isEldiaGeneral()
                || data.getRank() == Rank.COMMANDER
                || data.getRank() == Rank.SECTION_COMMANDER;
    }

    public static boolean canUseLoreMode(ServerCommandSource source) {
        if (isOp(source)) {
            return true;
        }

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            return false;
        }

        RegiPermState state = RegiPermState.getServerState(source.getServer());
        RegiPermPlayerData data = state.getPlayerData(player.getUuid());

        return data.isLoreTeam();
    }
}