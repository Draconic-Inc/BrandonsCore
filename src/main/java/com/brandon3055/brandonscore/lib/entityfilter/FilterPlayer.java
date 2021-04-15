package com.brandon3055.brandonscore.lib.entityfilter;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;

import java.util.regex.Pattern;

/**
 * Created by brandon3055 on 7/11/19.
 */
public class FilterPlayer extends FilterBase {

    public static Pattern namePattern = Pattern.compile("^[\\w]*$");
    protected boolean whitelistPlayers = true;
    private String playerName = "";
    private String playerUUID = ""; //This will be handled automatically behind the scenes

    public FilterPlayer(EntityFilter filter) {
        super(filter);
    }

    public void setWhitelistPlayers(boolean whitelistPlayers) {
        boolean prev = this.whitelistPlayers;
        this.whitelistPlayers = whitelistPlayers;
        getFilter().nodeModified(this);
        this.whitelistPlayers = prev;
    }

    public void setPlayerName(String playerName) {
//        String prev = this.playerName;//This needs to be set real time to avoid breaking the text field
        this.playerName = playerName;
        this.playerUUID = ""; //No need to restore this client side because the client does not care.
        getFilter().nodeModified(this);
//        this.playerName = prev;
    }

    public boolean isWhitelistPlayers() {
        return whitelistPlayers;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public boolean test(Entity entity) {
        boolean isPlayer = entity instanceof PlayerEntity;
        if (isPlayer) {
            if (playerName.isEmpty()) {
                return whitelistPlayers;
            }
            else {
                return isPlayerMatch((PlayerEntity) entity) == whitelistPlayers;
            }
        }
        else {
            return !whitelistPlayers;
        }
    }

    private boolean isPlayerMatch(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity)) {
            return player.getGameProfile().getName().equalsIgnoreCase(playerName);
        }
        else if (!playerUUID.isEmpty()) {
            return player.getUUID().toString().equals(playerUUID);
        }
        MinecraftServer server = player.getServer();
        if (server != null){
            GameProfile profile = server.getProfileCache().get(playerName);
            if (profile != null) {
                playerUUID = profile.getId().toString();
                return player.getUUID().toString().equals(playerUUID);
            }
        }
        return player.getGameProfile().getName().equalsIgnoreCase(playerName);
    }

    @Override
    public FilterType getType() {
        return FilterType.PLAYER;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compound = super.serializeNBT();
        compound.putBoolean("include", whitelistPlayers);
        compound.putString("name", playerName);
        compound.putString("uuid", playerUUID);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        whitelistPlayers = nbt.getBoolean("include");
        playerName = nbt.getString("name");
        playerUUID = nbt.getString("uuid");
    }

    @Override
    public void serializeMCD(MCDataOutput output) {
        super.serializeMCD(output);
        output.writeBoolean(whitelistPlayers);
        output.writeString(playerName);
        output.writeString(playerUUID);
    }

    @Override
    public void deSerializeMCD(MCDataInput input) {
        super.deSerializeMCD(input);
        whitelistPlayers = input.readBoolean();
        playerName = input.readString();
        playerUUID = input.readString();
        if (!namePattern.matcher(playerName).find()) {
            playerName = ""; //A client probably just tried to screw with us
        }
    }
}
