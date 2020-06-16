package de.leon.gradebungee.utils;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.leon.gradebungee.GradeBungee;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Internal use only!
 */
@Getter
public class PermissionCache {

    private List<PermissionGroup> cachedGroups = new ArrayList<>();
    private Map<UUID, PermissionUser> cachedUsers = new HashMap<>();

    public ListenableFuture<?> cacheGroups() {

        getCachedGroups().clear();

        return GradeBungee.getInstance().getExecutorService().submit(() -> {

            PreparedStatement statement = null;
            try {

                statement = GradeBungee.getConnection().prepareStatement("SELECT * FROM `perms_groups`");
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    getCachedGroups().add(new PermissionGroup(rs.getString("name").toUpperCase(), rs.getString("prefix"), new Gson().fromJson(rs.getString("permissions"), new TypeToken<List<String>>() {
                    }.getType())));
                }
                getCachedGroups().forEach(group -> {
                    if (group.getPermissions() == null) group.setPermissions(new ArrayList<>());
                });
                if (getCachedGroups().isEmpty()) {
                    GradeBungee.logError("§cNo Groups loaded! This could lead to Errors!");
                }
            } catch (SQLException e) {
                GradeBungee.logError("§7Couldn't cache Groups (" + e.getMessage() + ")");
                e.printStackTrace();
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                    }
                }
            }
        });
    }

    public PermissionGroup getGroup(String name) {
        if (name == null) return null;
        return getCachedGroups().stream().filter(group -> group.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void cacheGroup(PermissionGroup group) {
        getCachedGroups().add(group);
    }

    public void uncacheGroup(PermissionGroup group) {
        getCachedGroups().remove(group);
    }

    public PermissionUser getUser(ProxiedPlayer p) {
        if (p == null) return null;
        return getUser(p.getUniqueId());
    }

    public PermissionUser getUser(UUID uuid) {
        return getCachedUsers().get(uuid);
    }

    public void cacheUser(PermissionUser user) {
        getCachedUsers().put(user.getUuid(), user);
    }

    public void uncacheUser(UUID uuid) {
        getCachedUsers().remove(uuid);
    }

    public void reloadGlobal() {
        try {
            NetworkCommunication.sendToNetwork("reloadGlobal");
        } catch (IOException e) {
            GradeBungee.logError("§cCouldn't send Network-Message (" + e.getMessage() + ")");
            e.printStackTrace();
        }
    }
}
