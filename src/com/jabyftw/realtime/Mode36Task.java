package com.jabyftw.realtime;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class Mode36Task implements Runnable {
    private RealTime plugin;

    public Mode36Task(RealTime plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.resultedTime = plugin.resultedTime + 1;
        if(plugin.resultedTime > 24000) // Reset the timeline
            plugin.resultedTime = 0;
        if(plugin.usePlayerTime) {
            for(World w : plugin.enabledWorlds)
                for(Player p : w.getPlayers())
                    p.setPlayerTime(plugin.resultedTime, false);
            if(plugin.debug)
                plugin.getLogger().info("Resulted - usePlayerTime: " + plugin.resultedTime);
        } else {
            if(plugin.usePVPTime) {
                 // 500 ticks PVP Time
                if(plugin.resultedTime > (plugin.pvpStart - 5) && plugin.resultedTime < (plugin.pvpStart + 5))
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.pvpStart + 4);
                // Normal day time
                else if (plugin.resultedTime > (plugin.pvpStart + 6) && plugin.resultedTime < (plugin.pvpEnd - 6))
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.resultedTime);
                // 12500 ticks PVP Time
                else if (plugin.resultedTime > (plugin.pvpEnd - 5) && plugin.resultedTime < (plugin.pvpEnd + 5))
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.pvpEnd + 4);
                // Normal night time
                else if (plugin.resultedTime > (plugin.pvpEnd + 6) && plugin.resultedTime < (plugin.pvpStart -6))
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.resultedTime);
            } else {
                for(World w : plugin.enabledWorlds)
                    w.setFullTime(plugin.resultedTime);
                if(plugin.debug)
                    plugin.getLogger().info("Resulted - !usePlayerTime: " + plugin.resultedTime);
            }
        }
    }
}