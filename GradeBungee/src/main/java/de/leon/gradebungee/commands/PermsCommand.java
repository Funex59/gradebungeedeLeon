package de.leon.gradebungee.commands;

import de.leon.gradebungee.GradeBungee;
import de.leon.gradebungee.utils.PermissionGroup;
import de.leon.gradebungee.utils.PermissionUser;
import de.leon.gradebungee.utils.UserNotFoundException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Internal use only!
 */
public class PermsCommand extends Command {

    public PermsCommand() {
        super("perms");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (!sender.hasPermission("perms." + args[0].toLowerCase()) && !sender.hasPermission("*")) {
                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cYou don't have enough Permissions to use this Command."));
                return;
            }
        }
        if (args.length == 0) {
            sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "================ §6Perms §7================"));
            sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§a/perms list"));
            sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§a/perms reload"));
            sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§a/perms help"));
            sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§b/perms list <Group>"));
            sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§b/perms info <Player>"));
            sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§b/perms create <Group>"));
            sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§b/perms delete <Group>"));
            sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§3/perms add <Group> <Permission>"));
            sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§3/perms remove <Group> <Permission>"));
            sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§3/perms prefix <Group> <Prefix>"));
            sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§c/perms set <Player> <Group>"));
            sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "================ §6Perms §7================"));
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {

                boolean errorOnGroupLoad = false;
                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cReloading §7Groups and Players..."));
                ProxyServer.getInstance().getPlayers().forEach(entry -> {
                    PermissionUser user = GradeBungee.getInstance().getPermissionCache().getUser(entry);
                    if (user != null && user.getGroup() != null) user.removeBungeePermissions(entry);
                });
                try {
                    GradeBungee.getInstance().getPermissionCache().cacheGroups().get(15, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + " §4Error while reloading the Groups! Exiting..."));
                    errorOnGroupLoad = true;
                    e.printStackTrace();
                }
                if (errorOnGroupLoad) return;
                ProxyServer.getInstance().getPlayers().forEach(entry -> {
                    try {
                        GradeBungee.getInstance().getMySQL().loadAndCacheSync(entry);
                    } catch (UserNotFoundException e) {
                        sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + " §7- §3Error while reloading " + entry.getName()));
                        e.printStackTrace();
                    }
                });

                GradeBungee.getInstance().getPermissionCache().reloadGlobal();
                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§aReloaded §7Groups and Players!"));

            } else if (args[0].equalsIgnoreCase("list")) {
                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "=============== §6Groups §7==============="));
                GradeBungee.getInstance().getPermissionCache().getCachedGroups().forEach(entry -> sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + " §7- §e" + entry.getName() + " §7(" + entry.getPrefix() + "§7)")));
                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "=============== §6Groups §7==============="));
            } else if (args[0].equalsIgnoreCase("help")) {

                TextComponent textComponent = new TextComponent();
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.fiverr.com/leonkttndck"));
                textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§eClick me!")));
                textComponent.setText("§eLeonPrefix");

                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "================ §6Help §7================"));
                sender.sendMessage(new TextComponent(GradeBungee.getPrefix() + "Developer: "), textComponent);
                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "Good To Know:"));
                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + " - Updates are active after §e/perms reload"));
                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + " - Spaces in the Prefix can be set with §e" + GradeBungee.getInstance().getConfigFile().getSpaceFormat()));
                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "================ §6Help §7================"));

            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list")) {

                String name = args[1].toUpperCase();
                PermissionGroup group = GradeBungee.getInstance().getPermissionCache().getGroup(name);

                if (group == null) {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cThis Group doesn't exists."));
                    return;
                }

                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§7Permissions of §e" + group.getName() + "§7:"));
                group.getPermissions().forEach(s -> sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + " §7- §e" + s)));
                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§7Prefix of §e" + group.getName() + "§7: " + group.getPrefix()));

            } else if (args[0].equalsIgnoreCase("info")) {

                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
                PermissionUser user = GradeBungee.getInstance().getPermissionCache().getUser(target);

                if (target == null) {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cThis Player isn't connected to the Server."));
                    return;
                }
                if (user == null) {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cThis Player isn't registered in the Database."));
                    return;
                }

                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§e" + target.getName() + " §7has Group §e" + user.getGroup().getName() + "§7."));

            } else if (args[0].equalsIgnoreCase("create")) {

                String name = args[1].toUpperCase();

                if (name.length() >= 64) {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cThis Name is too long."));
                    return;
                }
                if (GradeBungee.getInstance().getPermissionCache().getGroup(name) != null) {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cThis Group already exists."));
                    return;
                }

                PermissionGroup group = new PermissionGroup(name, "NO_PREFIX", new ArrayList<>());

                GradeBungee.getInstance().getPermissionCache().cacheGroup(group);
                GradeBungee.getInstance().getMySQL().createGroup(group);

                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "Group '§e" + name + "§7' has been §acreated§7."));

            } else if (args[0].equalsIgnoreCase("delete")) {

                String name = args[1].toUpperCase();
                PermissionGroup group = GradeBungee.getInstance().getPermissionCache().getGroup(name);

                if (group == null) {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cThis Group doesn't exists."));
                    return;
                }

                GradeBungee.getInstance().getPermissionCache().uncacheGroup(group);
                GradeBungee.getInstance().getMySQL().deleteGroup(group);

                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "Group '§e" + name + "§7' has been §cdeleted§7."));

            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add")) {

                PermissionGroup group = GradeBungee.getInstance().getPermissionCache().getGroup(args[1]);
                String permission = args[2].toLowerCase();

                if (group == null) {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cThis Group doesn't exists."));
                    return;
                }
                if (group.addPermission(permission)) {
                    GradeBungee.getInstance().getMySQL().updateGroup(group);
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "Added '§e" + permission + "§7' to §e" + group.getName() + "§7."));
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cThis Permission has already been added to this Group."));
                }
            } else if (args[0].equalsIgnoreCase("remove")) {

                PermissionGroup group = GradeBungee.getInstance().getPermissionCache().getGroup(args[1]);
                String permission = args[2].toLowerCase();

                if (group == null) {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cThis Group doesn't exists."));
                    return;
                }
                if (group.removePermission(permission)) {
                    GradeBungee.getInstance().getMySQL().updateGroup(group);
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "Removed '§e" + permission + "§7' from §e" + group.getName() + "§7."));
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cThis Permission doesn't exists in this Group."));
                }
            } else if (args[0].equalsIgnoreCase("prefix")) {

                PermissionGroup group = GradeBungee.getInstance().getPermissionCache().getGroup(args[1]);
                String prefix = args[2].replace(GradeBungee.getInstance().getConfigFile().getSpaceFormat(), " ");

                if (prefix.length() > 64) {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cThis Prefix is too long."));
                    return;
                }
                if (prefix.length() > 16) {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§7This Prefix may be too long for the Tablist."));
                }
                if (group == null) {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cThis Group doesn't exists."));
                    return;
                }

                group.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix));
                GradeBungee.getInstance().getMySQL().updateGroup(group);

                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "New Prefix of §e" + group.getName() + "§7: " + group.getPrefix()));

            } else if (args[0].equalsIgnoreCase("set")) {

                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
                PermissionGroup group = GradeBungee.getInstance().getPermissionCache().getGroup(args[2]);
                PermissionUser user = GradeBungee.getInstance().getPermissionCache().getUser(target);

                if (target == null) {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cThis Player isn't connected to the Server."));
                    return;
                }
                if (group == null) {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cThis Group doesn't exists."));
                    return;
                }
                if (user == null) {
                    sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "§cThis Player isn't registered in the Database."));
                    return;
                }

                user.removeBungeePermissions(target);
                user.setGroup(group);
                user.updateUserGlobal(target);
                GradeBungee.getInstance().getMySQL().updateUser(user);
                user.addBungeePermissions(target);

                sender.sendMessage(TextComponent.fromLegacyText(GradeBungee.getPrefix() + "Set Group for §e" + target.getName() + "§7 to §e" + group.getName() + "§7."));

            }
        }
    }
}
