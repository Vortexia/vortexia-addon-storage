// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class StorageDriveNode extends AbstractStorageNode {

    private final Inventory inventory;
    private final me.alikuxac.vortexia.addon.storage.StorageAddon addon;

    public StorageDriveNode(me.alikuxac.vortexia.addon.storage.StorageAddon addon, Location location) {
        super(location);
        this.addon = addon;
        this.inventory = Bukkit.createInventory(null, 18, addon.getLanguageManager().getMessage("gui.drive_title"));
    }

    @Override
    public NodeType getType() {
        return NodeType.DRIVE;
    }

    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Calculates total capacity provided by all cells in this drive.
     */
    public int getTotalCapacity() {
        int total = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getItemMeta() == null) continue;
            Integer capacity = item.getItemMeta().getPersistentDataContainer().get(StorageItems.CELL_CAPACITY_KEY, PersistentDataType.INTEGER);
            if (capacity != null) {
                total += capacity;
            }
        }
        return total;
    }
}
