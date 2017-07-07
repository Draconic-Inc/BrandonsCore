package com.brandon3055.brandonscore.lib;

/**
 * Created by brandon3055 on 23/06/2017.
 */
public class FakeWorld {//extends World {
//
//    private static final WorldSettings worldSettings = new WorldSettings(0, GameType.SURVIVAL, false, false, WorldType.DEFAULT);
//    private static final WorldInfo worldInfo = new WorldInfo(worldSettings, "bc_fake_world");
//    private static final ISaveHandler saveHandler = new SaveHandlerMP();
//    private static final WorldProvider worldProvider = new WorldProvider() {
//        @Override
//        public DimensionType getDimensionType() {
//            return DimensionType.OVERWORLD;
//        }
//    };
//
//    public static FakeWorld instance = new FakeWorld();
//
//    private FakeWorld() {
//        super(saveHandler, worldInfo, worldProvider, new Profiler(), true);
//        this.mapStorage = new SaveDataMemoryStorage();
//    }
//
//    @Override
//    public BlockPos getSpawnPoint() {
//        return new BlockPos(0, 0, 0);
//    }
//
//    @Override
//    public IBlockState getBlockState(BlockPos pos) {
//        return Blocks.AIR.getDefaultState();
//    }
//
//    @Override
//    protected IChunkProvider createChunkProvider() {
//        return new IChunkProvider() {
//            @Nullable
//            @Override
//            public Chunk getLoadedChunk(int x, int z) {
//                return new EmptyChunk(FakeWorld.this, x, z);
//            }
//
//            @Override
//            public Chunk provideChunk(int x, int z) {
//                return new EmptyChunk(FakeWorld.this, x, z);
//            }
//
//            @Override
//            public boolean tick() {
//                return false;
//            }
//
//            @Override
//            public String makeString() {
//                return "";
//            }
//
//            @Override
//            public boolean isChunkGeneratedAt(int p_191062_1_, int p_191062_2_) {
//                return false;
//            }
//        };
//    }
//
//    @Override
//    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
//        return false;
//    }
}
