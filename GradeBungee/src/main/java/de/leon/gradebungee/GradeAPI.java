package de.leon.gradebungee;

import com.google.common.util.concurrent.ListenableFuture;
import de.leon.gradebungee.utils.PermissionGroup;
import de.leon.gradebungee.utils.PermissionUser;
import de.leon.gradebungee.utils.UserNotFoundException;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/**
 * API Class for future use in other plugins.
 */
public class GradeAPI {

    /**
     * Get the stored permission information of a online player. May return null, when user isn't online.
     *
     * @param uuid The uuid of an online player.
     * @return The permission information stored in a PermissionUser.
     */
    public static PermissionUser getPermissionUser(UUID uuid) {
        return GradeBungee.getInstance().getPermissionCache().getUser(uuid);
    }

    /**
     * Get the stored permission information of a existing group. May return null, when group doesn't exist.
     *
     * @param name The name of the group.
     * @return The permission information stored in a PermissionGroup.
     */
    public static PermissionGroup getPermissionGroup(String name) {
        return GradeBungee.getInstance().getPermissionCache().getGroup(name);
    }

    /**
     * Reloads a user's permission group from the database.
     *
     * @param p The user that should be reloaded
     * @throws UserNotFoundException Gets thrown if no user was found in 15 seconds.
     */
    public static void reloadUser(ProxiedPlayer p) throws UserNotFoundException {
        GradeBungee.getInstance().getMySQL().loadAndCacheSync(p);
    }

    /**
     * Reloads all groups and permissions from the database.<br>
     * Runs asynchronously! Use the ListenableFuture if you want to make it synchronized.
     *
     * @return ListenableFuture for synchronized use.
     */
    public static ListenableFuture<?> reloadGroups() {
        return GradeBungee.getInstance().getPermissionCache().cacheGroups();
    }
}
