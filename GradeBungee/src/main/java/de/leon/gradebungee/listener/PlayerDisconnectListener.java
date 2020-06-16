package de.leon.gradebungee.listener;

import de.leon.gradebungee.GradeBungee;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Internal use only!
 */
public class PlayerDisconnectListener implements Listener {

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent e) {
        GradeBungee.getInstance().getPermissionCache().uncacheUser(e.getPlayer().getUniqueId());
    }
}
