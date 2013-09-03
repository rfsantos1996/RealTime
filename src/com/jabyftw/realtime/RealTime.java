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
            log("Mode 2 running.", 0);
        } else if(useMode == 1) {
            sche.scheduleAsyncDelayedTask(this, new CalculateTask(this));
            sche.scheduleSyncRepeatingTask(this, new SetTimeTask(this, 1), 20, M1UpdateDelay);
            log("Mode 1 running.", 0);
        } else {
            log("You can only set mode 0 or 1.", 1);
        }
    }
    
    @Override
    public void onDisable() {}
    
    void setConfig() {
        FileConfiguration config = getConfig();
        config.addDefault("config.usePlayerTime", true);
        config.addDefault("config.useMode", 0); // 0 = Normal mode, 1 = 3.6 sec Mode
        config.addDefault("config.timeFixInTicks", 0);
        config.addDefault("config.worldList", toStringList(getServer().getWorlds()));
        
        config.addDefault("config.mode0.CalcDelayInTicks", 1);
        config.addDefault("config.mode0.UpdateDelayInTicks", 1);
        config.addDefault("config.mode1.UpdateDelayInTicks", 72); // 3.6 sec = 72ticks || 144 = 7.2 sec
        
        config.addDefault("PVPTime.enabled", false);
        config.addDefault("PVPTime.startTime", 500); // default values
        config.addDefault("PVPTime.endTime", 12500);
        
        config.addDefault("debug.useDebugMode", false);
        config.addDefault("debug.DebugTime.enabled", false);
        config.addDefault("debug.DebugTime.hour", 0);
        config.addDefault("debug.DebugTime.min", 0);
        config.addDefault("debug.DebugTime.sec", 0);
        config.options().copyDefaults(true);
        saveConfig();
        
        usePlayerTime = config.getBoolean("config.usePlayerTime");
        useMode = config.getInt("config.useMode");
        timeFix = config.getInt("config.timeFixInTicks");
        enabledWorlds = toWorldList(config.getStringList("config.worldList"));
        
        M0CalcDelay = config.getInt("config.mode0.CalcDelayInTicks");
        M0UpdateDelay = config.getInt("config.mode0.UpdateDelayInTicks");
        M1UpdateDelay = config.getInt("config.mode1.UpdateDelayInTicks");
        
        usePVPTime = config.getBoolean("PVPTime.enabled");
        pvpStart = config.getInt("PVPTime.startTime");
        pvpEnd = config.getInt("PVPTime.endTime");
        
        useDebugMode = config.getBoolean("debug.useDebugMode");
        useDebugTime = config.getBoolean("debug.DebugTime.enabled");
        debugHour = config.getInt("debug.DebugTime.hour");
        debugMin = config.getInt("debug.DebugTime.min");
        debugSec = config.getInt("debug.DebugTime.sec");
        
        if(usePlayerTime && usePVPTime) {
            log("Can't use PlayerTime + PVPTime", 1);
            usePVPTime = false;
        }
        if(useMode == 1 && M1UpdateDelay != 72)
            log("You are changing the time scale!", 1);
        if(usePlayerTime && M0UpdateDelay < 40 && useMode == 0)
            log("You may have some lag with this updateTime", 1);
        if(usePlayerTime && M1UpdateDelay < 72 && useMode == 1)
            log("You may have some lag with this updateTime", 1);
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
    
    public int getTimeSec(String time) {
        int hour = Integer.parseInt(time.substring(0, 2));
        int min = Integer.parseInt(time.substring(3, 5));
        int sec = Integer.parseInt(time.substring(6, 8));
        if(useDebugTime)
            return ((debugHour * 60 * 60) + (debugMin * 60) + debugSec);
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
        plugin.mcTime = (plugin.getTimeSec(time) - 6000) + plugin.timeFix;
    }
}