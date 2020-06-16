package de.leon.gradebungee;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import de.leon.gradebungee.commands.PermsCommand;
import de.leon.gradebungee.listener.LoginListener;
import de.leon.gradebungee.listener.PlayerDisconnectListener;
import de.leon.gradebungee.utils.NetworkCommunication;
import de.leon.gradebungee.utils.PermissionCache;
import de.leon.gradebungee.utils.PermissionGroup;
import de.leon.gradebungee.utils.configs.ConfigFile;
import de.leon.gradebungee.utils.configs.MySQL;
import lombok.Getter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Internal use only!
 */
@Getter
public class GradeBungee extends Plugin {

    @Getter
    public static GradeBungee instance;

    private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    private ConfigFile configFile;

    private MySQL mySQL;

    private PermissionCache permissionCache;

    @Override
    public void onEnable() {

        instance = this;

        logMessage("§7Starting up §eGradeBungee§7...");

        try {
            logMessage("§7Loading Configuration File...");
            configFile = new ConfigFile();
            getConfigFile().init();
        } catch (InvalidConfigurationException e) {
            logError("§7Couldn't load Configuration File (" + e.getMessage() + ")");
            e.printStackTrace();
        }

        try {
            logMessage("§7Connection to MySQL...");
            mySQL = new MySQL(getConfigFile().getHost(), getConfigFile().getUser(), getConfigFile().getPassword(), getConfigFile().getDatabase());
            getMySQL().connect();
        } catch (SQLException e) {
            logError("§7Couldn't establish MySQL Connection (" + e.getMessage() + ")");
            e.printStackTrace();
        }

        try {
            logMessage("§7Loading MySQL Tables...");
            getMySQL().getConnection().setAutoCommit(false);
            getMySQL().getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS `perms_groups` (`name` VARCHAR(128) PRIMARY KEY, `prefix` VARCHAR(128), `permissions` LONGTEXT)").executeUpdate();
            getMySQL().getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS `perms_users` (`uuid` VARCHAR(128) PRIMARY KEY, `group` VARCHAR(128))").executeUpdate();
            getMySQL().getConnection().commit();
        } catch (SQLException e) {
            logError("§7Couldn't load MySQL Tables (" + e.getMessage() + ")");
            e.printStackTrace();
            try {
                getMySQL().getConnection().rollback();
            } catch (SQLException ex) {
            }
        } finally {
            try {
                getMySQL().getConnection().setAutoCommit(true);
            } catch (SQLException e) {
            }
        }

        try {
            logMessage("§7Loading Permission Groups..");
            permissionCache = new PermissionCache();
            getPermissionCache().cacheGroups().get(15, TimeUnit.SECONDS);
            for (PermissionGroup entry : getPermissionCache().getCachedGroups()) {
                logMessage("§7 - Loaded Group '§e" + entry.getName() + "§7'");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logError("§7Couldn't load Permission Groups (" + e.getMessage() + ")");
            e.printStackTrace();
        }

        ProxyServer.getInstance().getPluginManager().registerCommand(this, new PermsCommand());

        ProxyServer.getInstance().getPluginManager().registerListener(this, new LoginListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new PlayerDisconnectListener());

        ProxyServer.getInstance().registerChannel(NetworkCommunication.NETWORK_CHANNEL);

        logMessage("§7Finished loading §eGradeBungee§7!");

    }

    @Override
    public void onDisable() {
        logMessage("§7Shutting down §eGradeBungee§7...");
        try {
            if (!getMySQL().getConnection().isClosed()) getMySQL().getConnection().close();
        } catch (SQLException e) {
        }
        logMessage("§7Finished unloading §eGradeBungee§7! Good Night!");
    }

    public static String getPrefix() {
        return getInstance().getConfigFile().getPrefix();
    }

    public static Connection getConnection() {
        return getInstance().getMySQL().getConnection();
    }

    public static void logMessage(String legacyText) {
        ProxyServer.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText("§3LOG §7> " + legacyText));
    }

    public static void logError(String legacyText) {
        ProxyServer.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText("§4ERROR §7> " + legacyText));
    }

    public PermissionGroup getDefaultGroup() {
        return getPermissionCache().getGroup(getConfigFile().getDefaultGroup());
    }
}
