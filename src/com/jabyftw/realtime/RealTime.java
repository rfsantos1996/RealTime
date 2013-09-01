package com.jabyftw.realtime;

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
    int calcTime;
    int updateTime;
    
    public List<World> enabledWorlds;
    List<String> worldList;
    
    @Override
    public void onEnable() {
        BukkitScheduler sche = getServer().getScheduler();
        setConfig();
        if(!useMode36) {
            sche.scheduleAsyncRepeatingTask(this, new CalculateTask(this), calcTime, calcTime);
            if(usePlayerTime)
                sche.scheduleSyncRepeatingTask(this, new SetPTimeTask(this), updateTime, updateTime);
            else
                sche.scheduleSyncRepeatingTask(this, new SetTimeTask(this), updateTime, updateTime);
        } else if(useMode36) {
            sche.runTaskAsynchronously(this, new CalculateTask(this));
            sche.scheduleSyncRepeatingTask(this, new Mode36Task(this), 72, 72);
        }
        getLogger().info("Registred tasks, we are enabled!");
    }
    
    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("Unregistered tasks!");
    }
    
    void setConfig() {
        FileConfiguration config = getConfig();
        config.addDefault("config.useMode36", false);
        config.addDefault("config.usePlayerTime", true);
        config.addDefault("config.usePVPTimeCompatibility", false);
        
        config.addDefault("config.updateTime", 60);
        config.addDefault("config.calculateTime", 30);
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
        /*
         * CONFIG GERAL
         */
        useMode36 = config.getBoolean("config.useMode36");
        usePlayerTime = config.getBoolean("config.usePlayerTime");
        usePVPTime = config.getBoolean("config.usePVPTimeCompatibility");
        
        updateTime = config.getInt("config.updateTime");
        calcTime = config.getInt("config.calculateTime");
        timeFix = config.getInt("config.fixYourTimeInTicks");
        worldList = config.getStringList("config.worldList");
        enabledWorlds = toWorld(worldList);
        /* 
         * PVP TIME
         */
        pvpStart = config.getInt("pvpTime.pvpStartTime");
        pvpEnd = config.getInt("pvpTime.pvpEndTime");
        /*
         * DEBUG
         */
        debug = config.getBoolean("debug.enabled");
        debugTime = config.getBoolean("debug.useDebugTimeChange");
        getLogger().info("Configured and loaded WorldList: " + toString(enabledWorlds).toString());
        /*
         * Warnings
         */
        if(usePlayerTime && updateTime < 40 && !useMode36)
            getLogger().log(Level.WARNING, "Recommended to low your update time, since you're using ptime");
        if(usePlayerTime && usePVPTime) {
            getLogger().log(Level.WARNING, "Disabling PVPTimeCompatibility (due PlayerTime use)");
            config.set("config.usePVPTimeCompatibility", false);
            usePVPTime = false;
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