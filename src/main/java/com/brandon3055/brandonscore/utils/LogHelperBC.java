package com.brandon3055.brandonscore.utils;

import codechicken.lib.reflect.ObfMapping;
import com.brandon3055.brandonscore.BrandonsCore;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.Set;

/**
 * Created by covers1624 on 3/21/2016.
 * Covers gave me permission to use this. In fact he FORCED me to use it!!!
 */
public class LogHelperBC {

    private static Logger logger = LogManager.getLogger(BrandonsCore.MODID);

    /**
     * Log with a supplied level.
     */
    public static void log(Level logLevel, Object object) {
        logger.log(logLevel, String.valueOf(object));
    }

    public static void log(Level logLevel, Object object, Throwable throwable) {
        logger.log(logLevel, String.valueOf(object), throwable);
    }

    //region Standard log entries.

    public static void dev(Object object) {
        if (!ObfMapping.obfuscated) {
            log(Level.INFO, "[DEV]: " + object);
        }
    }

    public static void all(Object object) {
        log(Level.ALL, object);
    }

    public static void debug(Object object) {
        log(Level.DEBUG, object);
    }

    public static void error(Object object) {
        log(Level.ERROR, object);
    }

    public static void fatal(Object object) {
        log(Level.FATAL, object);
    }

    public static void info(Object object) {
        log(Level.INFO, object);
    }

    public static void off(Object object) {
        log(Level.OFF, object);
    }

    public static void trace(Object object) {
        log(Level.TRACE, object);
    }

    public static void warn(Object object) {
        log(Level.WARN, object);
    }

    //endregion

    //region Log with format.

    public static void dev(String object, Object... format) {
        if (!ObfMapping.obfuscated) {
            log(Level.INFO, "[DEV]: " + String.format(object, format));
        }
    }

    public static void all(String object, Object... format) {
        log(Level.ALL, String.format(object, format));
    }

    public static void debug(String object, Object... format) {
        log(Level.DEBUG, String.format(object, format));
    }

    public static void error(String object, Object... format) {
        log(Level.ERROR, String.format(object, format));
    }

    public static void fatal(String object, Object... format) {
        log(Level.FATAL, String.format(object, format));
    }

    public static void info(String object, Object... format) {
        log(Level.INFO, String.format(object, format));
    }

    public static void off(String object, Object... format) {
        log(Level.OFF, String.format(object, format));
    }

    public static void trace(String object, Object... format) {
        log(Level.TRACE, String.format(object, format));
    }

    public static void warn(String object, Object... format) {
        log(Level.WARN, String.format(object, format));
    }

    //endregion

    //region Log Throwable with format.

    public static void allError(String object, Throwable throwable, Object... format) {
        log(Level.ALL, String.format(object, format), throwable);
    }

    public static void debugError(String object, Throwable throwable, Object... format) {
        log(Level.DEBUG, String.format(object, format), throwable);
    }

    public static void errorError(String object, Throwable throwable, Object... format) {
        log(Level.ERROR, String.format(object, format), throwable);
    }

    public static void fatalError(String object, Throwable throwable, Object... format) {
        log(Level.FATAL, String.format(object, format), throwable);
    }

    public static void infoError(String object, Throwable throwable, Object... format) {
        log(Level.INFO, String.format(object, format), throwable);
    }

    public static void offError(String object, Throwable throwable, Object... format) {
        log(Level.OFF, String.format(object, format), throwable);
    }

    public static void traceError(String object, Throwable throwable, Object... format) {
        log(Level.TRACE, String.format(object, format), throwable);
    }

    public static void warnError(String object, Throwable throwable, Object... format) {
        log(Level.WARN, String.format(object, format), throwable);
    }

    //endregion

    //region Log throwable.

    public static void allError(String object, Throwable throwable) {
        log(Level.ALL, object, throwable);
    }

    public static void debugError(String object, Throwable throwable) {
        log(Level.DEBUG, object, throwable);
    }

    public static void errorError(String object, Throwable throwable) {
        log(Level.ERROR, object, throwable);
    }

    public static void fatalError(String object, Throwable throwable) {
        log(Level.FATAL, object, throwable);
    }

    public static void infoError(String object, Throwable throwable) {
        log(Level.INFO, object, throwable);
    }

    public static void offError(String object, Throwable throwable) {
        log(Level.OFF, object, throwable);
    }

    public static void traceError(String object, Throwable throwable) {
        log(Level.TRACE, object, throwable);
    }

    public static void warnError(String object, Throwable throwable) {
        log(Level.WARN, object, throwable);
    }

    //endregion

    //region Log with trace element.

