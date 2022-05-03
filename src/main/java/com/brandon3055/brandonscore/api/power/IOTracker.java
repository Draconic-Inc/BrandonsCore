package com.brandon3055.brandonscore.api.power;

import com.brandon3055.brandonscore.api.TimeKeeper;
import net.minecraftforge.fml.util.thread.EffectiveSide;

/**
 * Created by brandon3055 on 16/10/19.
 */
public class IOTracker implements IOInfo, Runnable {

    protected long[] inputArray;
    protected long[] outputArray;
    protected long inputPerTick = 0;
    protected long outputPerTick = 0;
    protected int tick;
    protected int lastInputCheck = 0;
    protected int lastOutputCheck = 0;

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

    /**
     * This method must be called every tick (server side) by whatever is implementing the parent OPStorage. e.g. a tile entity.
     * If this is not possible then use {@link IOTrackerSelfTimed} Its less efficient but it does not need to be updated.
     */
    public void run() {
        tick++;
        inputArray[tick % inputArray.length] = 0;
        outputArray[tick % outputArray.length] = 0;
    }

    public void energyInserted(long amount) {
        inputArray[tick % inputArray.length] += amount;
    }

    public void energyExtracted(long amount) {
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

    public void syncClientValues(long inputPerTick, long outputPerTick) {
        this.inputPerTick = inputPerTick;
        this.outputPerTick = outputPerTick;
    }

    @Override
    public long currentInput() {
        if (EffectiveSide.get().isClient()) {
            return inputPerTick;
        }
        else {
            if (tick != lastInputCheck) {
                lastInputCheck = tick;
                inputPerTick = averageLongArray(inputArray, tick % inputArray.length);
            }
            return inputPerTick;
        }
    }

    @Override
    public long currentOutput() {
        if (EffectiveSide.get().isClient()) {
            return outputPerTick;
        }
        else {
            int tick = TimeKeeper.getServerTick();
            if (tick != lastOutputCheck) {
                lastOutputCheck = tick;
                outputPerTick = averageLongArray(outputArray, tick % inputArray.length);
            }
            return outputPerTick;
        }
    }

    //This gets a little inaccurate when dealing with extremely large numbers but for this use case it is more than accurate enough.
    public static long averageLongArray(long[] array, int skipIndex) {
        long average = 0;
        double remainder = 0;
        int count = array.length - 1;
        for (int i = 0; i < array.length; i++) {
            if (i == skipIndex) continue;
            long val = array[i];
            average += val / count;
            remainder += ((double) val % (double) count) / (double) count;
        }
        return average + Math.round(remainder + 0.1F);
    }
}