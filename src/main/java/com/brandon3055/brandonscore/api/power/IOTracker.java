package com.brandon3055.brandonscore.api.power;

import com.brandon3055.brandonscore.api.TimeKeeper;

public class IOTracker implements IOInfo {

    private long[] inputArray;
    private long[] outputArray;
    private long inputPerTick = 0;
    private long outputPerTick = 0;
    private int lastInputTick = 0;
    private int lastOutputTick = 0;
    private int lastInputCheck = 0;
    private int lastOutputCheck = 0;

    /**
     * @param averageTime Over how many ticks the IO data will be averaged
     */
    public IOTracker(int averageTime) {
        this.inputArray = new long[averageTime];
        this.outputArray = new long[averageTime];
    }

    public IOTracker() {
        this(20);
    }

    public void energyInserted(long amount) {
        int tick = TimeKeeper.getServerTick();
        zeroSkippedTicks(inputArray, lastInputTick, tick);
        if (tick != lastInputTick) {
            lastInputTick = tick;
            inputArray[tick % inputArray.length] = 0;
        }
        inputArray[tick % inputArray.length] += amount;
    }

    public void energyExtracted(long amount) {
        int tick = TimeKeeper.getServerTick();
        zeroSkippedTicks(outputArray, lastInputTick, tick);
        if (tick != lastOutputTick) {
            lastOutputTick = tick;
            outputArray[tick % outputArray.length] = 0;
        }
        outputArray[tick % outputArray.length] += amount;
    }

    public void energyModified(long amount) {
        if (amount > 0) {
            energyInserted(amount);
        }
        else {
            energyExtracted(amount * -1);
        }
    }

    private void zeroSkippedTicks(long[] array, int lastUpdateTick, int currentTick) {
        if (currentTick > lastUpdateTick) {
            for (int i = 0; i < Math.min(currentTick - lastUpdateTick, array.length); i++) {
                array[(lastUpdateTick + i) % array.length] = 0;
            }
        }
    }

    @Override
    public long currentInput() {
//        if (Bra)

        int tick = TimeKeeper.getServerTick();
        if (tick != lastInputCheck) {
            zeroSkippedTicks(inputArray, lastInputTick, tick);
            lastInputCheck = tick;
            inputPerTick = averageLongArray(inputArray);
        }
        return inputPerTick;
    }

    @Override
    public long currentOutput() {
        int tick = TimeKeeper.getServerTick();
        if (tick != lastOutputCheck) {
            zeroSkippedTicks(outputArray, lastInputTick, tick);
            lastOutputCheck = tick;
            outputPerTick = averageLongArray(outputArray);
        }
        return outputPerTick;
    }

    //This gets a little inaccurate when dealing with extremely large numbers but for this use case it is more than accurate enough.
    public static long averageLongArray(long[] array) {
        long average = 0;
        double remainder = 0;
        int count = array.length;
        for (long val : array) {
            average += val / count;
            remainder += ((double) val % (double) count) / (double) count;
        }
        return average + (long) remainder;
    }
}