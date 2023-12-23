package com.brandon3055.brandonscore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This command has all of the functionality of the vanilla tp command but with the ability
 * to teleport across dimensions,
 * <p>
 * Created by brandon3055 on 23/12/2017.
 */
public class CommandTPX {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tpx")
                        .requires((p_198816_0_) -> p_198816_0_.hasPermission(2))
                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                .executes(ctx -> teleportToPos(ctx.getSource(), Collections.singleton(ctx.getSource().getEntityOrException()), DimensionArgument.getDimension(ctx, "dimension"), null, null))
                                .then(Commands.argument("location", Vec3Argument.vec3())
                                        .executes(ctx -> teleportToPos(ctx.getSource(), Collections.singleton(ctx.getSource().getEntityOrException()), DimensionArgument.getDimension(ctx, "dimension"), Vec3Argument.getCoordinates(ctx, "location"), null))
                                )
                        )
                        .then(Commands.argument("destination", EntityArgument.entity())
                                .executes(ctx -> teleportToEntity(ctx.getSource(), Collections.singleton(ctx.getSource().getEntityOrException()), EntityArgument.getEntity(ctx, "destination")))
                        )
                        .then(Commands.argument("targets", EntityArgument.entities())
                                        .then(Commands.argument("destination", EntityArgument.entity())
                                                .executes(ctx -> teleportToEntity(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), EntityArgument.getEntity(ctx, "destination")))
                                        )
                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                .executes(ctx -> teleportToPos(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), DimensionArgument.getDimension(ctx, "dimension"), new EntityLocation(ctx.getSource().getEntityOrException()), null))
                                                .then(Commands.argument("location", Vec3Argument.vec3())
                                                        .executes(ctx -> teleportToPos(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), DimensionArgument.getDimension(ctx, "dimension"), Vec3Argument.getCoordinates(ctx, "location"), null))
                                                )
                                        )
//                                .then(Commands.argument("location", Vec3Argument.vec3())
//                                        .executes(ctx -> teleportToPos(ctx.getSource(), Collections.singleton(ctx.getSource().assertIsEntity()), ctx.getSource().getWorld(), Vec3Argument.getLocation(ctx, "location"), null))
//                                        .then(Commands.argument("dimension", DimensionArgument.getDimension())
//                                                .executes(ctx -> teleportToPos(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), ctx.getSource().getServer().getWorld(DimensionArgument.getDimension(ctx, "dimension")), Vec3Argument.getLocation(ctx, "location"), null))
//                                        )
//                                )
                        )
