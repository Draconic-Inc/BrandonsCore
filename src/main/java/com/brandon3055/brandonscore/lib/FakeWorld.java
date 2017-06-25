package com.brandon3055.brandonscore.lib;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;
import java.io.File;

/**
 * Created by brandon3055 on 23/06/2017.
 */
public class FakeWorld extends World {

    private static ISaveHandler fakeSaveHandler = new ISaveHandler() {
        @Nullable
        @Override
        public WorldInfo loadWorldInfo() {
            return null;
        }

        @Override
        public void checkSessionLock() throws MinecraftException {

        }

        @Override
        public IChunkLoader getChunkLoader(WorldProvider provider) {
            return null;
        }

        @Override
        public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {

        }

        @Override
        public void saveWorldInfo(WorldInfo worldInformation) {

        }

        @Override
        public IPlayerFileData getPlayerNBTManager() {
            return null;
        }

        @Override
        public void flush() {

        }

        @Override
        public File getWorldDirectory() {
            return null;
        }

        @Override
        public File getMapFileFromName(String mapName) {
            return null;
        }

        @Override
        public TemplateManager getStructureTemplateManager() {
            return null;
        }
    };
    public static WorldProvider fakeProvider = new WorldProvider() {

        @Override
        public DimensionType getDimensionType() {
            return DimensionType.OVERWORLD;
        }
    };

    public static FakeWorld instance = new FakeWorld();

    protected FakeWorld() {
        super(fakeSaveHandler, new WorldInfo(new NBTTagCompound()), fakeProvider, new Profiler(), false);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return new IChunkProvider() {
            @Nullable
            @Override
            public Chunk getLoadedChunk(int x, int z) {
                return null;
            }

            @Override
            public Chunk provideChunk(int x, int z) {
                return new Chunk(FakeWorld.this, x, z);
            }

            @Override
            public boolean tick() {
                return false;
            }

            @Override
            public String makeString() {
                return "";
            }

            @Override
            public boolean isChunkGeneratedAt(int p_191062_1_, int p_191062_2_) {
                return false;
            }
        };
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return false;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        return false;
    }

    @Override
    public boolean setBlockToAir(BlockPos pos) {
        return false;
    }
}
