package com.brandon3055.brandonscore.lib;

import gnu.trove.set.hash.THashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/**
 * Created by covers1624 on 8/1/2016.
 */
public class BlockPlacementBatcher {

    private final WorldServer serverWorld;
    private THashSet<Chunk> modifiedChunks = new THashSet<Chunk>();

    public BlockPlacementBatcher(WorldServer serverWorld) {
        this.serverWorld = serverWorld;
    }

    public void setBlockState(BlockPos pos, IBlockState state) {
        if (!hasBlockStorage(pos) && state.getBlock() == Blocks.AIR){
            return;
        }
        ExtendedBlockStorage storage = getBlockStorage(pos);
        storage.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, state);
        setChunkModified(pos);
    }

    public void setChunkModified(BlockPos blockPos) {
        setChunkModified(getChunk(blockPos));
    }

    public void setChunkModified(Chunk chunk) {
        modifiedChunks.add(chunk);
    }

    private Chunk getChunk(BlockPos pos) {
        return serverWorld.getChunkFromChunkCoords(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private boolean hasBlockStorage(BlockPos pos){
        Chunk chunk = getChunk(pos);
        return chunk.storageArrays[pos.getY() >> 4] != null;
    }

    private ExtendedBlockStorage getBlockStorage(BlockPos pos) {
        Chunk chunk = serverWorld.getChunkFromChunkCoords(pos.getX() >> 4, pos.getZ() >> 4);
        ExtendedBlockStorage storage = chunk.storageArrays[pos.getY() >> 4];
        if (storage == null) {
            storage = new ExtendedBlockStorage(pos.getY() >> 4 << 4, !serverWorld.provider.getHasNoSky());
            chunk.storageArrays[pos.getY() >> 4] = storage;
        }
        return storage;
    }

    /**
     * Call when finished placing blocks to que lighting and send chunk updates to the client.
     */
    public void finish() {
        for (Chunk chunk : modifiedChunks) {
            PlayerChunkMap playerChunkMap = serverWorld.getPlayerChunkMap();
            if (playerChunkMap == null) {
                return;
            }
            PlayerChunkMapEntry watcher = playerChunkMap.getEntry(chunk.xPosition, chunk.zPosition);
            if (watcher != null) {
                watcher.sendPacket(new SPacketChunkData(chunk, 65535));
            }
        }
        modifiedChunks.clear();
    }

}