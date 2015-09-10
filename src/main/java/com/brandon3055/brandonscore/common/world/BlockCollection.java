package com.brandon3055.brandonscore.common.world;

///**
// * Created by brandon3055 on 1/9/2015.
// *
// * BlockCollection is an object that acts as a small virtual minecraft world object. It allows you to set and get blocks
// * within a predetermined area as if setting or getting blocks in an actual world object but without the performance
// * hit that comes with placing hundreds or thousands of blocks all at once.
// *
// * This was created to be used with the ChaosIsland world generator implemented in DraconicEvolution. It will make it
// * possible to generate the entire chaos island structure virtually without actually placing any blocks in the world.
// * The structure can then be loaded in chunk by chunk as the world generates for optimal performance.
// *
// * Block collections are not currently saved when the game closes.
// */

/**Never used this class but left it here just in case i use it in the future*/
public class BlockCollection {

////	private short[] blocks;
////	private byte[] blockMeta;
////	private int size;
////	private boolean centreAt00;
//	private Map<Long, BlockDat> blocks = new HashMap<Long, BlockDat>();
//	private Map<Integer, Block> idMap = new HashMap<Integer, Block>();
//
//	/**@param size sets the x and z size of the collection. The height is 255 blocks
//	 * @param centreAt00 if true pos 0X, 0Z will be at the centre of the collection allowing for negative and positive
//	 * block coordinates*/
//	public BlockCollection(int size, boolean centreAt00){
//		//this.blocks = new short[size * 255 * size];
//		//this.blockMeta = new byte[size * 255 * size];
//		//this.size = size;
//		//this.centreAt00 = centreAt00;
//	}
//
//
//
//	public void setBlock(int x, int y, int z, Block block) {
//		setBlock(x, y, z, block, 0);
//	}
//
//	/**setBlock with a dummy int used to make converting world generators that use World#setBlock(x, y, z, block, meta, updateFlag) a little easier*/
//	public void setBlock(int x, int y, int z, Block block, int meta, int i) {
//		setBlock(x, y, z, block, 0);
//	}
//
//	/**Sets the block at the given coords if the given coords are withing the collection boundaries*/
//	public void setBlock(int x, int y, int z, Block block, int meta) {
//		if (block != Blocks.air) {
//			if (!idMap.containsValue(block)) idMap.put(Block.getIdFromBlock(block), block);
//			blocks.put(getIndex(x, y, z), new BlockDat(Block.getIdFromBlock(block), meta));
//		}
////		if (centreAt00){
////			x += (size/2);
////			z += (size/2);
////		}
////
////		int blockIndex = (y * size + z) * size + x;
////
////		if (blockIndex >= 0 && blockIndex < blocks.length)
////		{
////			if (!idMap.containsValue(block)) idMap.put(Block.getIdFromBlock(block), block);
////			blocks[blockIndex] = (short)Block.getIdFromBlock(block);
////			blockMeta[blockIndex] = (byte)meta;
////		}
////		else LogHelper.error("BlockCollection.setBlock: Attempt to access block position outside collection boundaries! {"+x+", "+y+", "+z+", Size:"+size+", centreAt00:"+centreAt00+"}");
//	}
//
//	public Block getBlock(int x, int y, int z){
//		return blocks.containsKey(getIndex(x, y, z)) ? idMap.get(blocks.get(getIndex(x, y, z)).getID()) : Blocks.air;
////		if (centreAt00){
////			x += (size/2);
////			z += (size/2);
////		}
////
////		int blockIndex = (y * size + z) * size + x;
////
////		if (blockIndex >= 0 && blockIndex < blocks.length)
////		{
////			int blockId = blocks[blockIndex];
////			if (idMap.containsKey(blockId)) return idMap.get(blockId);
////			else if (blockId == 0) return Blocks.air;
////			else {
////				LogHelper.error("BlockCollection.getBlock: Block id "+blockId+" Not bound in idMap!");
////				return Blocks.air;
////			}
////		}
////		else {
////			LogHelper.error("BlockCollection.getBlock: Attempt to access block position outside collection boundaries! {"+x+", "+y+", "+z+", Size:"+size+", centreAt00:"+centreAt00+"}");
////			LogHelper.error(blockIndex+" : "+blocks.length);
////			return Blocks.air;
////		}
//	}
//
//	public int getBlockMetadata(int x, int y, int z){
//		return blocks.containsKey(getIndex(x, y, z)) ? blocks.get(getIndex(x, y, z)).getMeta() : 0;
////		if (centreAt00){
////			x += (size/2);
////			z += (size/2);
////		}
////
////		int blockIndex = (y * size + z) * size + x;
////
////		if (blockIndex >= 0 && blockIndex < blocks.length)
////		{
////			return blockMeta[blockIndex];
////		}
////		else {
////			LogHelper.error("BlockCollection.getBlockMetadata: Attempt to access block position outside collection boundaries! {"+x+", "+y+", "+z+", Size:"+size+", centreAt00:"+centreAt00+"}");
////			return 0;
////		}
//	}
//
//	public boolean isAirBlock(int x, int y, int z){
//		return getBlock(x, y, z) == Blocks.air;
//	}
//
//	public boolean blockExists(int x, int y, int z){
//		return blocks.containsKey(getIndex(x, y, z));
//	}
//
//	public long getIndex(int x, int y, int z){
//		x += 30000000;
//		z += 30000000;
//		return (y * 60000000L + z) * 60000000L + x;
//	}
//
//	public static class BlockDat {
//		private byte data1;
//		private byte data2;
//		public BlockDat(int id, int meta){
//			this.data1 = (byte)(id >> 4);
//			this.data2 = (byte)(((id & 0xF) << 4) | meta);
//		}
//
//		public int getID(){
//			return ((data1 & 0xFF) << 4) | ((data2 & 0xF0) >> 4);
//		}
//
//		public int getMeta(){
//			return data2 & 0x0F;
//		}
//	}
}
