package com.jabyftw.realtime;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MyCommandExecutor implements CommandExecutor {

    private final RealTime plugin;

    public MyCommandExecutor(RealTime plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        } else if (args[0].equalsIgnoreCase("stop")) {
            if (sender.hasPermission("realtime.stop")) {
                if (!plugin.started) {
                    sender.sendMessage(ChatColor.YELLOW + "RealTime isn't started yet!");
                    return true;
                } else {
                    plugin.getServer().getScheduler().cancelTasks(plugin);
                    sender.sendMessage(ChatColor.YELLOW + "RealTime stoped!");
                    plugin.started = false;
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You dont have 'realtime.stop'!");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("start")) {
            if (sender.hasPermission("realtime.start")) {
                if (!plugin.started) {
                    plugin.startTasks();
                    sender.sendMessage(ChatColor.YELLOW + "RealTime running!");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "RealTime is already enabled!");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You dont have 'realtime.start'!");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("realtime.reload")) {
                if (plugin.started) {
                    plugin.getServer().getScheduler().cancelTasks(plugin);
                }
                plugin.started = false;
                plugin.setConfig();
                plugin.startTasks();
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You dont have 'realtime.reload'!");
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "RealTime " + ChatColor.DARK_RED + "v" + plugin.getDescription().getVersion());
            return true;
        }
    }
}