    public static void bigDev(String format, Object... data) {
        if (!ObfMapping.obfuscated) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            dev("****************************************");
            dev("* " + format, data);
            for (int i = 2; i < 8 && i < trace.length; i++) {
                dev("*  at %s%s", trace[i].toString(), i == 7 ? "..." : "");
            }
            dev("****************************************");
        }
    }

    public static void bigAll(String format, Object... data) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        all("****************************************");
        all("* " + format, data);
        for (int i = 2; i < 8 && i < trace.length; i++) {
            all("*  at %s%s", trace[i].toString(), i == 7 ? "..." : "");
        }
        all("****************************************");
    }

    public static void bigDebug(String format, Object... data) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        debug("****************************************");
        debug("* " + format, data);
        for (int i = 2; i < 8 && i < trace.length; i++) {
            debug("*  at %s%s", trace[i].toString(), i == 7 ? "..." : "");
        }
        debug("****************************************");
    }

    public static void bigError(String format, Object... data) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        error("****************************************");
        error("* " + format, data);
        for (int i = 2; i < 8 && i < trace.length; i++) {
            error("*  at %s%s", trace[i].toString(), i == 7 ? "..." : "");
        }
        error("****************************************");
    }

    public static void bigFatal(String format, Object... data) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        fatal("****************************************");
        fatal("* " + format, data);
        for (int i = 2; i < 8 && i < trace.length; i++) {
            fatal("*  at %s%s", trace[i].toString(), i == 7 ? "..." : "");
        }
        fatal("****************************************");
    }

    public static void bigInfo(String format, Object... data) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        info("****************************************");
        info("* " + format, data);
        for (int i = 2; i < 8 && i < trace.length; i++) {
            info("*  at %s%s", trace[i].toString(), i == 7 ? "..." : "");
        }
        info("****************************************");
    }

    public static void bigOff(String format, Object... data) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        off("****************************************");
        off("* " + format, data);
        for (int i = 2; i < 8 && i < trace.length; i++) {
            off("*  at %s%s", trace[i].toString(), i == 7 ? "..." : "");
        }
        off("****************************************");
    }

    public static void bigTrace(String format, Object... data) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        trace("****************************************");
        trace("* " + format, data);
        for (int i = 2; i < 8 && i < trace.length; i++) {
            trace("*  at %s%s", trace[i].toString(), i == 7 ? "..." : "");
        }
        trace("****************************************");
    }

    public static void bigWarn(String format, Object... data) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        warn("****************************************");
        warn("* " + format, data);
        for (int i = 2; i < 8 && i < trace.length; i++) {
            warn("*  at %s%s", trace[i].toString(), i == 7 ? "..." : "");
        }
        warn("****************************************");
    }

    //endregion

    //region formatted NBT log output

    public static void logNBTDev(NBTTagCompound compound) {
        logNBT(compound, true);
    }

    public static void logNBT(NBTTagCompound compound) {
        logNBT(compound, false);
    }

    public static void logNBTDev(ItemStack stack) {
        logNBTDev(stack.getTagCompound());
    }

    public static void logNBT(ItemStack stack) {
        logNBTDev(stack.getTagCompound());
    }

    public static void logNBT(NBTTagCompound compound, boolean debug) {
        if (debug && ObfMapping.obfuscated) {
            return;
        }

        if (compound == null || compound.hasNoTags()) {
            info("[NBT]: " + compound);
            return;
        }

        StringBuilder builder = new StringBuilder();
        buildNBT(builder, compound, "", "Tag", false);
        info(builder.toString());
    }

    public static void buildNBT(StringBuilder builder, NBTBase nbt, String indent, String name, boolean comma) {
        if (nbt instanceof NBTTagCompound) {
            builder.append("\n[NBT]: ").append(indent).append(name).append(":{");
            Set<String> keys = ((NBTTagCompound) nbt).getKeySet();
            int index = 0;
            for (String key : keys) {
                index++;
                buildNBT(builder, ((NBTTagCompound) nbt).getTag(key), indent + "|  ", key, index < keys.size());
            }
            builder.append("\n[NBT]: ").append(indent).append("}").append(comma ? "," : "");
        }
        else if (nbt instanceof NBTTagList) {
            builder.append("\n[NBT]: ").append(indent).append(name).append(":[");
            int tacCount = ((NBTTagList)nbt).tagCount();
            for (int i = 0; i < tacCount; i++) {
                NBTBase base = ((NBTTagList) nbt).get(i);
                buildNBT(builder, base , indent + "|  ", i+"", (i + 1) < tacCount);
            }
            builder.append("\n[NBT]: ").append(indent).append("]").append(comma ? "," : "");
        }
        else {
            builder.append("\n[NBT]: ").append(indent).append(name).append(":").append(nbt.toString()).append(comma ? "," : "");
        }
    }


    //endregion

    public static String[] comment = {"Sorry I did not mean to do that... Please forgive me?", "KABOOM!!!! It Blew Up!!!!", "Oh Sh** what was it this time!?!?", "WHAT DID YOU DO!?!?!?!.. Oh never mind that was me...", "HA! You thought you were going to play minecraft today? NO! You get to play \"Decode the Crash Report\" ", "Hmm. That was unexpected..."};

    public static void fatalErrorMessage(String error) {
        error(comment[new Random(System.nanoTime()).nextInt(comment.length)]);
        error("*************************************************************************************");
        error("It looks like a fatal error occurred which has caused the game to crash... [%s]", error);
        error("Please go here for assistance: https://github.com/brandon3055/BrandonsCore/issues");
        error("You can also try the #DraconicEvolution IRC channel on espernet");
        error("*************************************************************************************");
    }

    private static long startTime = 0;
    private static String timerName = "";
    private static boolean timerRunning = false;

    public static void startTimer(String name) {
        if (timerRunning) {
            error("The timer is already running!");
            return;
        }

        timerName = name;
        timerRunning = true;
        startTime = System.nanoTime();
    }

    public static void stopTimer() {
        if (!timerRunning) {
            error("The timer was not running!!!");
            return;
        }

        long ns = System.nanoTime() - startTime;

        String value;
        long ms = 1000000;
        long s = ms * 1000;

        if (ns > s) {
            value = Utils.round(ns / (double) s, 1000) + "s";
        }
        else if (ns > 1000) {
            value = Utils.round(ns / (double) ms, 10000) + "ms";
        }
        else {
            value = ns + "ns";
        }

        dev("[Timer]: " + timerName + " Took " + value);
        timerRunning = false;
    }
}