//                        .then(Commands.argument("location", Vec3Argument.vec3())
//                                .executes(ctx -> teleportToPos(ctx.getSource(), Collections.singleton(ctx.getSource().assertIsEntity()), ctx.getSource().getWorld(), Vec3Argument.getLocation(ctx, "location"), null))
//                                .then(Commands.argument("dimension", DimensionArgument.getDimension())
//                                        .executes(ctx -> teleportToPos(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), ctx.getSource().getServer().getWorld(DimensionArgument.getDimension(ctx, "dimension")), Vec3Argument.getLocation(ctx, "location"), null))
//                                )
//                        )
        );
    }

    private static int teleportToEntity(CommandSourceStack source, Collection<? extends Entity> targets, Entity destination) {
        for (Entity entity : targets) {
            teleport(source, entity, (ServerLevel) destination.level(), destination.getX(), destination.getY(), destination.getZ(), EnumSet.noneOf(RelativeMovement.class), destination.getYRot(), destination.getXRot());
        }

        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.teleport.success.entity.single", targets.iterator().next().getDisplayName(), destination.getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.teleport.success.entity.multiple", targets.size(), destination.getDisplayName()), true);
        }

        return targets.size();
    }

    private static Random rand = new Random();
    private static int teleportToPos(CommandSourceStack source, Collection<? extends Entity> targets, ServerLevel targetWorld, @Nullable Coordinates position, @Nullable Coordinates rotationIn) throws CommandSyntaxException {
        if (position == null) {
            BlockPos pos = new BlockPos(0, 127, 0);
            rand.setSeed(0);
            for (int i = 0; i < 1000; i++) {
                if (i < 500) {
                    pos = new BlockPos(-150 + rand.nextInt(300), 32 + rand.nextInt(80), -150 + rand.nextInt(300));
                }else {
                    pos = new BlockPos(-250 + rand.nextInt(500), 256, -250 + rand.nextInt(500));
                }
                if (targetWorld.isEmptyBlock(pos)) {
                    while (targetWorld.isEmptyBlock(pos.below()) && targetWorld.isInWorldBounds(pos.below())) {
                        pos = pos.below();
                    }
                    BlockState state = targetWorld.getBlockState(pos.below());
                    if (!state.getFluidState().isEmpty()) {
                        continue;
                    }
                    if (state.blocksMotion()) {
                        break;
                    }
                }
            }
            position = new BlockLocation(pos);
        }

        Vec3 vec3d = position.getPosition(source);
        Vec2 vec2f = rotationIn == null ? null : rotationIn.getRotation(source);
        Set<RelativeMovement> set = EnumSet.noneOf(RelativeMovement.class);
        if (position.isXRelative()) {
            set.add(RelativeMovement.X);
        }

        if (position.isYRelative()) {
            set.add(RelativeMovement.Y);
        }

        if (position.isZRelative()) {
            set.add(RelativeMovement.Z);
        }

        if (rotationIn == null) {
            set.add(RelativeMovement.X_ROT);
            set.add(RelativeMovement.Y_ROT);
        } else {
            if (rotationIn.isXRelative()) {
                set.add(RelativeMovement.X_ROT);
            }

            if (rotationIn.isYRelative()) {
                set.add(RelativeMovement.Y_ROT);
            }
        }

        for (Entity entity : targets) {
            if (rotationIn == null) {
                teleport(source, entity, targetWorld, vec3d.x, vec3d.y, vec3d.z, set, entity.getYRot(), entity.getXRot());
            } else {
                teleport(source, entity, targetWorld, vec3d.x, vec3d.y, vec3d.z, set, vec2f.y, vec2f.x);
            }
        }

        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.teleport.success.location.single", targets.iterator().next().getDisplayName(), vec3d.x, vec3d.y, vec3d.z), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.teleport.success.location.multiple", targets.size(), vec3d.x, vec3d.y, vec3d.z), true);
        }

        return targets.size();
    }

    private static void teleport(CommandSourceStack source, Entity entityIn, ServerLevel worldIn, double x, double y, double z, Set<RelativeMovement> relativeList, float yaw, float pitch) {
        if (entityIn instanceof ServerPlayer) {
            ChunkPos chunkpos = new ChunkPos(BlockPos.containing(x, y, z));
            worldIn.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 1, entityIn.getId());
            entityIn.stopRiding();
            if (((ServerPlayer) entityIn).isSleeping()) {
                ((ServerPlayer) entityIn).stopSleeping();
            }

            if (worldIn == entityIn.level()) {
                ((ServerPlayer) entityIn).connection.teleport(x, y, z, yaw, pitch, relativeList);
            } else {
                ((ServerPlayer) entityIn).teleportTo(worldIn, x, y, z, yaw, pitch);
            }

            entityIn.setYHeadRot(yaw);
        } else {
            float f1 = Mth.wrapDegrees(yaw);
            float f = Mth.wrapDegrees(pitch);
            f = Mth.clamp(f, -90.0F, 90.0F);
            if (worldIn == entityIn.level()) {
                entityIn.moveTo(x, y, z, f1, f);
                entityIn.setYHeadRot(f1);
            } else {
                entityIn.unRide();
                entityIn.changeDimension(worldIn);
                Entity entity = entityIn;
                entityIn = entityIn.getType().create(worldIn);
                if (entityIn == null) {
                    return;
                }

                entityIn.restoreFrom(entity);
                entityIn.moveTo(x, y, z, f1, f);
                entityIn.setYHeadRot(f1);
                worldIn.addDuringTeleport(entityIn);
            }
        }

        if (!(entityIn instanceof LivingEntity) || !((LivingEntity) entityIn).isFallFlying()) {
            entityIn.setDeltaMovement(entityIn.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
            entityIn.setOnGround(true);
        }
    }

    // All possible argument combinations
    // /tpx <destination player>                                      1
    // /tpx <destination dimension>                                   1
    // /tpx [target player] <destination player>                      2
    // /tpx [target player] <destination dimension>                   2
    // /tpx [target player] <x> <y> <z>                               4
    // /tpx [target player] <x> <y> <z> [dimension]                   5
    // /tpx <x> <y> <z>                                               3
    // /tpx <x> <y> <z> [dimension]                                   4

    private static class EntityLocation implements Coordinates {
        private Entity entity;

        public EntityLocation(Entity entity) {
            this.entity = entity;
        }

        @Override
        public Vec3 getPosition(CommandSourceStack source) {
            return entity.position();
        }

        @Override
        public Vec2 getRotation(CommandSourceStack source) {
            return source.getRotation();
        }

        @Override
        public boolean isXRelative() {
            return false;
        }

        @Override
        public boolean isYRelative() {
            return false;
        }

        @Override
        public boolean isZRelative() {
            return false;
        }
    }

    private static class BlockLocation implements Coordinates {
        private BlockPos pos;

        public BlockLocation(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public Vec3 getPosition(CommandSourceStack source) {
            return Vec3.atCenterOf(pos);
        }

        @Override
        public Vec2 getRotation(CommandSourceStack source) {
            return source.getRotation();
        }

        @Override
        public boolean isXRelative() {
            return false;
        }

        @Override
        public boolean isYRelative() {
            return false;
        }

        @Override
        public boolean isZRelative() {
            return false;
        }
    }
}