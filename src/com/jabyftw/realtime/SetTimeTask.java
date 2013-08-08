package com.jabyftw.realtime;

import com.jabyftw.realtime.RealTime;
import org.bukkit.World;

    public class SetTimeTask implements Runnable {
        private RealTime plugin;
        
        public SetTimeTask(RealTime plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            if(plugin.pvpTime) {
                // 500 ticks PVP Time
                if(plugin.resultedTime > (plugin.pvpStart - 5) && plugin.resultedTime < (plugin.pvpStart + 5)) {
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.pvpStart + 4);
                // Normal DAY time
                } else if (plugin.resultedTime > (plugin.pvpStart + 6) && plugin.resultedTime < (plugin.pvpEnd - 6)) {
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.resultedTime);
                // 12500 ticks PVP Time
                } else if (plugin.resultedTime > (plugin.pvpEnd - 5) && plugin.resultedTime < (plugin.pvpEnd + 5)){
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.pvpEnd + 4);
                // Normal NIGHT time
                } else if (plugin.resultedTime > (plugin.pvpEnd + 6) && plugin.resultedTime < (plugin.pvpStart -6)) {
                    for (World w : plugin.enabledWorlds)
                        w.setFullTime(plugin.resultedTime);
                }
            } else {
                // No pvpTimeCompatibility
                for (World w : plugin.enabledWorlds)
                    w.setFullTime(plugin.resultedTime);
            }
        }
    }