package com.brandon3055.brandonscore.command;

import com.brandon3055.brandonscore.BrandonsCore;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This command has all of the functionality of the vanilla tp command but with the ability
 * to teleport across dimensions,
 * <p>
 * Created by brandon3055 on 23/12/2017.
 */
public class CommandTPX {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
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
                                                        .executes(ctx -> teleportToPos(ctx.getSource(), Collections.singleton(ctx.getSource().getEntityOrException()), DimensionArgument.getDimension(ctx, "dimension"), Vec3Argument.getCoordinates(ctx, "location"), null))
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

    private static int teleportToEntity(CommandSource source, Collection<? extends Entity> targets, Entity destination) {
        for (Entity entity : targets) {
            teleport(source, entity, (ServerWorld) destination.level, destination.getX(), destination.getY(), destination.getZ(), EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class), destination.yRot, destination.xRot);
        }

        if (targets.size() == 1) {
            source.sendSuccess(new TranslationTextComponent("commands.teleport.success.entity.single", targets.iterator().next().getDisplayName(), destination.getDisplayName()), true);
        } else {
            source.sendSuccess(new TranslationTextComponent("commands.teleport.success.entity.multiple", targets.size(), destination.getDisplayName()), true);
        }

        return targets.size();
    }

    private static Random rand = new Random();
    private static int teleportToPos(CommandSource source, Collection<? extends Entity> targets, ServerWorld targetWorld, @Nullable ILocationArgument position, @Nullable ILocationArgument rotationIn) throws CommandSyntaxException {
        if (position == null) {
            BlockPos pos = new BlockPos(0, 127, 0);
            rand.setSeed(0);
            for (int i = 0; i < 1000; i++) {
                pos = new BlockPos(-150 + rand.nextInt(300), 32 + rand.nextInt(80), -150 + rand.nextInt(300));
                if (targetWorld.isEmptyBlock(pos)) {
                    while (targetWorld.isEmptyBlock(pos.below())) {
                        pos = pos.below();
                    }
                    BlockState state = targetWorld.getBlockState(pos.below());
                    if (!state.getFluidState().isEmpty()) {
                        continue;
                    }
                    if (state.getMaterial().blocksMotion()) {
                        break;
                    }
                }
            }
            position = new BlockLocation(pos);
        }

        Vector3d vec3d = position.getPosition(source);
        Vector2f vec2f = rotationIn == null ? null : rotationIn.getRotation(source);
        Set<SPlayerPositionLookPacket.Flags> set = EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class);
        if (position.isXRelative()) {
            set.add(SPlayerPositionLookPacket.Flags.X);
        }

        if (position.isYRelative()) {
            set.add(SPlayerPositionLookPacket.Flags.Y);
        }

        if (position.isZRelative()) {
            set.add(SPlayerPositionLookPacket.Flags.Z);
        }

        if (rotationIn == null) {
            set.add(SPlayerPositionLookPacket.Flags.X_ROT);
            set.add(SPlayerPositionLookPacket.Flags.Y_ROT);
        } else {
            if (rotationIn.isXRelative()) {
                set.add(SPlayerPositionLookPacket.Flags.X_ROT);
            }

            if (rotationIn.isYRelative()) {
                set.add(SPlayerPositionLookPacket.Flags.Y_ROT);
            }
        }

        for (Entity entity : targets) {
            if (rotationIn == null) {
                teleport(source, entity, targetWorld, vec3d.x, vec3d.y, vec3d.z, set, entity.yRot, entity.xRot);
            } else {
                teleport(source, entity, targetWorld, vec3d.x, vec3d.y, vec3d.z, set, vec2f.y, vec2f.x);
            }
        }

        if (targets.size() == 1) {
            source.sendSuccess(new TranslationTextComponent("commands.teleport.success.location.single", targets.iterator().next().getDisplayName(), vec3d.x, vec3d.y, vec3d.z), true);
        } else {
            source.sendSuccess(new TranslationTextComponent("commands.teleport.success.location.multiple", targets.size(), vec3d.x, vec3d.y, vec3d.z), true);
        }

        return targets.size();
    }

    private static void teleport(CommandSource source, Entity entityIn, ServerWorld worldIn, double x, double y, double z, Set<SPlayerPositionLookPacket.Flags> relativeList, float yaw, float pitch) {
        if (entityIn instanceof ServerPlayerEntity) {
            ChunkPos chunkpos = new ChunkPos(new BlockPos(x, y, z));
            worldIn.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 1, entityIn.getId());
            entityIn.stopRiding();
            if (((ServerPlayerEntity) entityIn).isSleeping()) {
                ((ServerPlayerEntity) entityIn).stopSleeping();
            }

            if (worldIn == entityIn.level) {
                ((ServerPlayerEntity) entityIn).connection.teleport(x, y, z, yaw, pitch, relativeList);
            } else {
                ((ServerPlayerEntity) entityIn).teleportTo(worldIn, x, y, z, yaw, pitch);
            }

            entityIn.setYHeadRot(yaw);
        } else {
            float f1 = MathHelper.wrapDegrees(yaw);
            float f = MathHelper.wrapDegrees(pitch);
            f = MathHelper.clamp(f, -90.0F, 90.0F);
            if (worldIn == entityIn.level) {
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
                worldIn.addFromAnotherDimension(entityIn);
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

    private static class EntityLocation implements ILocationArgument {
        private Entity entity;

        public EntityLocation(Entity entity) {
            this.entity = entity;
        }

        @Override
        public Vector3d getPosition(CommandSource source) {
            return entity.position();
        }

        @Override
        public Vector2f getRotation(CommandSource source) {
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

    private static class BlockLocation implements ILocationArgument {
        private BlockPos pos;

        public BlockLocation(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public Vector3d getPosition(CommandSource source) {
            return Vector3d.atCenterOf(pos);
        }

        @Override
        public Vector2f getRotation(CommandSource source) {
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