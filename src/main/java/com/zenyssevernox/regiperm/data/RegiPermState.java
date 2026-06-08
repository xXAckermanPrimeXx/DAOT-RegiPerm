package com.zenyssevernox.regiperm.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegiPermState extends PersistentState {
    private final Map<UUID, RegiPermPlayerData> players = new HashMap<>();
    private final Map<UUID, RegiPermClaim> claims = new HashMap<>();

    public static RegiPermState getServerState(MinecraftServer server) {
        PersistentStateManager stateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        return stateManager.getOrCreate(
                new Type<>(
                        RegiPermState::new,
                        RegiPermState::createFromNbt,
                        null
                ),
                "regiperm"
        );
    }

    public RegiPermPlayerData getPlayerData(UUID uuid) {
        return players.computeIfAbsent(uuid, id -> new RegiPermPlayerData());
    }

    public static RegiPermState createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        RegiPermState state = new RegiPermState();

        NbtList playerList = nbt.getList("players", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < playerList.size(); i++) {
            NbtCompound playerNbt = playerList.getCompound(i);

            UUID uuid = playerNbt.getUuid("uuid");
            RegiPermPlayerData data = RegiPermPlayerData.fromNbt(playerNbt);

            state.players.put(uuid, data);
        }

        NbtList claimList = nbt.getList("claims", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < claimList.size(); i++) {
            NbtCompound claimNbt = claimList.getCompound(i);
            RegiPermClaim claim = RegiPermClaim.fromNbt(claimNbt);
            state.claims.put(claim.getId(), claim);
        }

        state.loreActive = nbt.getBoolean("loreActive");

        return state;
    }



    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList playerList = new NbtList();

        for (Map.Entry<UUID, RegiPermPlayerData> entry : players.entrySet()) {
            NbtCompound playerNbt = entry.getValue().toNbt();
            playerNbt.putUuid("uuid", entry.getKey());
            playerList.add(playerNbt);
        }

        nbt.put("players", playerList);
        NbtList claimList = new NbtList();

        for (RegiPermClaim claim : claims.values()) {
            claimList.add(claim.toNbt());
        }

        nbt.put("claims", claimList);

        nbt.putBoolean("loreActive", loreActive);

        return nbt;
    }

    public RegiPermClaim getClaimAt(ChunkPos chunkPos) {
        for (RegiPermClaim claim : claims.values()) {
            if (claim.containsChunk(chunkPos)) {
                return claim;
            }
        }

        return null;
    }

    public RegiPermClaim createClaim(ClaimType type, Regiment regiment, ChunkPos chunkPos) {
        RegiPermClaim existing = getClaimAt(chunkPos);

        if (existing != null) {
            return null;
        }

        RegiPermClaim newClaim = new RegiPermClaim(UUID.randomUUID(), type, regiment);
        newClaim.addChunk(chunkPos);

        RegiPermClaim north = getClaimAt(new ChunkPos(chunkPos.x, chunkPos.z - 1));
        RegiPermClaim south = getClaimAt(new ChunkPos(chunkPos.x, chunkPos.z + 1));
        RegiPermClaim east = getClaimAt(new ChunkPos(chunkPos.x + 1, chunkPos.z));
        RegiPermClaim west = getClaimAt(new ChunkPos(chunkPos.x - 1, chunkPos.z));

        RegiPermClaim mainClaim = newClaim;

        for (RegiPermClaim nearbyClaim : new RegiPermClaim[]{north, south, east, west}) {
            if (nearbyClaim == null) {
                continue;
            }

            if (!canClaimsMerge(mainClaim, nearbyClaim)) {
                continue;
            }

            nearbyClaim.getChunks().addAll(mainClaim.getChunks());
            nearbyClaim.getTrustedPlayers().addAll(mainClaim.getTrustedPlayers());

            if (mainClaim.isHq()) {
                nearbyClaim.setHq(true);
            }

            mainClaim = nearbyClaim;
        }

        for (RegiPermClaim nearbyClaim : new RegiPermClaim[]{north, south, east, west}) {
            if (nearbyClaim == null || nearbyClaim == mainClaim) {
                continue;
            }

            if (!canClaimsMerge(mainClaim, nearbyClaim)) {
                continue;
            }

            mainClaim.getChunks().addAll(nearbyClaim.getChunks());
            mainClaim.getTrustedPlayers().addAll(nearbyClaim.getTrustedPlayers());

            if (nearbyClaim.isHq()) {
                mainClaim.setHq(true);
            }

            claims.remove(nearbyClaim.getId());
        }

        claims.put(mainClaim.getId(), mainClaim);
        markDirty();

        return mainClaim;
    }

    private boolean canClaimsMerge(RegiPermClaim a, RegiPermClaim b) {
        if (a.getType() != b.getType()) {
            return false;
        }

        if (a.getType() == ClaimType.REGIMENT) {
            return a.getRegiment() == b.getRegiment();
        }

        return a.getType() == ClaimType.CIVILIAN;
    }

    private boolean loreActive = false;

    public boolean isLoreActive() {
        return loreActive;
    }

    public void setLoreActive(boolean loreActive) {
        this.loreActive = loreActive;
        markDirty();
    }
}