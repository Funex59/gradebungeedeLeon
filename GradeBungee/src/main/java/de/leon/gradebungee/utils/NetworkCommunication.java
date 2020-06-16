package de.leon.gradebungee.utils;

import de.leon.gradebungee.GradeBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Internal use only!
 */
public class NetworkCommunication {

    public static final String NETWORK_CHANNEL = "perms:global";

    public static void sendToServer(ServerInfo serverInfo, String channel, String... messages) throws IOException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(stream);

        output.writeUTF(channel);
        for (String entry : messages) output.writeUTF(entry);

        serverInfo.sendData(NETWORK_CHANNEL, stream.toByteArray());
        GradeBungee.logMessage("§7Sent Network-Message (Server: " + serverInfo.getName() + ", Channel: " + channel + ")");

    }

    public static void sendToNetwork(String channel, String... messages) throws IOException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(stream);

        output.writeUTF(channel);
        for (String entry : messages) output.writeUTF(entry);

        ProxyServer.getInstance().getServers().values().forEach(entry -> entry.sendData(NETWORK_CHANNEL, stream.toByteArray()));
        GradeBungee.logMessage("§7Sent Network-Message (Channel: " + channel + ")");

    }
}
