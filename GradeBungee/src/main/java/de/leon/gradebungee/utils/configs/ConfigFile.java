package de.leon.gradebungee.utils.configs;

import de.leon.gradebungee.GradeBungee;
import lombok.Getter;
import net.cubespace.Yamler.Config.YamlConfig;

import java.io.File;

/**
 * Internal use only!
 */
@Getter
public class ConfigFile extends YamlConfig {

    private String prefix = "§6Perms §7» ", defaultGroup = "PLAYER", spaceFormat = "$_", host = "localhost", user = "root", password = "password", database = "perms";

    public ConfigFile() {
        CONFIG_FILE = new File(GradeBungee.getInstance().getDataFolder(), "config.yml");
    }
}
