package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.packet.PacketCustomChannelBuilder;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.event.EventNetworkChannel;

/**
 * Created by brandon3055 on 17/12/19.
 */
public class BCoreNetwork {

    public static final ResourceLocation CHANNEL = new ResourceLocation(BrandonsCore.MODID + ":network");
    public static EventNetworkChannel netChannel;

    //Server to client
    public static final int C_TILE_DATA_MANAGER = 1;
    public static final int C_TILE_MESSAGE = 2;
    public static final int C_SERVER_CONFIG_SYNC = 3;
    public static final int C_NO_CLIP = 4;
    public static final int C_PLAYER_ACCESS = 5;
    public static final int C_PLAYER_ACCESS_UPDATE = 6;
    public static final int C_INDEXED_MESSAGE = 7;
    public static final int C_TILE_CAP_DATA = 8;
    public static final int C_PLAY_SOUND = 9;
    public static final int C_SPAWN_ENTITY = 10;
    public static final int C_SPAWN_PARTICLE = 11;
    public static final int C_ENTITY_VELOCITY = 12;
    //Client to server
    public static final int S_TILE_MESSAGE = 1;
    public static final int S_PLAYER_ACCESS_BUTTON = 2;
    public static final int S_TILE_DATA_MANAGER = 3;


    public static void sendConfigToClient(ServerPlayerEntity player) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_SERVER_CONFIG_SYNC);
//        ModConfigParser.writeConfigForSync(packet);
        packet.sendToPlayer(player);
        LogHelperBC.dev("Sending Config To Client: " + player);
    }

    public static void sendNoClip(ServerPlayerEntity player, boolean enabled) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_SERVER_CONFIG_SYNC);
        packet.writeBoolean(enabled);
        packet.sendToPlayer(player);
        LogHelperBC.dev("Sending NoClip update to player: " + player + " Enabled: " + enabled);
    }

    public static void sendOpenPlayerAccessUI(ServerPlayerEntity player, int windowID) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_PLAYER_ACCESS);
        packet.writeInt(windowID);
        packet.sendToPlayer(player);
    }

    public static void sendPlayerAccessUIUpdate(ServerPlayerEntity player, PlayerEntity target) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_PLAYER_ACCESS_UPDATE);
        packet.writeString(target.getGameProfile().getName());
        packet.writePos(target.blockPosition());
//        packet.writeInt(target.dimension.getId());
//        packet.sendToPlayer(player);
    }

    public static void sendPlayerAccessButton(int button) {
        PacketCustom packet = new PacketCustom(CHANNEL, S_PLAYER_ACCESS_BUTTON);
        packet.writeByte(button);
        packet.sendToServer();
    }

    public static void sendIndexedMessage(ServerPlayerEntity player, ITextComponent message, int index) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_INDEXED_MESSAGE);
        packet.writeTextComponent(message);
        packet.writeInt(index);
        packet.sendToPlayer(player);
    }

    public static void sendSound(World world, int x, int y, int z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
        sendSound(world, new BlockPos(x, y, z), sound, category, volume, pitch, distanceDelay);
    }

    public static void sendSound(World world, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
        sendSound(world, entity.blockPosition(), sound, category, volume, pitch, distanceDelay);
    }

    public static void sendSound(World world, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
        if (!world.isClientSide) {
            PacketCustom packet = new PacketCustom(CHANNEL, C_PLAY_SOUND);
            packet.writePos(pos);
            packet.writeRegistryId(sound);
            packet.writeVarInt(category.ordinal());
            packet.writeFloat(volume);
            packet.writeFloat(pitch);
            packet.writeBoolean(distanceDelay);
            packet.sendToChunk(world, pos);
        }
    }

    public static void sendParticle(World world, IParticleData particleData, Vector3 pos, Vector3 motion, boolean distanceOverride) {
        if (!world.isClientSide) {
            PacketCustom packet = new PacketCustom(CHANNEL, C_SPAWN_PARTICLE);
            packet.writeRegistryId(particleData.getType());
            particleData.writeToNetwork(packet.toPacketBuffer());
            packet.writeVector(pos);
            packet.writeVector(motion);
            packet.writeBoolean(distanceOverride);
            packet.sendToChunk(world, pos.pos());
        }
    }

    /**
     * This is a custom entity spawn packet that removes the min/max velocity constraints.
     * @param entity The entity being spawned.
     * @return A packet to return in {@link Entity#getAddEntityPacket()}
     */
    public static IPacket<?> getEntitySpawnPacket(Entity entity) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_SPAWN_ENTITY);
        packet.writeVarInt(Registry.ENTITY_TYPE.getId(entity.getType()));
        packet.writeInt(entity.getId());
        packet.writeUUID(entity.getUUID());
        packet.writeDouble(entity.getX());
        packet.writeDouble(entity.getY());
        packet.writeDouble(entity.getZ());
        packet.writeByte((byte) MathHelper.floor(entity.xRot * 256.0F / 360.0F));
        packet.writeByte((byte) MathHelper.floor(entity.yRot * 256.0F / 360.0F));
        packet.writeByte((byte) (entity.getYHeadRot() * 256.0F / 360.0F));
        Vector3d velocity = entity.getDeltaMovement();
        packet.writeFloat((float) velocity.x);
        packet.writeFloat((float) velocity.y);
        packet.writeFloat((float) velocity.z);
        return packet.toPacket(NetworkDirection.PLAY_TO_CLIENT);
    }

    public static IPacket<?> sendEntityVelocity(Entity entity, boolean movement) {
        PacketCustom packet = new PacketCustom(CHANNEL, C_ENTITY_VELOCITY);
        packet.writeInt(entity.getId());
        packet.writeVec3f(new Vector3f(entity.getDeltaMovement()));
        packet.writeBoolean(movement);
        if (movement) {
            packet.writeFloat(entity.xRot);
            packet.writeFloat(entity.yRot);
            packet.writeBoolean(entity.isOnGround());
        }
        return packet.toPacket(NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void init() {
        netChannel = PacketCustomChannelBuilder.named(CHANNEL)
                .networkProtocolVersion(() -> "1")//
                .clientAcceptedVersions(e -> true)//
                .serverAcceptedVersions(e -> true)//
                .assignClientHandler(() -> ClientPacketHandler::new)//
                .assignServerHandler(() -> ServerPacketHandler::new)//
                .build();
    }
}
