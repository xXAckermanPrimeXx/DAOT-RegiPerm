package com.zenyssevernox.regiperm.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RegiPermClaim {
    private final UUID id;
    private ClaimType type;
    private Regiment regiment;
    private boolean hq;
    private final Set<ChunkPos> chunks = new HashSet<>();
    private final Set<UUID> trustedPlayers = new HashSet<>();

    public RegiPermClaim(UUID id, ClaimType type, Regiment regiment) {
        this.id = id;
        this.type = type;
        this.regiment = regiment;
    }

    public static RegiPermClaim fromNbt(NbtCompound nbt) {
        UUID id = nbt.getUuid("id");

        ClaimType type = ClaimType.valueOf(nbt.getString("type"));

        Regiment regiment = Regiment.fromString(nbt.getString("regiment"));
        if (regiment == null) {
            regiment = Regiment.NONE;
        }

        RegiPermClaim claim = new RegiPermClaim(id, type, regiment);
        claim.hq = nbt.getBoolean("hq");

        NbtList chunkList = nbt.getList("chunks", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < chunkList.size(); i++) {
            NbtCompound chunkNbt = chunkList.getCompound(i);
            claim.chunks.add(new ChunkPos(chunkNbt.getInt("x"), chunkNbt.getInt("z")));
        }

        NbtList trustedList = nbt.getList("trustedPlayers", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < trustedList.size(); i++) {
            NbtCompound trustedNbt = trustedList.getCompound(i);
            claim.trustedPlayers.add(trustedNbt.getUuid("uuid"));
        }

        return claim;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();

        nbt.putUuid("id", id);
        nbt.putString("type", type.name());
        nbt.putString("regiment", regiment.name());
        nbt.putBoolean("hq", hq);

        NbtList chunkList = new NbtList();
        for (ChunkPos chunk : chunks) {
            NbtCompound chunkNbt = new NbtCompound();
            chunkNbt.putInt("x", chunk.x);
            chunkNbt.putInt("z", chunk.z);
            chunkList.add(chunkNbt);
        }
        nbt.put("chunks", chunkList);

        NbtList trustedList = new NbtList();
        for (UUID trustedPlayer : trustedPlayers) {
            NbtCompound trustedNbt = new NbtCompound();
            trustedNbt.putUuid("uuid", trustedPlayer);
            trustedList.add(trustedNbt);
        }
        nbt.put("trustedPlayers", trustedList);

        return nbt;
    }

    public UUID getId() {
        return id;
    }

    public ClaimType getType() {
        return type;
    }

    public Regiment getRegiment() {
        return regiment;
    }

    public boolean isHq() {
        return hq;
    }

    public void setHq(boolean hq) {
        this.hq = hq;
    }

    public Set<ChunkPos> getChunks() {
        return chunks;
    }

    public Set<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }

    public boolean containsChunk(ChunkPos pos) {
        return chunks.contains(pos);
    }

    public void addChunk(ChunkPos pos) {
        chunks.add(pos);
    }
}