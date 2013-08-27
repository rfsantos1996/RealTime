package com.jabyftw.realtime;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class RealTime extends JavaPlugin {
    public boolean pvpTime;
    public boolean debug;
    
    public int pvpStart;
    public int pvpEnd;
    public int resultedTime;
    public int timeFix;
    /*public int timeH;
    public int timeM;
    public int timeS;*/
    int calcTime;
    int updateTime;
    
    public List<World> enabledWorlds;
    List<String> worldEnabledList;
    
    @Override
    public void onEnable() {
        setConfig();
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new CalculateTask(this), calcTime, calcTime);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new SetTimeTask(this), updateTime, updateTime);
        getLogger().info("Registred tasks, we are running!");
    }
    
    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("Unregistered tasks!");
    }
    
    void setConfig() {
        FileConfiguration config = getConfig();
        config.addDefault("config.updateTime", 1);
        config.addDefault("config.calculateTime", 20);
        config.addDefault("config.fixYourTimeInTicks", 0);
        config.addDefault("config.enabledWorldsList", toString(getServer().getWorlds()));
        config.addDefault("pvpTime.enabled", false);
        config.addDefault("pvpTime.pvpStartTime", 500);
        config.addDefault("pvpTime.pvpEndTime", 12500);
        config.addDefault("debug.enabled", false);
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
        worldEnabledList = config.getStringList("config.enabledWorldsList");
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
        /*timeH = config.getInt("debug.timeInHour");
        timeM = config.getInt("debug.timeInMin");
        timeS = config.getInt("debug.timeInSec");*/
        getLogger().info("Configured!");
        enabledWorlds = toWorld(worldEnabledList);
        getLogger().info("Loaded WorldList!");
    }
    
    public List<World> toWorld(List<String> worldList) {
        List<World> worlds = new ArrayList();
        for(int x = 0; x < worldList.size(); x++)
            worlds.add(getServer().getWorld(worldEnabledList.get(x)));
        return worlds;
    }
    
    public List<String> toString(List<World> worldList) {
        List<String> worlds = new ArrayList();
        for(int x = 0; x < worldList.size(); x++)
            worlds.add(worldList.get(x).getName());
        return worlds;
    }
}