/*
 * CODE FROM https://github.com/keybordpiano459/kEssentials/blob/master/kEssentials/src/me/KeybordPiano459/kEssentials/config/kConfig.java
 * AND http://wiki.bukkit.org/Introduction_to_the_New_Configuration#Implementation_for_Reloading
 */
package com.jabyftw.realtime;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class NewConfig {

    private final RealTime plugin;
    public FileConfiguration ConfigConfiguration;
    public File ConfigFile;

    public NewConfig(RealTime plugin) {
        this.plugin = plugin;
        ConfigFile = new File(plugin.getDataFolder(), "config.yml");
        ConfigConfiguration = YamlConfiguration.loadConfiguration(ConfigFile);
    }

    public void createConfig() {
        ConfigFile = new File(plugin.getDataFolder(), "config.yml");
        if (!ConfigFile.exists()) {
            try {
                ConfigFile.createNewFile();
                generateConfig();
            } catch (IOException e) {
                plugin.log("Could not create the config file: " + e, 1);
            }
        }
    }

    public void deleteConfig() {
        ConfigFile = new File(plugin.getDataFolder(), "config.yml");
        if (ConfigFile.exists()) {
            ConfigFile.delete();
        }
    }

    public void generateConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            FileWriter w = new FileWriter(configFile);
            w(w, "config:");
            w(w, "  modeZero:");
            w(w, "    # The calculation time in ticks (20 ticks = 1 second)");
            w(w, "    CalcDelayInTicks: 40");
            w(w, "    # The update time in ticks (20 ticks = 1 second)");
            w(w, "    UpdateDelayInTicks: 40");
            w(w, "");
            w(w, "  modeOne:");
            w(w, "    # The update time in 3.6 seconds (x * 3.6 sec)");
            w(w, "    UpdateDelayIn3dot6Seconds: 1");
            w(w, "");
            w(w, "  # This will enable the use of PlayerTime instead of changing the real time");
            w(w, "  # !! You can disable that for any player using the permission: realtime.noptime !!");
            w(w, "  usePlayerTime: true");
            w(w, "  # Mode = 0 - Normal mode - time is calculated and set");
            w(w, "  # Mode = 1 - Mode 3.6 - time is calculated ONCE and added after some time");
            w(w, "  # Mode 1 for +18 TPS servers, or it'll change the time scale");
            w(w, "  ModeBeingUsed: 0");
            w(w, "  # This will decide if the plugin will automaticly enable when loading");
            w(w, "  # You can manually enable/disable using '/realtime start/stop'");
            w(w, "  enableOnLoad: true");
            w(w, "  # This will enable/disable the permissions - even for OPs");
            w(w, "  # - This DOENST APPLY for commands! -");
            w(w, "  permissionActionsInGameEnabled: false");
            w(w, "  # This will fix your time in case your server is on another country, for example");
            w(w, "  # Like, your city is 8pm, and your server is 9pm, you add 1000 to this and it'll be fixed once you restart");
            w(w, "  timeFixInTicks: 0");
            w(w, "  # The list of NORMAL worlds (NO NETHER OR THE END AVALIABLE)");
            w(w, "  # - THE WORLD LIST IS CASE SENSITIVE! -");
            w(w, "  # If you set it 'null', or leave it, the plugin will use all NORMAL worlds (not nether or the end).");
            w(w, "  # This will probably conflit with MultiWorld plugins, BUT I created a system so you dont need to manually edit the plugin.yml to softdepend");
            w(w, "  worldList:");
            w(w, "  - world");
            w(w, "\n");
            w(w, "PVPTime:");
            w(w, "  # This will enable or disable your PVPTime compatibility, if you dont enable it, the PVP will be announced more than one time");
            w(w, "  enabled: false");
            w(w, "  # Ajust with your PVPTime's configuration");
            w(w, "  startTime: 500");
            w(w, "  endTime: 12500");
            w(w, "\n");
            w(w, "debug:");
            w(w, "  useDebugMode: false");
            w(w, "  # You can test your server for certain clocks, just enable this (even if useDebugMode is false), set the time and restart");
            w(w, "  DebugTime:");
            w(w, "    enabled: false");
            w(w, "    hour: 0");
            w(w, "    min: 0");
            w(w, "    sec: 0");
            w(w, "\n");
            w(w, "DoNotChangeOrItWillEraseYourConfig:");
            w(w, "  configVersion: " + plugin.cVersion);
            w.close();
            reloadConfig();
        } catch (IOException e) {
            plugin.log("Could not generate the config file: " + e, 1);
        }
    }

    private void w(FileWriter writer, String string) throws IOException {
        writer.write(string + "\n");
    }

    public void reloadConfig() {
        if (!ConfigFile.exists()) {
            ConfigFile = new File(plugin.getDataFolder(), "config.yml");
        }
        ConfigConfiguration = YamlConfiguration.loadConfiguration(ConfigFile);

        InputStream defConfigStream = plugin.getResource("config.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            ConfigConfiguration.setDefaults(defConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (ConfigConfiguration == null) {
            reloadConfig();
        }
        return ConfigConfiguration;
    }

    public void saveConfig() {
        if (ConfigConfiguration == null || ConfigFile == null) {
            return;
        }

        try {
            ConfigConfiguration.save(ConfigFile);
        } catch (IOException e) {
            plugin.log("Could not save the config file to the disk: " + e, 1);
        }
    }
}
