package de.leon.gradebungee.utils.configs;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import de.leon.gradebungee.GradeBungee;
import de.leon.gradebungee.utils.PermissionGroup;
import de.leon.gradebungee.utils.PermissionUser;
import de.leon.gradebungee.utils.UserNotFoundException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Internal use only!
 */
@RequiredArgsConstructor
public class MySQL {

    @NonNull
    private String host, user, password, database;

    @Getter
    private Connection connection;

    public void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":3306/" + this.database + "?autoReconnect=true", this.user, this.password);
    }

    public void createGroup(PermissionGroup group) {
        GradeBungee.getInstance().getExecutorService().execute(() -> {

            PreparedStatement statement = null;
            try {

                statement = GradeBungee.getConnection().prepareStatement("INSERT INTO `perms_groups` (`name`, `prefix`, `permissions`) VALUES (?, ?, ?)");
                statement.setString(1, group.getName());
                statement.setString(2, group.getPrefix());
                statement.setString(3, new Gson().toJson(group.getPermissions()));

                statement.executeUpdate();

            } catch (SQLException e) {
                GradeBungee.logError("§7Couldn't create Group (" + e.getMessage() + ")");
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

    public void updateGroup(PermissionGroup group) {
        GradeBungee.getInstance().getExecutorService().execute(() -> {

            PreparedStatement statement = null;
            try {

                statement = GradeBungee.getConnection().prepareStatement("UPDATE `perms_groups` SET `prefix` = ?, `permissions` = ? WHERE `name` = ?");
                statement.setString(1, group.getPrefix());
                statement.setString(2, new Gson().toJson(group.getPermissions()));
                statement.setString(3, group.getName());

                statement.executeUpdate();

            } catch (SQLException e) {
                GradeBungee.logError("§7Couldn't update Group (" + e.getMessage() + ")");
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

    public void deleteGroup(PermissionGroup group) {
        GradeBungee.getInstance().getExecutorService().execute(() -> {

            PreparedStatement statement = null;
            try {

                statement = GradeBungee.getConnection().prepareStatement("DELETE FROM `perms_groups` WHERE `name` = ?");
                statement.setString(1, group.getName());

                statement.executeUpdate();

            } catch (SQLException e) {
                GradeBungee.logError("§7Couldn't delete Group (" + e.getMessage() + ")");
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

    public void loadAndCacheSync(ProxiedPlayer p) throws UserNotFoundException {
        PermissionUser user = null;
        try {
            user = loadUser(p.getUniqueId()).get(15, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new UserNotFoundException(e.getMessage());
        }
        if (user == null) return;
        GradeBungee.getInstance().getPermissionCache().cacheUser(user);
        user.addBungeePermissions(p);
    }

    public ListenableFuture<PermissionUser> loadUser(UUID uuid) {
        return GradeBungee.getInstance().getExecutorService().submit(() -> {

            PreparedStatement statement = null;
            try {

                statement = GradeBungee.getConnection().prepareStatement("SELECT * FROM `perms_users` WHERE `uuid` = ?");
                statement.setString(1, uuid.toString());

                ResultSet rs = statement.executeQuery();

                if (rs.next()) {

                    PermissionUser user = new PermissionUser(uuid);
                    PermissionGroup group = GradeBungee.getInstance().getPermissionCache().getGroup(rs.getString("group"));

                    if (group != null) user.setGroup(group);
                    else user.setDefaultGroup();

                    return user;
                }
            } catch (SQLException e) {
                GradeBungee.logError("§7Couldn't load User (" + e.getMessage() + ")");
                e.printStackTrace();
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                    }
                }
            }
            return null;
        });
    }

    public void registerAndCache(PermissionUser user) {
        GradeBungee.getInstance().getPermissionCache().cacheUser(user);
        register(user);
    }

    public void register(PermissionUser user) {

        PreparedStatement statement = null;
        try {

            statement = GradeBungee.getConnection().prepareStatement("INSERT INTO `perms_users` (`uuid`, `group`) VALUES (?, ?)");
            statement.setString(1, user.getUuid().toString());
            statement.setString(2, user.getGroup().getName());

            statement.executeUpdate();

        } catch (SQLException e) {
            GradeBungee.logError("§7Couldn't register User (" + e.getMessage() + ")");
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public void updateUser(PermissionUser user) {
        GradeBungee.getInstance().getExecutorService().execute(() -> {

            PreparedStatement statement = null;
            try {

                statement = GradeBungee.getConnection().prepareStatement("UPDATE `perms_users` SET `group` = ? WHERE `uuid` = ?");
                statement.setString(1, user.getGroup().getName());
                statement.setString(2, user.getUuid().toString());

                statement.executeUpdate();

            } catch (SQLException e) {
                GradeBungee.logError("§7Couldn't update User (" + e.getMessage() + ")");
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
}
