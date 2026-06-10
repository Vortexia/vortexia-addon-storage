// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import me.alikuxac.vortexia.addon.storage.StorageAddon;
import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StorageNetworkManager {

    private final StorageAddon addon;
    private final Map<Location, StorageNode> nodes = new ConcurrentHashMap<>();
    private final Map<UUID, StorageGrid> gridCache = new ConcurrentHashMap<>();

    public StorageNetworkManager(StorageAddon addon) {
        this.addon = addon;
    }

    public void addNode(Location loc, StorageNode node) {
        nodes.put(loc, node);
        me.alikuxac.vortexia.api.VortexiaProvider.get().getGridManager().registerNode(node);
    }

    public void removeNode(Location loc) {
        StorageNode node = nodes.remove(loc);
        if (node != null) {
            me.alikuxac.vortexia.api.VortexiaProvider.get().getGridManager().unregisterNode(loc, "storage");
        }
    }

    public StorageGrid getOrCreateGrid(me.alikuxac.vortexia.api.grid.Grid coreGrid) {
        if (coreGrid == null) return null;
        return gridCache.computeIfAbsent(coreGrid.getId(), id -> new StorageGrid(addon, coreGrid));
    }

    public StorageNode getNode(Location loc) {
        return nodes.get(loc);
    }

    public Map<UUID, StorageGrid> getGrids() {
        Map<UUID, StorageGrid> activeGrids = new HashMap<>();
        for (me.alikuxac.vortexia.api.grid.Grid coreGrid : me.alikuxac.vortexia.api.VortexiaProvider.get().getGridManager().getGrids("storage")) {
            activeGrids.put(coreGrid.getId(), getOrCreateGrid(coreGrid));
        }
        return activeGrids;
    }

    public void shutdown() {
        gridCache.clear();
        nodes.clear();
    }
}
