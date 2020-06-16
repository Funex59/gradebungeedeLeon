package de.leon.gradebungee.utils;

import de.leon.gradebungee.GradeBungee;
import lombok.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class PermissionUser {

    @NonNull
    private final UUID uuid;

    @Setter
    private PermissionGroup group;

    /**
     *
     * Sets the default group for a newly registered player.
     *
     * @return The changed PermissionUser of the player.
     */
    public PermissionUser setDefaultGroup() {
        if (GradeBungee.getInstance().getDefaultGroup() != null) {
            setGroup(GradeBungee.getInstance().getDefaultGroup());
        } else {
            GradeBungee.logError("§cPlease specify an existing default Group in the Config!");
            throw new NoSuchElementException();
        }
        return this;
    }

    /**
     *
     * Adds all the permissions of the group to a player locally.
     *
     * @param p The player who receives the permissions.
     */
    public void addBungeePermissions(ProxiedPlayer p) {
        if (getGroup() == null) return;
        getGroup().getPermissions().forEach(s -> p.setPermission(s, true));
    }

    /**
     *
     * Removes all the permissions of the group from a player locally.
     *
     * @param p The player who loses the permissions.
     */
    public void removeBungeePermissions(ProxiedPlayer p) {
        if (getGroup() == null) return;
        getGroup().getPermissions().forEach(s -> p.setPermission(s, false));
    }

    /**
     * Notifies the server of a newly registered player.
     */
    public void notifyRegistration() {
        try {
            NetworkCommunication.sendToNetwork("notifyRegistration", getUuid().toString(), getGroup().getName());
        } catch (IOException e) {
            GradeBungee.logError("§cCouldn't send Network-Message (" + e.getMessage() + ")");
            e.printStackTrace();
        }
    }

    /**
     *
     * Send this message after a user received a new group to update them on the spigot server.
     *
     * @param p The player that should be updated.
     */
    public void updateUserGlobal(ProxiedPlayer p) {
        try {
            NetworkCommunication.sendToServer(p.getServer().getInfo(),"updateUser", getUuid().toString(), getGroup().getName());
        } catch (IOException e) {
            GradeBungee.logError("§cCouldn't send Network-Message (" + e.getMessage() + ")");
            e.printStackTrace();
        }
    }

    /**
     *
     * Builds a default object for a newly registered player.
     *
     * @param uuid The uuid of the new player.
     * @return The default PermissionUser of the new player.
     */
    public static PermissionUser buildDefaultUser(UUID uuid) {
        return new PermissionUser(uuid).setDefaultGroup();
    }
}
