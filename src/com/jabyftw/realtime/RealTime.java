package com.jabyftw.realtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class RealTime extends JavaPlugin {
    public boolean debug;
    public boolean debugTime;
    public boolean useMode36;
    public boolean usePlayerTime;
    public boolean usePVPTime;
    
    public int pvpStart;
    public int pvpEnd;
    public int resultedTime;
    public int timeFix;
    public double TperNight36; // 36 for Mode 3.6
    public double TperDay36;
    int NCalcTime;
    int NUpdateTime; // N for Normal
    
    public List<World> enabledWorlds;
    List<String> worldList;
    
    @Override
    public void onEnable() {
        BukkitScheduler sche = getServer().getScheduler();
        setConfig();
        if(!useMode36) {
            sche.scheduleAsyncRepeatingTask(this, new CalculateTask(this), NCalcTime, NCalcTime);
            if(usePlayerTime) {
                sche.scheduleSyncRepeatingTask(this, new SetPTimeTask(this), NUpdateTime, NUpdateTime);
                getLogger().info("Registred tasks - PTimeTask, we are enabled!");
            } else {
                sche.scheduleSyncRepeatingTask(this, new SetTimeTask(this), NUpdateTime, NUpdateTime);
                getLogger().info("Registred tasks - NormalTimeTask, we are enabled!");
            }
        } else {
            sche.runTaskAsynchronously(this, new CalculateTask(this));
            sche.scheduleSyncRepeatingTask(this, new Mode36Task(this), 144, 144); // 72 = 3.6 sec -> 144 = 7.2 not that smooth, but it'll work
            getLogger().info("Registred tasks - Mode36Task, we are enabled!");
        }

        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            getLogger().info("Couldn't connect to Metrics.org");
        }
    }
    
    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("Unregistered tasks!");
    }
    
    void setConfig() {
        FileConfiguration config = getConfig();
        config.addDefault("RealTime.useMode36", false);
        config.addDefault("RealTime.usePlayerTime", true);
        config.addDefault("RealTime.usePVPTimeCompatibility", false);
        
        config.addDefault("config.normalMode.updateTime", 60);
        config.addDefault("config.normalMode.calculateTime", 30);
        config.addDefault("config.mode36.ticksInDay", 2); // every (3.6 * 2) seconds, +2 in resulted
        config.addDefault("config.mode36.ticksInNight", 2.2); // every (3.6 * 2) seconds, +2.2 in resulted - night will be faster
        config.addDefault("config.fixYourTimeInTicks", 0);
        config.addDefault("config.worldList", toString(getServer().getWorlds()));
        
        config.addDefault("pvpTime.pvpStartTime", 500);
        config.addDefault("pvpTime.pvpEndTime", 12500);
        
        config.addDefault("debug.enabled", false);
        config.addDefault("debug.useDebugTimeChange", false);
        config.addDefault("debug.timeInHour", 0);
        config.addDefault("debug.timeInMin", 0);
        config.addDefault("debug.timeInSec", 0);
        
        config.options().copyDefaults(true);
        saveConfig();
        reloadConfig();

        useMode36 = config.getBoolean("RealTime.useMode36");
        usePlayerTime = config.getBoolean("RealTime.usePlayerTime");
        usePVPTime = config.getBoolean("RealTime.usePVPTimeCompatibility");
        
        NUpdateTime = config.getInt("config.normalMode.updateTime");
        NCalcTime = config.getInt("config.normalMode.calculateTime");
        
        TperDay36 = config.getDouble("config.mode36.ticksInDay");
        TperNight36 = config.getDouble("config.mode36.ticksInNight");
        
        timeFix = config.getInt("config.fixYourTimeInTicks");
        worldList = config.getStringList("config.worldList");
        enabledWorlds = toWorld(worldList);

        pvpStart = config.getInt("pvpTime.pvpStartTime");
        pvpEnd = config.getInt("pvpTime.pvpEndTime");

        debug = config.getBoolean("debug.enabled");
        debugTime = config.getBoolean("debug.useDebugTimeChange");
        getLogger().info("Configured and loaded WorldList: " + toString(enabledWorlds).toString());
        /*
         * Warnings
         */
        if(usePlayerTime && NUpdateTime < 40 && !useMode36)
            getLogger().log(Level.WARNING, "Recommended to low your update time, since you're using ptime");
        if(usePlayerTime && usePVPTime) {
            getLogger().log(Level.WARNING, "Disabling PVPTimeCompatibility (due PlayerTime use)");
            config.set("config.usePVPTimeCompatibility", false);
            usePVPTime = false;
            reloadConfig();
        }
        if(enabledWorlds.size() < worldList.size())
            getLogger().log(Level.WARNING, "Only NORMAL worlds are enabled.");
    }
    
    public List<World> toWorld(List<String> List) {
        List<World> worlds = new ArrayList();
        for(int x = 0; x < List.size(); x++) {
            World w = getServer().getWorld(worldList.get(x));
            if(w.getEnvironment().getId() == 0)
                worlds.add(w);
        }
        return worlds;
    }
    
    public List<String> toString(List<World> worldList) {
        List<String> worlds = new ArrayList();
        for(int x = 0; x < worldList.size(); x++)
            worlds.add(worldList.get(x).getName());
        return worlds;
    }
}