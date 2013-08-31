package com.jabyftw.realtime;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class RealTime extends JavaPlugin {
    public boolean pvpTime;
    public boolean debug;
    public boolean debugTime;
    boolean usePlayerTime;
    
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
        setConfig();
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new CalculateTask(this), calcTime, calcTime);
        if(usePlayerTime)
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new SetPTimeTask(this), updateTime, updateTime);
        else
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new SetTimeTask(this), updateTime, updateTime);
        getLogger().info("Registred tasks, we are enabled!");
    }
    
    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("Unregistered tasks!");
    }
    
    void setConfig() {
        FileConfiguration config = getConfig();
        config.addDefault("config.updateTime", 60);
        config.addDefault("config.calculateTime", 30);
        config.addDefault("config.fixYourTimeInTicks", 0);
        config.addDefault("config.usePlayerTimeInstead", true);
        config.addDefault("config.worldList", toString(getServer().getWorlds()));
        
        config.addDefault("pvpTime.enabled", false);
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
        updateTime = config.getInt("config.updateTime");
        calcTime = config.getInt("config.calculateTime");
        timeFix = config.getInt("config.fixYourTimeInTicks");
        usePlayerTime = config.getBoolean("config.usePlayerTimeInstead");
        worldList = config.getStringList("config.worldList");
        enabledWorlds = toWorld(worldList);
        /* 
         * PVP TIME
         */
        pvpTime = config.getBoolean("pvpTime.enabled");
        pvpStart = config.getInt("pvpTime.pvpStartTime");
        pvpEnd = config.getInt("pvpTime.pvpEndTime");
        /*
         * DEBUG
         */
        debug = config.getBoolean("debug.enabled");
        debugTime = config.getBoolean("debug.useDebugTimeChange");
        getLogger().info("Configured and loaded WorldList!");
        /*
         * Warnings
         */
        if(usePlayerTime && updateTime < 40) // maybe it'll be laggy
            getLogger().log(Level.WARNING, "Recommended to low your update time, since you're using ptime");
        if(usePlayerTime && pvpTime) // pvptime wont change anything when using ptime
            getLogger().log(Level.WARNING, "If you are using Ptime, there's no need of using pvpTime Compatibility");
        if(enabledWorlds.size() < worldList.size()) // there's more worlds on WorldList than enabled worlds
            getLogger().log(Level.WARNING, "Only NORMAL worlds are enabled.");
        if(debug)
            getLogger().info("enabled World: " + toString(enabledWorlds).toString());
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