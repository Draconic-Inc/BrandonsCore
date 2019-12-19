package com.brandon3055.brandonscore.lib.datamanager;

///**
// * Created by brandon3055 on 12/06/2017.
// */ If i ever actually need this i may revive it but for now its kinda useless. I made it for 1 purpose... then never used it.
//public class ManagedBoolArray extends AbstractManagedData {
//
//    private final int lenghth;
//    private boolean[] boolCache;
//    private byte[] bytes;
//    private byte[] lastTickBytes;
//
//    public ManagedBoolArray(boolean[] value) {
//        int r = value.length % 8;
//        this.lenghth = value.length + (r == 0 ? 0 : 8 - r);
//        this.bytes = new byte[lenghth / 8];
//        this.lastTickBytes = new byte[lenghth / 8];
//        this.boolCache = new boolean[lenghth];
//
//        if (value.length > 2048) {
//            throw new IllegalArgumentException("BrandonsCore, SyncableBoolArray max array size is " + 2048 + " but was given " + value.length);
//        }
//    }
//
//    @Override
//    public void detectAndSendChanges(TileBCBase tile, PlayerEntity player, boolean forceSync) {
//        for (int i = 0; i < bytes.length; i++) {
//            if (bytes[i] != lastTickBytes[i] || forceSync) {
//                lastTickBytes[i] = bytes[i];
//                tile.dirtyBlock();
//                if (player == null) {
//                    BrandonsCore.network.sendToAllAround(new PacketSyncableObject(tile, index, bytes[i], (byte) i, updateOnReceived), tile.syncRange());
//                }
//                else if (player instanceof ServerPlayerEntity) {
//                    BrandonsCore.network.sendTo(new PacketSyncableObject(tile, index, bytes[i], (byte) i, updateOnReceived), (ServerPlayerEntity) player);
//                }
//
//                reCache();
//            }
//        }
//    }
//
//    @Override
//    public void updateReceived(PacketSyncableObject packet) {
//        if (packet.dataType == PacketSyncableObject.BOOL_ARRAY_INDEX) {
//            int index = packet.boolArrayPartIndex & 0xFF;
//            if (index < 0 || index >= bytes.length) {
//                return;
//            }
//
//            bytes[index] = packet.boolArrayPart;
//            reCache();
//        }
//    }
//
//    @Override
//    public void toNBT(CompoundNBT compound) {
//        compound.setByteArray("SyncableBoolArray" + index, bytes);
//    }
//
//    @Override
//    public void fromNBT(CompoundNBT compound) {
//        if (compound.hasKey("SyncableBoolArray" + index)) {
//            bytes = compound.getByteArray("SyncableBoolArray" + index);
//        }
//        else {
//            bytes = new byte[lenghth];
//        }
//        lastTickBytes = bytes.clone();
//        reCache();
//    }
//
//    public boolean get(int index) {
//        if (index >= lenghth) {
//            throw new IndexOutOfBoundsException("Index: " + index + " Array Size: " + lenghth);
//        }
//        return boolCache[index];
//    }
//
//    public void set(boolean b, int index) {
//        try {
//            if (index >= lenghth) {
//                throw new IndexOutOfBoundsException("Index: " + index + " Array Size: " + lenghth);
//            }
//            boolCache[index] = b;
//            String bs = Integer.toBinaryString(Byte.toUnsignedInt(bytes[index / 8]));
//
//            for (; bs.length() < 8; bs = "0" + bs) ;
//            char[] bits = bs.toCharArray();
//            bits[index % 8] = b ? '1' : '0';
//            bytes[index / 8] = (byte) Integer.parseInt(new String(bits), 2);
//        }
//        catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void reCache() {
//        try {
//            for (int i = 0; i < lenghth; i++) {
//                String bits = Integer.toBinaryString(Byte.toUnsignedInt(bytes[i / 8]));
//                for (; bits.length() < 8; bits = "0" + bits) ;
//                boolCache[i] = bits.charAt(i % 8) == '1';
//            }
//        }
//        catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }
//
//    public int lenghth() {
//        return lenghth;
//    }
//
//    @Override
//    public String toString() {
//        return String.valueOf(boolCache);
//    }
//}
