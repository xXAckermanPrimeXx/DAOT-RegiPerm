package com.zenyssevernox.regiperm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.zenyssevernox.regiperm.data.Rank;
import com.zenyssevernox.regiperm.data.RegiPermPlayerData;
import com.zenyssevernox.regiperm.data.Regiment;
import com.zenyssevernox.regiperm.data.RegiPermState;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import com.zenyssevernox.regiperm.data.Squad;
import com.zenyssevernox.regiperm.data.RegiPermState;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.zenyssevernox.regiperm.data.RegiPermPermissions;
import com.zenyssevernox.regiperm.data.ClaimType;
import com.zenyssevernox.regiperm.data.RegiPermClaim;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import static net.minecraft.server.command.CommandManager.argument;
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
                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());

                            context.getSource().sendFeedback(
                                    () -> Text.literal("RegiPerm is working! Lore Active: " + (state.isLoreActive() ? "ON" : "OFF")),
                                    false
                            );

                            return 1;
                        })

                        .then(literal("rank")
                                .requires(RegiPermPermissions::canUseStaffCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .then(argument("rank", StringArgumentType.string())
                                                .then(argument("regiment", StringArgumentType.string())
                                                        .executes(context -> {
                                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                                                            String rankInput = StringArgumentType.getString(context, "rank");
                                                            String regimentInput = StringArgumentType.getString(context, "regiment");

                                                            Rank rank = Rank.fromString(rankInput);
                                                            Regiment regiment = Regiment.fromString(regimentInput);

                                                            if (rank == null) {
                                                                context.getSource().sendError(Text.literal("Unknown rank: " + rankInput));
                                                                return 0;
                                                            }

                                                            if (regiment == null) {
                                                                context.getSource().sendError(Text.literal("Unknown regiment: " + regimentInput));
                                                                return 0;
                                                            }

                                                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                                            RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                                            data.setRank(rank);
                                                            data.setRegiment(regiment);

                                                            state.markDirty();

                                                            context.getSource().sendFeedback(
                                                                    () -> Text.literal("Set " + target.getName().getString()
                                                                            + " to " + rank.getDisplayName()
                                                                            + " of the " + regiment.getDisplayName() + "."),
                                                                    true
                                                            );

                                                            target.sendMessage(
                                                                    Text.literal("You are now "
                                                                                    + rank.getDisplayName()
                                                                                    + " of the "
                                                                                    + regiment.getDisplayName() + ".")
                                                                            .formatted(regiment.getColor()),
                                                                    false
                                                            );

                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(literal("info")
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                            RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("----- RegiPerm Info -----"),
                                                    false
                                            );

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Player: " + target.getName().getString()),
                                                    false
                                            );

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Rank: " + data.getRank().getDisplayName()),
                                                    false
                                            );

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Regiment: " + data.getRegiment().getDisplayName())
                                                            .formatted(data.getRegiment().getColor()),
                                                    false
                                            );

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Squad: " + data.getSquad().getDisplayName()),
                                                    false
                                            );

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Rogue: " + data.isRogue()),
                                                    false
                                            );

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Lore Team: " + data.isLoreTeam()),
                                                    false
                                            );

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Eldia General: " + data.isEldiaGeneral()),
                                                    false
                                            );

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Warnings: " + data.getWarnings()),
                                                    false
                                            );

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Points: " + data.getPoints()),
                                                    false
                                            );

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Regiment Chat: " + data.isRegimentChat()),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )
                        .then(literal("set")
                                .requires(RegiPermPermissions::canUseStaffCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .then(argument("value", StringArgumentType.string())
                                                .executes(context -> {
                                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                                    String valueInput = StringArgumentType.getString(context, "value");

                                                    RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                                    RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                                    Regiment regiment = Regiment.fromString(valueInput);
                                                    Squad squad = Squad.fromString(valueInput);

                                                    if (regiment != null && regiment != Regiment.NONE) {
                                                        data.setRegiment(regiment);
                                                        state.markDirty();

                                                        context.getSource().sendFeedback(
                                                                () -> Text.literal("Set " + target.getName().getString()
                                                                                + "'s regiment to " + regiment.getDisplayName() + ".")
                                                                        .formatted(regiment.getColor()),
                                                                true
                                                        );

                                                        target.sendMessage(
                                                                Text.literal("Your regiment is now " + regiment.getDisplayName() + ".")
                                                                        .formatted(regiment.getColor()),
                                                                false
                                                        );

                                                        return 1;
                                                    }

                                                    if (squad != null && squad != Squad.NONE) {
                                                        if (squad.getRequiredRegiment() != data.getRegiment()) {
                                                            context.getSource().sendError(
                                                                    Text.literal("That player must be in "
                                                                            + squad.getRequiredRegiment().getDisplayName()
                                                                            + " to join " + squad.getDisplayName() + ".")
                                                            );
                                                            return 0;
                                                        }

                                                        data.setSquad(squad);
                                                        state.markDirty();

                                                        context.getSource().sendFeedback(
                                                                () -> Text.literal("Set " + target.getName().getString()
                                                                                + "'s squad to " + squad.getDisplayName() + ".")
                                                                        .formatted(squad.getColor()),
                                                                true
                                                        );

                                                        target.sendMessage(
                                                                Text.literal("Your squad is now " + squad.getDisplayName() + ".")
                                                                        .formatted(squad.getColor()),
                                                                false
                                                        );

                                                        return 1;
                                                    }

                                                    context.getSource().sendError(Text.literal("Unknown regiment or squad: " + valueInput));
                                                    return 0;
                                                })
                                        )
                                )
                        )
                        .then(literal("rogue")
                                .requires(RegiPermPermissions::canUseStaffCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                            RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                            data.setRogue(true);
                                            state.markDirty();

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal(target.getName().getString() + " is now rogue."),
                                                    true
                                            );

                                            target.sendMessage(
                                                    Text.literal("You have been marked as ROGUE.")
                                                            .formatted(net.minecraft.util.Formatting.RED, net.minecraft.util.Formatting.BOLD),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )

                        .then(literal("unrogue")
                                .requires(RegiPermPermissions::canUseStaffCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                            RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                            data.setRogue(false);
                                            state.markDirty();

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal(target.getName().getString() + " is no longer rogue."),
                                                    true
                                            );

                                            target.sendMessage(
                                                    Text.literal("You are no longer marked as rogue.")
                                                            .formatted(net.minecraft.util.Formatting.GREEN),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )
                        .then(literal("warn")
                                .requires(RegiPermPermissions::canUseStaffCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                            RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                            data.addWarning();
                                            state.markDirty();

                                            int warnings = data.getWarnings();

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal(target.getName().getString()
                                                            + " has been warned. Warnings: "
                                                            + warnings + "/3"),
                                                    true
                                            );

                                            target.sendMessage(
                                                    Text.literal("You have been warned. Warnings: " + warnings + "/3")
                                                            .formatted(net.minecraft.util.Formatting.YELLOW),
                                                    false
                                            );

                                            if (warnings >= 3) {
                                                context.getSource().sendFeedback(
                                                        () -> Text.literal(target.getName().getString()
                                                                        + " has reached 3 warnings. Punishment is not set yet.")
                                                                .formatted(net.minecraft.util.Formatting.RED),
                                                        true
                                                );

                                                target.sendMessage(
                                                        Text.literal("You have reached 3 warnings. Punishment is not set yet.")
                                                                .formatted(net.minecraft.util.Formatting.RED),
                                                        false
                                                );
                                            }

                                            return 1;
                                        })
                                )
                        )
                        .then(literal("unwarn")
                                .requires(RegiPermPermissions::canUseStaffCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                            RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                            if (data.getWarnings() <= 0) {
                                                context.getSource().sendError(
                                                        Text.literal(target.getName().getString() + " has no warnings.")
                                                );
                                                return 0;
                                            }

                                            data.removeWarning();
                                            state.markDirty();

                                            int warnings = data.getWarnings();

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal(target.getName().getString()
                                                            + " had one warning removed. Warnings: "
                                                            + warnings + "/3"),
                                                    true
                                            );

                                            target.sendMessage(
                                                    Text.literal("One of your warnings has been removed. Warnings: " + warnings + "/3")
                                                            .formatted(net.minecraft.util.Formatting.GREEN),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )
                        .then(literal("points")
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                            RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal(target.getName().getString()
                                                            + " has " + data.getPoints() + " points."),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )

                        .then(literal("addpoints")
                                .requires(RegiPermPermissions::canUseStaffCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .then(argument("amount", IntegerArgumentType.integer(1))
                                                .executes(context -> {
                                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                                    int amount = IntegerArgumentType.getInteger(context, "amount");

                                                    RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                                    RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                                    data.addPoints(amount);
                                                    state.markDirty();

                                                    context.getSource().sendFeedback(
                                                            () -> Text.literal("Added " + amount + " points to "
                                                                    + target.getName().getString()
                                                                    + ". Total: " + data.getPoints()),
                                                            true
                                                    );

                                                    target.sendMessage(
                                                            Text.literal("You gained " + amount + " points. Total: " + data.getPoints())
                                                                    .formatted(net.minecraft.util.Formatting.GOLD),
                                                            false
                                                    );

                                                    return 1;
                                                })
                                        )
                                )
                        )

                        .then(literal("removepoints")
                                .requires(RegiPermPermissions::canUseStaffCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .then(argument("amount", IntegerArgumentType.integer(1))
                                                .executes(context -> {
                                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                                    int amount = IntegerArgumentType.getInteger(context, "amount");

                                                    RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                                    RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                                    data.removePoints(amount);
                                                    state.markDirty();

                                                    context.getSource().sendFeedback(
                                                            () -> Text.literal("Removed " + amount + " points from "
                                                                    + target.getName().getString()
                                                                    + ". Total: " + data.getPoints()),
                                                            true
                                                    );

                                                    target.sendMessage(
                                                            Text.literal("You lost " + amount + " points. Total: " + data.getPoints())
                                                                    .formatted(net.minecraft.util.Formatting.RED),
                                                            false
                                                    );

                                                    return 1;
                                                })
                                        )
                                )
                        )

                        .then(literal("setpoints")
                                .requires(RegiPermPermissions::canUseStaffCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .then(argument("amount", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                                    int amount = IntegerArgumentType.getInteger(context, "amount");

                                                    RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                                    RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                                    data.setPoints(amount);
                                                    state.markDirty();

                                                    context.getSource().sendFeedback(
                                                            () -> Text.literal("Set " + target.getName().getString()
                                                                    + "'s points to " + data.getPoints() + "."),
                                                            true
                                                    );

                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(literal("regimentchat")
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();

                                    if (player == null) {
                                        context.getSource().sendError(Text.literal("Only players can use this command."));
                                        return 0;
                                    }

                                    RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                    RegiPermPlayerData data = state.getPlayerData(player.getUuid());

                                    if (data.getRegiment() == Regiment.NONE) {
                                        context.getSource().sendError(Text.literal("You are not in a regiment."));
                                        return 0;
                                    }

                                    data.setRegimentChat(!data.isRegimentChat());
                                    state.markDirty();

                                    if (data.isRegimentChat()) {
                                        player.sendMessage(
                                                Text.literal("Regiment chat enabled. Only your regiment can see your messages.")
                                                        .formatted(data.getRegiment().getColor()),
                                                false
                                        );
                                    } else {
                                        player.sendMessage(
                                                Text.literal("Regiment chat disabled. Your messages are now global.")
                                                        .formatted(net.minecraft.util.Formatting.GRAY),
                                                false
                                        );
                                    }

                                    return 1;
                                })
                        )
                        .then(literal("claim")
                                .requires(RegiPermPermissions::canUseOpOnlyCommands)
                                .then(argument("type", StringArgumentType.string())
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayer();

                                            if (player == null) {
                                                context.getSource().sendError(Text.literal("Only players can claim land."));
                                                return 0;
                                            }

                                            String typeInput = StringArgumentType.getString(context, "type");

                                            ClaimType claimType;
                                            Regiment regiment = Regiment.NONE;

                                            if (typeInput.equalsIgnoreCase("civilian")) {
                                                claimType = ClaimType.CIVILIAN;
                                            } else {
                                                Regiment parsedRegiment = Regiment.fromString(typeInput);

                                                if (parsedRegiment == null || parsedRegiment == Regiment.NONE) {
                                                    context.getSource().sendError(Text.literal("Unknown claim type/regiment: " + typeInput));
                                                    return 0;
                                                }

                                                claimType = ClaimType.REGIMENT;
                                                regiment = parsedRegiment;
                                            }

                                            ChunkPos chunkPos = player.getChunkPos();

                                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                            RegiPermClaim claim = state.createClaim(claimType, regiment, chunkPos);

                                            if (claim == null) {
                                                context.getSource().sendError(Text.literal("This chunk is already claimed."));
                                                return 0;
                                            }

                                            if (claimType == ClaimType.CIVILIAN) {
                                                context.getSource().sendFeedback(
                                                        () -> Text.literal("Claimed this chunk as civilian land."),
                                                        true
                                                );
                                            } else {
                                                final Regiment finalRegiment = regiment;

                                                context.getSource().sendFeedback(
                                                        () -> Text.literal("Claimed this chunk for " + finalRegiment.getDisplayName() + ".")
                                                                .formatted(finalRegiment.getColor()),
                                                        true
                                                );
                                            }

                                            return 1;
                                        })
                                )
                        )

                        .then(literal("claiminfo")
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();

                                    if (player == null) {
                                        context.getSource().sendError(Text.literal("Only players can check claim info."));
                                        return 0;
                                    }

                                    RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                    RegiPermClaim claim = state.getClaimAt(player.getChunkPos());

                                    if (claim == null) {
                                        context.getSource().sendFeedback(
                                                () -> Text.literal("This chunk is not claimed."),
                                                false
                                        );
                                        return 1;
                                    }

                                    context.getSource().sendFeedback(
                                            () -> Text.literal("----- Claim Info -----"),
                                            false
                                    );

                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Type: " + claim.getType()),
                                            false
                                    );

                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Regiment: " + claim.getRegiment().getDisplayName())
                                                    .formatted(claim.getRegiment().getColor()),
                                            false
                                    );

                                    context.getSource().sendFeedback(
                                            () -> Text.literal("HQ: " + (claim.isHq() ? "YES" : "NO"))
                                                    .formatted(claim.isHq()
                                                            ? net.minecraft.util.Formatting.GOLD
                                                            : net.minecraft.util.Formatting.GRAY),
                                            false
                                    );

                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Chunks: " + claim.getChunks().size()),
                                            false
                                    );

                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Trusted Players: " + claim.getTrustedPlayers().size()),
                                            false
                                    );

                                    return 1;
                                })
                        )
                        .then(literal("trust")
                                .requires(RegiPermPermissions::canUseStaffCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity executor = context.getSource().getPlayer();
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                                            if (executor == null) {
                                                context.getSource().sendError(Text.literal("Only players can trust players in claims."));
                                                return 0;
                                            }

                                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                            RegiPermClaim claim = state.getClaimAt(executor.getChunkPos());

                                            if (claim == null) {
                                                context.getSource().sendError(Text.literal("You are not standing in a claim."));
                                                return 0;
                                            }

                                            claim.getTrustedPlayers().add(target.getUuid());
                                            state.markDirty();

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Trusted " + target.getName().getString() + " in this claim."),
                                                    true
                                            );

                                            target.sendMessage(
                                                    Text.literal("You have been trusted in a RegiPerm claim.")
                                                            .formatted(net.minecraft.util.Formatting.GREEN),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )
                        .then(literal("untrust")
                                .requires(RegiPermPermissions::canUseStaffCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity executor = context.getSource().getPlayer();
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                                            if (executor == null) {
                                                context.getSource().sendError(Text.literal("Only players can untrust players in claims."));
                                                return 0;
                                            }

                                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                            RegiPermClaim claim = state.getClaimAt(executor.getChunkPos());

                                            if (claim == null) {
                                                context.getSource().sendError(Text.literal("You are not standing in a claim."));
                                                return 0;
                                            }

                                            if (!claim.getTrustedPlayers().remove(target.getUuid())) {
                                                context.getSource().sendError(Text.literal(target.getName().getString() + " is not trusted in this claim."));
                                                return 0;
                                            }

                                            state.markDirty();

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Untrusted " + target.getName().getString() + " from this claim."),
                                                    true
                                            );

                                            target.sendMessage(
                                                    Text.literal("You are no longer trusted in a RegiPerm claim.")
                                                            .formatted(net.minecraft.util.Formatting.RED),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )
                        .then(literal("sethq")
                                .requires(RegiPermPermissions::canUseStaffCommands)
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();

                                    if (player == null) {
                                        context.getSource().sendError(Text.literal("Only players can use this command."));
                                        return 0;
                                    }

                                    RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                    RegiPermClaim claim = state.getClaimAt(player.getChunkPos());

                                    if (claim == null) {
                                        context.getSource().sendError(Text.literal("You are not standing in a claim."));
                                        return 0;
                                    }

                                    claim.setHq(!claim.isHq());
                                    state.markDirty();

                                    if (claim.isHq()) {
                                        context.getSource().sendFeedback(
                                                () -> Text.literal("This claim is now an HQ.")
                                                        .formatted(net.minecraft.util.Formatting.GOLD),
                                                true
                                        );
                                    } else {
                                        context.getSource().sendFeedback(
                                                () -> Text.literal("This claim is no longer an HQ.")
                                                        .formatted(net.minecraft.util.Formatting.GRAY),
                                                true
                                        );
                                    }

                                    return 1;
                                })
                        )
                        .then(literal("wipe")
                                .requires(RegiPermPermissions::canUseOpOnlyCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                            RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                            data.reset();
                                            state.markDirty();

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Wiped RegiPerm data for " + target.getName().getString() + "."),
                                                    true
                                            );

                                            target.sendMessage(
                                                    Text.literal("Your RegiPerm data has been wiped.")
                                                            .formatted(net.minecraft.util.Formatting.RED),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )
                        .then(literal("eldiageneral")
                                .requires(RegiPermPermissions::canUseOpOnlyCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                            RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                            data.setEldiaGeneral(true);
                                            state.markDirty();

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal(target.getName().getString() + " is now an Eldia General.")
                                                            .formatted(net.minecraft.util.Formatting.GOLD),
                                                    true
                                            );

                                            target.sendMessage(
                                                    Text.literal("You are now an Eldia General.")
                                                            .formatted(net.minecraft.util.Formatting.GOLD),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )

                        .then(literal("removeeldiageneral")
                                .requires(RegiPermPermissions::canUseOpOnlyCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                            RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                            data.setEldiaGeneral(false);
                                            state.markDirty();

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal(target.getName().getString() + " is no longer an Eldia General."),
                                                    true
                                            );

                                            target.sendMessage(
                                                    Text.literal("You are no longer an Eldia General.")
                                                            .formatted(net.minecraft.util.Formatting.GRAY),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )
                        .then(literal("loreteam")
                                .requires(RegiPermPermissions::canUseOpOnlyCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                            RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                            data.setLoreTeam(true);
                                            state.markDirty();

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal(target.getName().getString() + " is now Lore Team.")
                                                            .formatted(net.minecraft.util.Formatting.LIGHT_PURPLE),
                                                    true
                                            );

                                            target.sendMessage(
                                                    Text.literal("You are now Lore Team.")
                                                            .formatted(net.minecraft.util.Formatting.LIGHT_PURPLE),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )

                        .then(literal("removeloreteam")
                                .requires(RegiPermPermissions::canUseOpOnlyCommands)
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

                                            RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                            RegiPermPlayerData data = state.getPlayerData(target.getUuid());

                                            data.setLoreTeam(false);
                                            data.setLoreMode(false);
                                            state.markDirty();

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal(target.getName().getString() + " is no longer Lore Team."),
                                                    true
                                            );

                                            target.sendMessage(
                                                    Text.literal("You are no longer Lore Team.")
                                                            .formatted(net.minecraft.util.Formatting.GRAY),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )
                        .then(literal("lore")
                                .requires(RegiPermPermissions::canUseLoreMode)
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();

                                    if (player == null) {
                                        context.getSource().sendError(Text.literal("Only players can use lore mode."));
                                        return 0;
                                    }

                                    RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());
                                    RegiPermPlayerData data = state.getPlayerData(player.getUuid());

                                    boolean newLoreMode = !data.isLoreMode();
                                    data.setLoreMode(newLoreMode);
                                    state.markDirty();

                                    if (newLoreMode) {
                                        player.addStatusEffect(new StatusEffectInstance(
                                                StatusEffects.INVISIBILITY,
                                                -1,
                                                0,
                                                false,
                                                false,
                                                false
                                        ));

                                        player.getAbilities().allowFlying = true;
                                        player.getAbilities().flying = true;
                                        player.sendAbilitiesUpdate();

                                        player.sendMessage(
                                                Text.literal("Lore Mode enabled. You are invisible, can fly, and cannot take damage.")
                                                        .formatted(net.minecraft.util.Formatting.LIGHT_PURPLE),
                                                false
                                        );
                                    } else {
                                        player.removeStatusEffect(StatusEffects.INVISIBILITY);

                                        player.getAbilities().flying = false;
                                        player.getAbilities().allowFlying = false;
                                        player.sendAbilitiesUpdate();

                                        player.sendMessage(
                                                Text.literal("Lore Mode disabled.")
                                                        .formatted(net.minecraft.util.Formatting.GRAY),
                                                false
                                        );
                                    }

                                    return 1;
                                })
                        )
                        .then(literal("loreactive")
                                .requires(RegiPermPermissions::canUseStaffCommands)
                                .executes(context -> {
                                    RegiPermState state = RegiPermState.getServerState(context.getSource().getServer());

                                    state.setLoreActive(!state.isLoreActive());

                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Lore Active is now " + (state.isLoreActive() ? "ON" : "OFF"))
                                                    .formatted(state.isLoreActive()
                                                            ? net.minecraft.util.Formatting.LIGHT_PURPLE
                                                            : net.minecraft.util.Formatting.GRAY),
                                            true
                                    );

                                    return 1;
                                })
                        )
        );
    }
}