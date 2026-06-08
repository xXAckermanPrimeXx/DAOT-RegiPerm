package com.zenyssevernox.regiperm.data;

import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegiPermPlayerData {
    private static final Map<UUID, RegiPermPlayerData> PLAYER_DATA = new HashMap<>();

    private Rank rank = Rank.RECRUIT;
    private Regiment regiment = Regiment.NONE;
    private Squad squad = Squad.NONE;

    private boolean loreTeam = false;
    private boolean eldiaGeneral = false;
    private boolean rogue = false;
    private boolean regimentChat = false;
    private boolean loreMode = false;

    private int warnings = 0;
    private int points = 0;

    public static RegiPermPlayerData get(UUID uuid) {
        return PLAYER_DATA.computeIfAbsent(uuid, id -> new RegiPermPlayerData());
    }

    public static RegiPermPlayerData fromNbt(NbtCompound nbt) {
        RegiPermPlayerData data = new RegiPermPlayerData();

        data.rank = Rank.fromString(nbt.getString("rank"));
        if (data.rank == null) data.rank = Rank.RECRUIT;

        data.regiment = Regiment.fromString(nbt.getString("regiment"));
        if (data.regiment == null) data.regiment = Regiment.NONE;

        data.squad = Squad.fromString(nbt.getString("squad"));
        if (data.squad == null) data.squad = Squad.NONE;

        data.loreTeam = nbt.getBoolean("loreTeam");
        data.eldiaGeneral = nbt.getBoolean("eldiaGeneral");
        data.rogue = nbt.getBoolean("rogue");
        data.regimentChat = nbt.getBoolean("regimentChat");
        data.loreMode = nbt.getBoolean("loreMode");
        data.warnings = nbt.getInt("warnings");
        data.points = nbt.getInt("points");

        return data;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();

        nbt.putString("rank", rank.name());
        nbt.putString("regiment", regiment.name());
        nbt.putString("squad", squad.name());

        nbt.putBoolean("loreTeam", loreTeam);
        nbt.putBoolean("eldiaGeneral", eldiaGeneral);
        nbt.putBoolean("rogue", rogue);
        nbt.putBoolean("regimentChat", regimentChat);
        nbt.putBoolean("loreMode", loreMode);

        nbt.putInt("warnings", warnings);
        nbt.putInt("points", points);

        return nbt;
    }

    public Rank getRank() {
        return rank;
    }

    public Regiment getRegiment() {
        return regiment;
    }

    public Squad getSquad() {
        return squad;
    }

    public boolean isLoreTeam() {
        return loreTeam;
    }

    public boolean isRogue() {
        return rogue;
    }

    public boolean isRegimentChat() {
        return regimentChat;
    }

    public boolean isLoreMode() {
        return loreMode;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public void setRegiment(Regiment regiment) {
        this.regiment = regiment;

        if (squad != Squad.NONE && squad.getRequiredRegiment() != regiment) {
            squad = Squad.NONE;
        }
    }

    public void setSquad(Squad squad) {
        if (squad == Squad.NONE || squad.getRequiredRegiment() == this.regiment) {
            this.squad = squad;
        }
    }

    public void setLoreTeam(boolean loreTeam) {
        this.loreTeam = loreTeam;
    }

    public void setRogue(boolean rogue) {
        this.rogue = rogue;

        if (rogue) {
            this.regiment = Regiment.NONE;
            this.squad = Squad.NONE;
            this.rank = Rank.RECRUIT;
        }
    }

    public void setRegimentChat(boolean regimentChat) {
        this.regimentChat = regimentChat;
    }

    public void setLoreMode(boolean loreMode) {
        this.loreMode = loreMode;
    }

    public int getWarnings() {
        return warnings;
    }

    public void addWarning() {
        warnings++;
    }

    public void clearWarnings() {
        warnings = 0;
    }

    public void removeWarning() {
        if (warnings > 0) {
            warnings--;
        }
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int amount) {
        points += Math.max(0, amount);
    }

    public void removePoints(int amount) {
        points -= Math.max(0, amount);

        if (points < 0) {
            points = 0;
        }
    }

    public void setPoints(int points) {
        this.points = Math.max(0, points);
    }

    public void reset() {
        rank = Rank.RECRUIT;
        regiment = Regiment.NONE;
        squad = Squad.NONE;

        loreTeam = false;
        eldiaGeneral = false;
        rogue = false;
        regimentChat = false;
        loreMode = false;

        warnings = 0;
        points = 0;
    }

    public boolean isEldiaGeneral() {
        return eldiaGeneral;
    }

    public void setEldiaGeneral(boolean eldiaGeneral) {
        this.eldiaGeneral = eldiaGeneral;
    }
}