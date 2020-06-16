package de.leon.gradebungee.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PermissionGroup {

    private String name;
    private String prefix;
    private List<String> permissions;

    /**
     *
     * Adds a new permission to a group locally.
     *
     * @param permission The permission that needs to be added.
     * @return If the permission was successfully added.
     */
    public boolean addPermission(String permission) {
        if (permissions.contains(permission.toLowerCase())) return false;
        return permissions.add(permission.toLowerCase());
    }

    /**
     *
     * Removes a permission from a group locally.
     *
     * @param permission The permission that needs to be removed.
     * @return If the permission was successfully removed.
     */
    public boolean removePermission(String permission) {
        if (!permissions.contains(permission.toLowerCase())) return false;
        return permissions.remove(permission.toLowerCase());
    }
}
