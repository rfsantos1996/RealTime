package com.jabyftw.realtime;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class SetPTimeTask implements Runnable {
    private RealTime plugin;
    
    public SetPTimeTask(RealTime plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        for(World w : plugin.enabledWorlds) {
            for(Player p : w.getPlayers()) {
                p.setPlayerTime(plugin.resultedTime, false);
                if(plugin.debug)
                    plugin.getLogger().info("Player/ptime/World/rtime: " + p.getName() + "/" + plugin.resultedTime + "/" + p.getWorld().getName() + "/" + p.getWorld().getTime());
            }
        }
    }
}