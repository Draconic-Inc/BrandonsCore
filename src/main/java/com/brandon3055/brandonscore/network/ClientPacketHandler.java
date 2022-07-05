package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.client.gui.GuiPlayerAccess;
import com.brandon3055.brandonscore.client.gui.HudConfigGui;
import com.brandon3055.brandonscore.handlers.BCEventHandler;
import com.brandon3055.brandonscore.lib.datamanager.IDataManagerProvider;
import com.brandon3055.brandonscore.multiblock.MultiBlockManager;
import com.mojang.math.Vector3f;
import net.covers1624.quack.util.SneakyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ClientPacketHandler implements ICustomPacketHandler.IClientPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, ClientPacketListener handler) {
        switch (packet.getType()) {
            case BCoreNetwork.C_TILE_DATA_MANAGER: {
                BlockPos pos = packet.readPos();
                BlockEntity tile = mc.level.getBlockEntity(pos);
                if (tile instanceof IDataManagerProvider) {
                    ((IDataManagerProvider) tile).getDataManager().receiveSyncData(packet);
                }
                break;
            }
            case BCoreNetwork.C_TILE_MESSAGE: {
                BlockPos pos = packet.readPos();
                BlockEntity tile = mc.level.getBlockEntity(pos);
                if (tile instanceof TileBCore) {
                    int id = packet.readByte() & 0xFF;
                    ((TileBCore) tile).receivePacketFromServer(packet, id);
                }
                break;
            }
            case BCoreNetwork.C_SERVER_CONFIG_SYNC:
//                ModConfigParser.readConfigForSync(packet);
                break;
            case BCoreNetwork.C_NO_CLIP:
                boolean enable = packet.readBoolean();
                if (enable) {
                    BCEventHandler.noClipPlayers.add(mc.player.getUUID());
                } else {
                    BCEventHandler.noClipPlayers.add(mc.player.getUUID());
                }
                break;
            case BCoreNetwork.C_PLAYER_ACCESS:
                //I dont think i need this any more?
//                int windowID = packet.readInt();
//                GuiPlayerAccess guiPlayerAccess = new GuiPlayerAccess(mc.player);
//                mc.displayGuiScreen(guiPlayerAccess);
//                mc.player.openContainer.windowId = windowID;
                break;
            case BCoreNetwork.C_PLAYER_ACCESS_UPDATE:
                GuiPlayerAccess gui = mc.screen instanceof GuiPlayerAccess ? (GuiPlayerAccess) mc.screen : null;
                if (gui != null) {
                    gui.name = packet.readString();
                    gui.pos = packet.readPos();
                    gui.dimension = packet.readInt();
                }
                break;
            case BCoreNetwork.C_INDEXED_MESSAGE:
                BrandonsCore.proxy.sendIndexedMessage(mc.player, packet.readTextComponent(), packet.readInt());
                break;
            case BCoreNetwork.C_TILE_CAP_DATA:
                BlockPos pos = packet.readPos();
                BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
                if (tile instanceof TileBCore) {
                    ((TileBCore) tile).getCapManager().receiveCapSyncData(packet);
                }
                break;
            case BCoreNetwork.C_PLAY_SOUND:
                handlePlaySound(packet, mc);
                break;
            case BCoreNetwork.C_SPAWN_ENTITY:
                handleEntitySpawn(packet, mc);
                break;
            case BCoreNetwork.C_SPAWN_PARTICLE:
                handleParticleSpawn(packet, mc);
                break;
            case BCoreNetwork.C_ENTITY_VELOCITY:
                handleEntityVelocity(packet, mc);
                break;
            case BCoreNetwork.C_OPEN_HUD_CONFIG:
                mc.setScreen(new HudConfigGui());
                break;
            case BCoreNetwork.C_MULTI_BLOCK_DEFINITIONS:
                MultiBlockManager.receiveDefinitionsFromServer(packet);
                break;
        }
    }

    private static void handlePlaySound(PacketCustom packet, Minecraft mc) {
        if (mc.level == null) return;
        BlockPos pos = packet.readPos();
        SoundEvent sound = packet.readRegistryId();
        SoundSource category = SoundSource.values()[packet.readVarInt()];
        float volume = packet.readFloat();
        float pitch = packet.readFloat();
        boolean distanceDelay = packet.readBoolean();
        mc.level.playLocalSound(pos, sound, category, volume, pitch, distanceDelay);
    }

    private static void handleEntitySpawn(PacketCustom packet, Minecraft mc) {
        if (mc.level == null) {
            return;
        }
        EntityType<?> type = Registry.ENTITY_TYPE.byId(packet.readVarInt());
        int entityID = packet.readInt();
        UUID uuid = packet.readUUID();
        double posX = packet.readDouble();
        double posY = packet.readDouble();
        double posZ = packet.readDouble();
        byte yaw = packet.readByte();
        byte pitch = packet.readByte();
        byte headYaw = packet.readByte();
        Vec3 velocity = new Vec3(packet.readFloat(), packet.readFloat(), packet.readFloat());
        Entity entity = type.create(mc.level);
        if (entity == null) {
            return;
        }

        entity.setPacketCoordinates(posX, posY, posZ);
        entity.absMoveTo(posX, posY, posZ, (pitch * 360) / 256.0F, (yaw * 360) / 256.0F);
        entity.setYHeadRot((headYaw * 360) / 256.0F);
        entity.setYBodyRot((headYaw * 360) / 256.0F);
        entity.setId(entityID);
        entity.setUUID(uuid);
        mc.level.putNonPlayerEntity(entityID, entity);
        entity.lerpMotion(velocity.x, velocity.y, velocity.z);
    }

    private static void handleEntityVelocity(PacketCustom packet, Minecraft mc) {
        if (mc.level == null) {
            return;
        }
        int entityID = packet.readInt();
        Entity entity = mc.level.getEntity(entityID);
        if (entity != null) {
            Vector3f motion = packet.readVec3f();
            entity.lerpMotion(motion.x(), motion.y(), motion.z());
            if (packet.readBoolean()) {
                entity.setXRot(packet.readFloat());
                entity.setYRot(packet.readFloat());
                entity.setOnGround(packet.readBoolean());
            }
        }
    }

    private static void handleParticleSpawn(PacketCustom packet, Minecraft mc) {
        if (mc.level == null) {
            return;
        }
        ParticleType<?> type = packet.readRegistryId();
        ParticleOptions data = type.getDeserializer().fromNetwork(SneakyUtils.unsafeCast(type), packet.toPacketBuffer());
        Vector3 pos = packet.readVector();
        Vector3 motion = packet.readVector();
        boolean distanceOverride = packet.readBoolean();
        ;
        mc.level.addParticle(data, distanceOverride, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
    }
}