package com.jabyftw.realtime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.mcstats.MetricsLite;

public class RealTime extends JavaPlugin {

    private NewConfig nConfig;
    private final File folder = new File("plugins" + File.separator + "RealTime");
    private FileConfiguration config;

    public boolean started = false, autoEnable, usePlayerTime, usePermissions, usePVPTime, useDebugMode, useDebugTime;
    public int useMode, timeFix, M0calcDelay, M0UpdateDelay, M0CalcDelay, M1UpdateDelay, pvpStart, pvpEnd, debugHour, debugMin, debugSec, mcTime, cVersion = 1;

    private List<String> worldList = new ArrayList();
    List<World> enabledWorlds = new ArrayList();

    @Override
    public void onEnable() {
        folder.mkdirs();
        nConfig = new NewConfig(this);
        nConfig.createConfig();
        setConfig();
        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
            log("Metrics started.", 0);
        } catch (IOException e) {
            log("Couldn't connect to Metrics.org: " + e, 1);
        }
        getCommand("realtime").setExecutor(new MyCommandExecutor(this));
        if (autoEnable) {
            if (!started) {
                startTasks();
            } else {
                log("RealTime IS already running somehow!", 1);
            }
        } else {
            log("RealTime ISNT running! Use: /realtime start", 1);
        }
    }

    @Override
    public void onDisable() {
        if (started) {
            getServer().getScheduler().cancelTasks(this);
            log("Closed tasks.", 0);
        } else {
            log("RealTime wasn't running!", 1);
        }
    }

    public void setConfig() {
        if (getConfig().getInt("DoNotChangeOrItWillEraseYourConfig.configVersion") != cVersion) {
            nConfig.deleteConfig();
            folder.delete();
            log("Recreating config for new version: " + cVersion, 1);
            folder.mkdirs();
            nConfig = new NewConfig(this);
            nConfig.createConfig();
            reloadConfig();
        }
        config = getConfig();
        M0CalcDelay = config.getInt("config.modeZero.CalcDelayInTicks");
        M0UpdateDelay = config.getInt("config.modeZero.UpdateDelayInTicks");
        M1UpdateDelay = config.getInt("config.modeOne.UpdateDelayIn3dot6Seconds");
        usePlayerTime = config.getBoolean("config.usePlayerTime");
        useMode = config.getInt("config.ModeBeingUsed");
        autoEnable = config.getBoolean("config.enableOnLoad");
        usePermissions = config.getBoolean("config.permissionActionsInGameEnabled");
        timeFix = config.getInt("config.timeFixInTicks");
        usePVPTime = config.getBoolean("PVPTime.enabled");
        pvpStart = config.getInt("PVPTime.startTime");
        pvpEnd = config.getInt("PVPTime.endTime");
        useDebugMode = config.getBoolean("debug.useDebugMode");
        useDebugTime = config.getBoolean("debug.DebugTime.enabled");
        debugHour = config.getInt("debug.DebugTime.hour");
        debugMin = config.getInt("debug.DebugTime.min");
        debugSec = config.getInt("debug.DebugTime.sec");
        log("Mode/PlayerTime/PVPTime+pvpStart : " + useMode + "/" + usePlayerTime + "/" + usePVPTime + pvpStart, 2);

        if (usePlayerTime && usePVPTime) {
            log("Can't use PlayerTime + PVPTime", 1);
            usePVPTime = false;
        }
        if (useMode == 1 && M1UpdateDelay != 1) {
            log("You are changing the time scale!", 1);
        }
        if (usePlayerTime && M0UpdateDelay < 40 && useMode == 0) {
            log("You may have some lag with this updateTime", 1);
        }
        if (timeFix > 24000 || timeFix < -24000) {
            timeFix = 0;
            log("You cant timeFix your time more than a day (24.000)", 1);
        }
        if (timeFix < 0) {
            timeFix = timeFix + 24000; // -2.000+24.000 = 22.000
            log("You cant set the timeFix to negative values. Setting into positive ones!", 1);
        }

        log("Configured.", 0);
    }

    public void startTasks() {
        BukkitScheduler sche = getServer().getScheduler();
        try {
            enabledWorlds = toWorldList(getConfig().getStringList("config.worldList"));
            log("Loaded WorldList: " + enabledWorlds.toString(), 0);
        } catch (NullPointerException e) {
            enabledWorlds = getServer().getWorlds(); // i was retarded
            log("Couldn't use WorldList from config.yml, using: " + enabledWorlds.toString(), 1);
        }
        if (useMode == 0) {
            sche.scheduleAsyncRepeatingTask(this, new CalculateTask(this), 40, M0CalcDelay);
            sche.scheduleSyncRepeatingTask(this, new SetTimeTask(this, 0), 45, M0UpdateDelay);
            started = true;
            log("NORMAL Mode (zero) is now running.", 0);
        } else if (useMode == 1) {
            sche.scheduleAsyncDelayedTask(this, new CalculateTask(this), 40);
            sche.scheduleSyncRepeatingTask(this, new SetTimeTask(this, 1), 45, M1UpdateDelay * 72);
            started = true;
            log("3.6 Mode (one) is now running.", 0);
        } else {
            log("You can only set mode 0 or 1.", 1);
            started = false;
        }
    }

    private List<World> toWorldList(List<String> NameList) {
        List<World> worlds = new ArrayList();
        for (int x = 0; x < NameList.size(); x++) {
            World w = getServer().getWorld(NameList.get(x));
            if (w.getEnvironment().getId() == 0) {
                worlds.add(w);
            }
        }
        return worlds;
    }

    /*
     * 0 - normal
     * 1 - warning
     * 2 - debug
     */
    void log(String msg, int mode) {
        switch (mode) {
            case 0:
                getLogger().log(Level.INFO, msg);
                break;
            case 1:
                getLogger().log(Level.WARNING, msg);
                break;
            default:
                if (useDebugMode) {
                    getLogger().log(Level.INFO, "Debug: " + msg);
                }
        }
    }

    int getTimeSec(String time) {
        int hour = Integer.parseInt(time.substring(0, 2));
        int min = Integer.parseInt(time.substring(3, 5));
        int sec = Integer.parseInt(time.substring(6, 8));
        if (useDebugTime) {
            return ((debugHour * 60 * 60) + (debugMin * 60) + debugSec);
        }
        return ((hour * 60 * 60) + (min * 60) + sec);
    }
}

class CalculateTask implements Runnable {

    private final RealTime plugin;

    public CalculateTask(RealTime plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        String time = new Date().toString().substring(11, 19);
        int timeInSec = (int) (plugin.getTimeSec(time) / 3.6);

        if ((timeInSec - 6000) < 0) { // Should fix 00:00 being day, but timeInSec will be negative until 6am
            plugin.mcTime = (int) (((timeInSec + 24000) - 6000) + plugin.timeFix); // timeInSec will be -6000, but it should be 18000
        } else {
            plugin.mcTime = (int) ((timeInSec - 6000) + plugin.timeFix);
        }
        plugin.log("mcTime: " + plugin.mcTime, 2);
    }
}
