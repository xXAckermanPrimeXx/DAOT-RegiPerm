package com.zenyssevernox.regiperm.event;

import com.zenyssevernox.regiperm.data.RegiPermPlayerData;
import com.zenyssevernox.regiperm.data.RegiPermState;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class RegiPermLoreEvents {

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return true;
            }

            RegiPermState state = RegiPermState.getServerState(player.getServer());
            RegiPermPlayerData data = state.getPlayerData(player.getUuid());

            return !data.isLoreMode();
        });
    }
}