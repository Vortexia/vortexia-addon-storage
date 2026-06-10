// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import org.bukkit.Location;

public abstract class AbstractStorageNode implements StorageNode {

    protected final Location location;
    protected StorageGrid grid;
    protected final java.util.List<org.bukkit.inventory.ItemStack> upgrades = new java.util.ArrayList<>();

    public AbstractStorageNode(Location location) {
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String getNetworkType() {
        return "storage";
    }

    @Override
    public void setGrid(me.alikuxac.vortexia.api.grid.Grid grid) {
        if (grid == null) {
            this.grid = null;
        } else if (grid instanceof StorageGrid sg) {
            this.grid = sg;
        } else {
            me.alikuxac.vortexia.addon.storage.StorageAddon addon = org.bukkit.plugin.java.JavaPlugin.getPlugin(me.alikuxac.vortexia.addon.storage.StorageAddon.class);
            this.grid = addon.getNetworkManager().getOrCreateGrid(grid);
        }
    }

    @Override
    public StorageGrid getGrid() {
        return grid;
    }

    public java.util.List<org.bukkit.inventory.ItemStack> getUpgrades() {
        return upgrades;
    }

    public boolean hasUpgrade(String id) {
        for (org.bukkit.inventory.ItemStack item : upgrades) {
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;
            String itemId = meta.getPersistentDataContainer().get(me.alikuxac.vortexia.api.VortexiaKeys.ITEM_ID, org.bukkit.persistence.PersistentDataType.STRING);
            if (id.equals(itemId)) return true;
        }
        return false;
    }
}
