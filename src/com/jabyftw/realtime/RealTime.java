package com.jabyftw.realtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class RealTime extends JavaPlugin {
    boolean usePlayerTime;
    boolean usePVPTime;
    boolean useDebugMode;
    boolean useDebugTime;
    boolean DebugTimeFixed;
    
    int useMode;
    int timeFix;
    int M0CalcDelay;
    int M0UpdateDelay;
    int M1UpdateDelay;
    int pvpStart;
    int pvpEnd;
    int debugHour;
    int debugMin;
    int debugSec;
    int mcTime;
    
    List<String> worldList;
    List<World> enabledWorlds;
    
    @Override
    public void onEnable() {
        setConfig();
        BukkitScheduler sche = getServer().getScheduler();        
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
            log("Metrics started.", 0);
        } catch (IOException e) {
            log("Couldn't connect to Metrics.org: " + e, 1);
        }
        
        if(useMode == 0) {
            sche.scheduleAsyncRepeatingTask(this, new CalculateTask(this), 10, M0CalcDelay);
            sche.scheduleSyncRepeatingTask(this, new SetTimeTask(this, 0), 20, M0UpdateDelay);
            log("NORMAL Mode (zero) running.", 0);
        } else if(useMode == 1) {
            sche.scheduleAsyncDelayedTask(this, new CalculateTask(this));
            sche.scheduleSyncRepeatingTask(this, new SetTimeTask(this, 1), 20, M1UpdateDelay * 72);
            log("Mode 3.6 (one) running.", 0);
        } else {
            log("You can only set mode 0 or 1.", 1);
        }
    }
    
    @Override
    public void onDisable() {}
    
    void setConfig() {
        getDataFolder().mkdir();
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        
        M0CalcDelay = config.getInt("config.modeZero.CalcDelayInTicks");
        M0UpdateDelay = config.getInt("config.modeZero.UpdateDelayInTicks");
        M1UpdateDelay = config.getInt("config.modeOne.UpdateDelayIn3dot6Seconds");
        
        usePlayerTime = config.getBoolean("config.usePlayerTime");
        useMode = config.getInt("config.ModeBeingUsed");
        timeFix = config.getInt("config.timeFixInTicks");
        enabledWorlds = toWorldList(config.getStringList("config.worldList"));
        
        usePVPTime = config.getBoolean("PVPTime.enabled");
        pvpStart = config.getInt("PVPTime.startTime");
        pvpEnd = config.getInt("PVPTime.endTime");
        
        useDebugMode = config.getBoolean("debug.useDebugMode");
        useDebugTime = config.getBoolean("debug.DebugTime.enabled");
        DebugTimeFixed = config.getBoolean("debug.DebugTime.fixed");
        debugHour = config.getInt("debug.DebugTime.hour");
        debugMin = config.getInt("debug.DebugTime.min");
        debugSec = config.getInt("debug.DebugTime.sec");
        
        if(usePlayerTime && usePVPTime) {
            log("Can't use PlayerTime + PVPTime", 1);
            usePVPTime = false;
        }
        if(useMode == 1 && M1UpdateDelay != 1)
            log("You are changing the time scale!", 1);
        if(usePlayerTime && M0UpdateDelay < 40 && useMode == 0)
            log("You may have some lag with this updateTime", 1);
        if(!DebugTimeFixed && M0CalcDelay != 20) {
            M0CalcDelay = 20;
            log("Setting calculation task to 1 sec due not Fixed Debug Time", 1);
            if(useDebugMode)
                M0UpdateDelay = 1;
        }
            
        log("Configured.", 0);
    }
    
        /*
         * 0 - normal
         * 1 - warning
         * 2 - debug
         */
    public void log(String msg, int mode) {
        if(mode == 0) // NORMAL
            getLogger().log(Level.INFO, msg);
        else if(mode == 1) // WARNING
            getLogger().log(Level.WARNING, msg);
        else if(mode == 2) { // DEBUG
            if(useDebugMode)
                getLogger().log(Level.INFO, "Debug: " + msg);
        }
    }
    
    private int fixed;
    private boolean firstRun = true;
    
    public int getTimeSec(String time) {
        int hour = Integer.parseInt(time.substring(0, 2));
        int min = Integer.parseInt(time.substring(3, 5));
        int sec = Integer.parseInt(time.substring(6, 8));
        if(useDebugTime) {
            if(!DebugTimeFixed) {
                if(firstRun) {
                    fixed = ((debugHour * 60 * 60) + (debugMin * 60) + debugSec); // 86340 for 23:59:00
                    firstRun = false;
                    return fixed;
                } else {
                    fixed = fixed + 1;
                    return fixed; // 86340 + 1 on each run -> every 20ticks
                }
            } else {
                return ((debugHour * 60 * 60) + (debugMin * 60) + debugSec);
            }
        }
        return ((hour * 60 * 60) + (min * 60) + sec);
    }
    
    List<World> toWorldList(List<String> NameList) {
        List<World> worlds = new ArrayList();
        for(int x = 0; x < NameList.size(); x++) {
            World w = getServer().getWorld(NameList.get(x));
            if(w.getEnvironment().getId() == 0)
                worlds.add(w);
        }
        return worlds;
    }
    
    List<String> toStringList(List<World> worldList) {
        List<String> names = new ArrayList();
        for(int x = 0; x < worldList.size(); x++)
            if(worldList.get(x).getEnvironment().getId() == 0)
                names.add(worldList.get(x).getName());
        return names;
    }
}

class CalculateTask implements Runnable {
    private RealTime plugin;
    public CalculateTask(RealTime plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        String time = new Date().toString().substring(11, 19);
        int timeInSec = (int) (plugin.getTimeSec(time) / 3.6);
        
        if((timeInSec - 6000) < 0) { // Should fix 00:00 being day, but timeInSec will be negative until 6am
            plugin.mcTime = (int) (((timeInSec + 24000) - 6000) + plugin.timeFix); // timeInSec will be -6000, but it should be 18000
        } else {
            plugin.mcTime = (int) ((timeInSec - 6000) + plugin.timeFix);
        }
        plugin.log("mcTime: " + plugin.mcTime, 2);
    }
}