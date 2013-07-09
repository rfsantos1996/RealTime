package com.jabyftw.realtime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class RealTime extends JavaPlugin {
    int updateTime = 1;
    int calcTime = 20;
    List<World> worldListWorld;
    static int calculatedTime = 0;
    boolean debug = false;
    int fixTimeInTicks = 0;
    boolean pvpTimeCompatibility = false;
    int startTime = 500;
    int endTime = 12500;
    int resulted = 0;
    boolean testing = false;
    int testingType = 1;
    
    @Override
    public void onEnable() {
        setConfig();
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new CalculateTime(this), calcTime, calcTime);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new SetTime(this), updateTime, updateTime);
        getLogger().info("Registred tasks");
    }
    
    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("Unregistered tasks and closing");
    }
    
    void setConfig() {
        FileConfiguration config = getConfig();
        config.addDefault("config.updateTime", 1);
        config.addDefault("config.calcTime", 20);
        config.addDefault("config.enabledWorlds", "world");
        config.addDefault("config.fixTimeInTicks", 0);
        config.addDefault("pvpTimeCompatibility.enabled", false);
        config.addDefault("pvpTimeCompatibility.startTime", 500);
        config.addDefault("pvpTimeCompatibility.endTime", 12500);
        config.addDefault("debug.enabled", false);
        config.addDefault("debug.testing", false);
        config.addDefault("debug.testingType", 1);
        config.options().copyDefaults(true);
        saveConfig();
        reloadConfig();
        updateTime = config.getInt("config.updateTime");
        calcTime = config.getInt("config.calcTime");
        worldListWorld = transform(config.getList("worlds.worldList"));
        fixTimeInTicks = config.getInt("config.fixTimeInTicks");
        pvpTimeCompatibility = config.getBoolean("pvpTimeCompatibility.enabled");
        startTime = config.getInt("pvpTimeCompatibility.startTime");
        endTime = config.getInt("pvpTimeCompatibility.endTime");
        debug = config.getBoolean("debug.enabled");
        testing = config.getBoolean("debug.testing");
        testingType = config.getInt("debug.testingType");
        getLogger().info("Configured!");
    }
    
    public List<World> transform(List<String> worldList) {
        List worlds = new ArrayList();
        for(String s : worldList) {
            worlds.add(getServer().getWorld(s));
        }
        return worlds;
    }
    
   /*
    * RUNNABLES
    */
    
    private class CalculateTime implements Runnable {
        private final JavaPlugin plugin;
        
        public CalculateTime(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            calculateTime();
        }

        private void calculateTime() {
            String time = new Date().toString().substring(11, 19);
            
            // (86400 / 3,6) - 6000
            if(testing) {
                if(testingType == 1) {
                    calculatedTime = startTime -1;
                } else if (testingType == 2) {
                    calculatedTime = endTime - 2;
                } else if (testingType != 1 && testingType != 2) {
                    testingType = 1;
                    getLogger().log(Level.WARNING, "testingType cant be different than 1 or 2");
                }
            } else {
                calculatedTime = (int) ((getTimeSec(time) / 3.6) - 6000) + (fixTimeInTicks); // -1000 will work, I hope
            }
        }
        
        public float getTimeSec(String time) {
            int hour = Integer.parseInt(time.substring(0, 2));
            int min = Integer.parseInt(time.substring(3, 5));
            int sec = Integer.parseInt(time.substring(6, 8));
            
            return (hour * 60 * 60) + (min * 60) + (sec);
        }
    }
    
    private class SetTime implements Runnable {
        private final JavaPlugin plugin;
       
        public SetTime(JavaPlugin plugin) {    
            this.plugin = plugin;
        }

        @Override
        public void run() {
            // MultiWorld support planned! (?) I need internet to learn how to do this ._.
           
            if(pvpTimeCompatibility) {
                // 500 ticks PVP Time
                if(calculatedTime > (startTime - 5) && calculatedTime < (startTime + 5)) {
                 for (World s : worldListWorld) {
                     s.setFullTime(startTime + 4);
                     resulted = startTime + 4;
                 }
                    
                // Normal DAY time
                } else if (calculatedTime > (startTime + 6) && calculatedTime < (endTime - 6)) {
                 for (World s : worldListWorld) {
                     s.setFullTime(calculatedTime);
                     resulted = calculatedTime;
                 }
                    
                // 12500 ticks PVP Time
                } else if (calculatedTime > (endTime - 5) && calculatedTime < (endTime + 5)){
                 for (World s : worldListWorld) {
                     s.setFullTime(endTime + 4);
                     resulted = endTime + 4;
                 }
                    
                // Normal NIGHT time
                } else if (calculatedTime > (endTime + 6) && calculatedTime < (startTime -6)) {
                 for (World s : worldListWorld) {
                     s.setFullTime(calculatedTime);
                     resulted = calculatedTime;
                 }
                }
            } else {
                // No pvpTimeCompatibility
                 for (World s : worldListWorld) {
                     s.setFullTime(calculatedTime);
                     resulted = calculatedTime;
                 }
            }
          if(debug)
              getLogger().info("Setted: " + resulted + " | Calced: " + calculatedTime + " | PVPTime: " + pvpTimeCompatibility);
        }
    }
}