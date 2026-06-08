package com.zenyssevernox.regiperm.event;

import com.zenyssevernox.regiperm.data.ClaimType;
import com.zenyssevernox.regiperm.data.RegiPermClaim;
import com.zenyssevernox.regiperm.data.RegiPermPlayerData;
import com.zenyssevernox.regiperm.data.RegiPermState;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.ChunkPos;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.BlockItem;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class RegiPermClaimEvents {

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient()) {
                return true;
            }

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return true;
            }

            /* if (serverPlayer.hasPermissionLevel(2)) {
                return true;
            }

             */

            RegiPermState regiState = RegiPermState.getServerState(serverPlayer.getServer());
            RegiPermClaim claim = regiState.getClaimAt(new ChunkPos(pos));

            if (claim == null) {
                return true;
            }

            if (canBuildInClaim(serverPlayer, claim, regiState)) {
                return true;
            }

            serverPlayer.sendMessage(Text.literal("You cannot break blocks in this claim."), true);
            return false;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

           /*  if (serverPlayer.hasPermissionLevel(2)) {
                return ActionResult.PASS;
            }

            */

            RegiPermState regiState = RegiPermState.getServerState(serverPlayer.getServer());
            RegiPermClaim claim = regiState.getClaimAt(new ChunkPos(hitResult.getBlockPos()));

            if (claim == null) {
                return ActionResult.PASS;
            }

            if (canBuildInClaim(serverPlayer, claim, regiState)) {
                return ActionResult.PASS;
            }

            serverPlayer.sendMessage(Text.literal("You cannot use/place blocks in this claim."), true);
            return ActionResult.FAIL;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }

            // Keep this commented while testing if you want OPs to be blocked too.
             /*
                if (serverPlayer.hasPermissionLevel(2)) {
                 return TypedActionResult.pass(player.getStackInHand(hand));
                }
            */

            if (!(player.getStackInHand(hand).getItem() instanceof BlockItem)) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }

            HitResult hitResult = player.raycast(5.0D, 0.0F, false);

            if (!(hitResult instanceof BlockHitResult blockHitResult)) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }

            RegiPermState regiState = RegiPermState.getServerState(serverPlayer.getServer());
            RegiPermClaim claim = regiState.getClaimAt(new ChunkPos(blockHitResult.getBlockPos()));

            if (claim == null) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }

            if (canBuildInClaim(serverPlayer, claim, regiState)) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }

            serverPlayer.sendMessage(Text.literal("You cannot place blocks in this claim."), true);
            return TypedActionResult.fail(player.getStackInHand(hand));
        });
    }

    private static boolean canBuildInClaim(ServerPlayerEntity player, RegiPermClaim claim, RegiPermState state) {
        RegiPermPlayerData data = state.getPlayerData(player.getUuid());

        if (claim.getTrustedPlayers().contains(player.getUuid())) {
            return true;
        }

        if (claim.getType() == ClaimType.REGIMENT) {
            return data.getRegiment() == claim.getRegiment() && !data.isRogue();
        }

        if (claim.getType() == ClaimType.CIVILIAN) {
            return false;
        }

        return false;
    }
}