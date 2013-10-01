package com.jabyftw.realtime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class RealTime extends JavaPlugin {
    int cVersion = 1;
    private NewConfig nConfig;
    private File folder = new File("plugins" + File.separator + "RealTime");
    
    boolean started = false;
    boolean autoEnable;
    boolean usePlayerTime;
    boolean usePermissions;
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
        folder.mkdirs();
        nConfig = new NewConfig(this);
        nConfig.createConfig();
        
        setConfig();
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
            log("Metrics started.", 0);
        } catch (IOException e) {
            log("Couldn't connect to Metrics.org: " + e, 1);
        }
        getCommand("realtime").setExecutor(new MyCommandExecutor(this));
        
        if(autoEnable) {
            if(!started)
                startTasks();
            else
                log("RealTime IS already running somehow!", 1);
        } else {
            log("RealTime ISNT running! Use: /realtime start", 1);
        }
    }
    
    @Override
    public void onDisable() {
        if(started) {
            getServer().getScheduler().cancelTasks(this);
            log("Closed tasks.", 0);
        } else {
            log("RealTime wasn't running!", 1);
        }
    }
    
    void setConfig() {
        boolean recreated = false;
        if(getConfig().getInt("DoNotChangeOrItWillEraseYourConfig.configVersion") != cVersion) {
            nConfig.deleteConfig();
            folder.delete();
            log("Recreating config for new version: " + cVersion, 1);
            folder.mkdirs();
            nConfig = new NewConfig(this);
            nConfig.createConfig();
            recreated = true;
            reloadConfig();
        }
        FileConfiguration config = getConfig();
        
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
        
        if(usePlayerTime && usePVPTime) {
            log("Can't use PlayerTime + PVPTime", 1);
            usePVPTime = false;
        }
        if(useMode == 1 && M1UpdateDelay != 1)
            log("You are changing the time scale!", 1);
        if(usePlayerTime && M0UpdateDelay < 40 && useMode == 0)
            log("You may have some lag with this updateTime", 1);
        if(timeFix > 24000 || timeFix < -24000) {
            timeFix = 0;
            log("You cant timeFix your time more than a day (24.000)", 1);
        }
        if(timeFix < 0) {
            timeFix = timeFix + 24000; // -2.000+24.000 = 22.000
            log("You cant set the timeFix to negative values. Setting into positive ones!", 1);
        }
        if(recreated)
            log("Recreated config for v" + config.getInt("DoNotChangeOrItWillEraseYourConfig.configVersion"), 1);
        
        log("Configured.", 0);
    }
    
    void startTasks() {
        BukkitScheduler sche = getServer().getScheduler();
        if(useMode == 0) {
            sche.scheduleSyncDelayedTask(this, new EnableWorldsTask(this), 20);
            sche.scheduleAsyncRepeatingTask(this, new CalculateTask(this), 40, M0CalcDelay);
            sche.scheduleSyncRepeatingTask(this, new SetTimeTask(this, 0), 45, M0UpdateDelay);
            started = true;
            log("NORMAL Mode (zero) is now running.", 0);
        } else if(useMode == 1) {
            sche.scheduleSyncDelayedTask(this, new EnableWorldsTask(this), 20);
            sche.scheduleAsyncDelayedTask(this, new CalculateTask(this), 40);
            sche.scheduleSyncRepeatingTask(this, new SetTimeTask(this, 1), 45, M1UpdateDelay * 72);
            started = true;
            log("3.6 Mode (one) is now running.", 0);
        } else {
            log("You can only set mode 0 or 1.", 1);
            started = false;
        }
    }
    
        /*
         * 0 - normal
         * 1 - warning
         * 2 - debug
         */
    void log(String msg, int mode) {
        if(mode == 0) // NORMAL
            getLogger().log(Level.INFO, msg);
        else if(mode == 1) // WARNING
            getLogger().log(Level.WARNING, msg);
        else if(mode == 2) { // DEBUG
            if(useDebugMode)
                getLogger().log(Level.INFO, "Debug: " + msg);
        }
    }
    
    boolean checkPerm(Player p, String perm) {
        if(usePermissions)
            if(p.hasPermission(perm))
                return true;
        return false;
    }
    
    int getTimeSec(String time) {
        int hour = Integer.parseInt(time.substring(0, 2));
        int min = Integer.parseInt(time.substring(3, 5));
        int sec = Integer.parseInt(time.substring(6, 8));
        if(useDebugTime) {
            return ((debugHour * 60 * 60) + (debugMin * 60) + debugSec);
        }
        return ((hour * 60 * 60) + (min * 60) + sec);
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

class EnableWorldsTask implements Runnable {
    private RealTime plugin;
    public EnableWorldsTask(RealTime plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            plugin.enabledWorlds = toWorldList(plugin.getConfig().getStringList("config.worldList"));
            plugin.log("Loaded WorldList: " + plugin.enabledWorlds.toString(), 0);
        } catch(NullPointerException e) {
            plugin.enabledWorlds = toWorldList(toStringList(plugin.getServer().getWorlds())); // I know, its ugly... But this will allow only NORMAL (not nether or the end) maps
            plugin.log("Couldn't use WorldList from config.yml, using: " + plugin.enabledWorlds.toString(), 1);
        }
    }
    
    List<World> toWorldList(List<String> NameList) {
        List<World> worlds = new ArrayList();
        for(int x = 0; x < NameList.size(); x++) {
            World w = plugin.getServer().getWorld(NameList.get(x));
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