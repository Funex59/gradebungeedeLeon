package de.leon.gradebungee.listener;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import de.leon.gradebungee.GradeBungee;
import de.leon.gradebungee.utils.FutureCallbackAdapter;
import de.leon.gradebungee.utils.PermissionUser;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Internal use only!
 */
public class LoginListener implements Listener {

    @Getter
    private final Map<UUID, SettableFuture<PermissionUser>> userCacheFuture = new HashMap<>();

    @EventHandler
    public void onLogin(LoginEvent e) {

        UUID uuid = e.getConnection().getUniqueId();
        getUserCacheFuture().put(uuid, SettableFuture.create());

        Futures.addCallback(GradeBungee.getInstance().getMySQL().loadUser(uuid), new FutureCallbackAdapter<PermissionUser>() {
            @Override
            public void onSuccess(@Nullable PermissionUser aUser) {
                PermissionUser user;
                if (aUser == null) {
                    GradeBungee.logMessage("Registering a new User...");
                    GradeBungee.getInstance().getMySQL().registerAndCache(user = PermissionUser.buildDefaultUser(uuid));
                    user.notifyRegistration();
                } else {
                    GradeBungee.getInstance().getPermissionCache().cacheUser(user = aUser);
                }
                getUserCacheFuture().get(uuid).set(user);
            }
        }, GradeBungee.getInstance().getExecutorService());
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent e) {

        ProxiedPlayer p = e.getPlayer();
        Futures.addCallback(getUserCacheFuture().get(p.getUniqueId()), new FutureCallbackAdapter<PermissionUser>() {
            @Override
            public void onSuccess(@Nullable PermissionUser user) {
                if (user != null) user.addBungeePermissions(p);
                getUserCacheFuture().remove(p.getUniqueId());
            }
        }, GradeBungee.getInstance().getExecutorService());
    }
}
