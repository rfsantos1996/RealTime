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
        if(mode == 0) { // NORMAL MODE
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
                        for(Player p : w.getPlayers())
                            p.setPlayerTime(plugin.mcTime, false);
                } else {
                    for(World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.mcTime);
                }
            }
        } else if(mode == 1) { // MODE 3.6
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
                        for(Player p : w.getPlayers())
                            p.setPlayerTime(plugin.mcTime, false);
                } else {
                    for(World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.mcTime);
                }
            }
        }
    }
}