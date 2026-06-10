// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.alikuxac.vortexia.addon.storage.StorageAddon;
import me.alikuxac.vortexia.api.VortexiaKeys;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

public class StoragePersistenceManager {

    private final StorageAddon addon;
    private final Gson gson = new GsonBuilder().create();

    public StoragePersistenceManager(StorageAddon addon) {
        this.addon = addon;
    }

    public void save(Map<UUID, StorageGrid> grids) {
        for (StorageGrid grid : grids.values()) {
            GridData data = new GridData(grid);
            String json = gson.toJson(data);
            addon.getDatabaseManager().saveGrid(grid.getId().toString(), json);
        }
    }

    public List<GridData> load() {
        List<GridData> list = new ArrayList<>();
        List<String[]> data = addon.getDatabaseManager().loadGrids();
        for (String[] entry : data) {
            GridData gd = gson.fromJson(entry[1], GridData.class);
            if (gd != null) list.add(gd);
        }
        return list;
    }

    public static class GridData {
        public UUID id;
        public List<NodeData> nodes;

        public GridData(StorageGrid grid) {
            this.id = grid.getId();
            this.nodes = new ArrayList<>();
            for (me.alikuxac.vortexia.api.grid.GridNode node : grid.getNodes()) {
                if (node instanceof StorageNode storageNode) {
                    nodes.add(new NodeData(storageNode));
                }
            }
        }
    }

    public static class NodeData {
        public String type;
        public String world;
        public double x, y, z;
        public List<String> upgrades;
        public String cloudId;

        public NodeData() {}

        public NodeData(StorageNode node) {
            this.type = node.getType().name();
            this.world = node.getLocation().getWorld().getName();
            this.x = node.getLocation().getX();
            this.y = node.getLocation().getY();
            this.z = node.getLocation().getZ();
            this.upgrades = new ArrayList<>();
            for (org.bukkit.inventory.ItemStack item : ((AbstractStorageNode) node).getUpgrades()) {
                String id = item.getItemMeta().getPersistentDataContainer().get(VortexiaKeys.ITEM_ID, org.bukkit.persistence.PersistentDataType.STRING);
                if (id != null) upgrades.add(id);
            }
            if (node instanceof CloudDriveNode cloudNode) {
                this.cloudId = cloudNode.getCloudId();
            }
        }

        public Location toLocation() {
            World w = Bukkit.getWorld(world);
            if (w == null) return null;
            return new Location(w, x, y, z);
        }
    }
}
