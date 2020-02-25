package com.brandon3055.brandonscore.api.power;

import com.brandon3055.brandonscore.api.TimeKeeper;
import net.minecraftforge.fml.common.thread.EffectiveSide;

/**
 * Created by brandon3055 on 16/10/19.
 *
 * This io tracker does not need to be updated every tick in order to function.
 * However it does have increased overhead as a result.
 */
public class IOTrackerSelfTimed extends IOTracker {

    protected int lastInputTick = 0;
    protected int lastOutputTick = 0;

    /**
     * @param averageTime Over how many ticks the IO data will be averaged
     */
    public IOTrackerSelfTimed(int averageTime) {
        super(averageTime);
    }

    public IOTrackerSelfTimed() {
        super(20);
    }

    @Override
    public void energyInserted(long amount) {
        int tick = TimeKeeper.getServerTick();
        zeroSkippedTicks(inputArray, lastInputTick, tick);
        if (tick != lastInputTick) {
            lastInputTick = tick;
            inputArray[tick % inputArray.length] = 0;
        }
        inputArray[tick % inputArray.length] += amount;
    }

    @Override
    public void energyExtracted(long amount) {
        int tick = TimeKeeper.getServerTick();
        zeroSkippedTicks(outputArray, lastInputTick, tick);
        if (tick != lastOutputTick) {
            lastOutputTick = tick;
            outputArray[tick % outputArray.length] = 0;
        }
        outputArray[tick % outputArray.length] += amount;
    }

    @Override
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
            for (int i = 1; i < Math.min(currentTick - lastUpdateTick, array.length + 1); i++) {
                array[(lastUpdateTick + i) % array.length] = 0;
            }
        }
    }

    @Override
    public long currentInput() {
        if (EffectiveSide.get().isClient()) {
            return inputPerTick;
        }
        else {
            int tick = TimeKeeper.getServerTick();
            if (tick != lastInputCheck) {
                zeroSkippedTicks(inputArray, lastInputTick, tick);
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
                zeroSkippedTicks(outputArray, lastOutputTick, tick);
                lastOutputCheck = tick;
                outputPerTick = averageLongArray(outputArray, tick % inputArray.length);
            }
            return outputPerTick;
        }
    }
}