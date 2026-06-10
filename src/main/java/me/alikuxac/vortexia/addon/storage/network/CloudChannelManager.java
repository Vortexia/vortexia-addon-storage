// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.alikuxac.vortexia.addon.storage.StorageAddon;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class CloudChannelManager {

    private final StorageAddon addon;
    private final Map<String, CloudChannel> channels = new HashMap<>();

    public CloudChannelManager(StorageAddon addon) {
        this.addon = addon;
        load();
    }

    public static class CloudChannel {
        public String id;
        public String name;
        public UUID owner;
        public String password;
        public Set<UUID> members = new HashSet<>();
        public Set<UUID> blocked = new HashSet<>();

        public CloudChannel(String id, String name, UUID owner, String password) {
            this.id = id;
            this.name = name;
            this.owner = owner;
            this.password = password;
        }
    }

    public CloudChannel createChannel(String name, UUID owner, String password) {
        String id = "VTX-" + (1000 + new Random().nextInt(9000));
        while (channels.containsKey(id)) {
            id = "VTX-" + (1000 + new Random().nextInt(9000));
        }
        
        CloudChannel channel = new CloudChannel(id, name, owner, password);
        channel.members.add(owner);
        channels.put(id, channel);
        save(channel);
        return channel;
    }

    public CloudChannel getChannel(String id) {
        return channels.get(id);
    }

    public boolean canAccess(String id, UUID player, String inputPassword) {
        CloudChannel channel = channels.get(id);
        if (channel == null) return false;
        if (channel.blocked.contains(player)) return false;
        if (channel.owner.equals(player)) return true;
        if (channel.members.contains(player)) return true;
        
        if (channel.password != null && !channel.password.isEmpty()) {
            return channel.password.equals(inputPassword);
        }
        return true;
    }

    public void addMember(String id, UUID player) {
        CloudChannel channel = channels.get(id);
        if (channel != null) {
            channel.members.add(player);
            save(channel);
        }
    }

    public void blockPlayer(String id, UUID owner, UUID target) {
        CloudChannel channel = channels.get(id);
        if (channel != null && channel.owner.equals(owner)) {
            channel.members.remove(target);
            channel.blocked.add(target);
            save(channel);
        }
    }

    public void save(CloudChannel channel) {
        addon.getDatabaseManager().saveChannel(channel);
    }

    private void load() {
        for (CloudChannel c : addon.getDatabaseManager().loadChannels()) {
            channels.put(c.id, c);
        }
    }
}
