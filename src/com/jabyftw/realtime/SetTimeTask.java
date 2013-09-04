package com.jabyftw.realtime;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class SetTimeTask implements Runnable {
    private RealTime plugin;
    private int mode;
    
    public SetTimeTask(RealTime plugin, int mode) {
        this.plugin = plugin;
        this.mode = mode;
    }

    @Override
    public void run() {
        /*
         * NORMAL MODE
         */
        if(mode == 0) {
            if(plugin.usePVPTime) {
                 // 500 ticks PVP Time
                if(plugin.mcTime > (plugin.pvpStart - 5) && plugin.mcTime < (plugin.pvpStart + 5))
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.pvpStart + 4);
                // Normal day time
                else if (plugin.mcTime > (plugin.pvpStart + 6) && plugin.mcTime < (plugin.pvpEnd - 6))
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.mcTime);
                // 12500 ticks PVP Time
                else if (plugin.mcTime > (plugin.pvpEnd - 5) && plugin.mcTime < (plugin.pvpEnd + 5))
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.pvpEnd + 4);
                // Normal night time
                else if (plugin.mcTime > (plugin.pvpEnd + 6) && plugin.mcTime < (plugin.pvpStart -6))
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.mcTime);
            } else {
                if(plugin.usePlayerTime) {
                    for(World w : plugin.enabledWorlds)
                        for(Player p : w.getPlayers()) {
                          if(p.hasPermission("realtime.noptime")) {
                                plugin.log("NORMAL : Player have noptime - " + p.getName(), 2);
                                return;
                            }
                            p.setPlayerTime(plugin.mcTime, true);
                            plugin.log("NORMAL : PlayerTime : time - " + p.getName() + " | " + plugin.mcTime, 2);
                        }
                } else {
                    for(World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.mcTime);
                    plugin.log("NORMAL : World : time - " + plugin.mcTime, 2);
                }
            }
            /*
             * MODE 3.6
             */
        } else if(mode == 1) {
            plugin.mcTime = plugin.mcTime + 1;
            if(plugin.mcTime > 24000)
                plugin.mcTime = 0;
            
            if(plugin.usePVPTime) {
                 // 500 ticks PVP Time
                if(plugin.mcTime > (plugin.pvpStart - 5) && plugin.mcTime < (plugin.pvpStart + 5))
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.pvpStart + 4);
                // Normal day time
                else if (plugin.mcTime > (plugin.pvpStart + 6) && plugin.mcTime < (plugin.pvpEnd - 6))
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.mcTime);
                // 12500 ticks PVP Time
                else if (plugin.mcTime > (plugin.pvpEnd - 5) && plugin.mcTime < (plugin.pvpEnd + 5))
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.pvpEnd + 4);
                // Normal night time
                else if (plugin.mcTime > (plugin.pvpEnd + 6) && plugin.mcTime < (plugin.pvpStart -6))
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.mcTime);
            } else {
                if(plugin.usePlayerTime) {
                    for(World w : plugin.enabledWorlds)
                        for(Player p : w.getPlayers()) {
                            if(p.hasPermission("realtime.noptime")) {
                                plugin.log("MODE36 : Player have noptime - " + p.getName(), 2);
                                return;
                            }
                            p.setPlayerTime(plugin.mcTime, true);
                            plugin.log("MODE36 : PlayerTime : time - " + p.getName() + " | " + plugin.mcTime, 2);
                        }
                } else {
                    for(World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.mcTime);
                    plugin.log("MODE36 : World : time - " + plugin.mcTime, 2);
                }
            }
        }
    }
}