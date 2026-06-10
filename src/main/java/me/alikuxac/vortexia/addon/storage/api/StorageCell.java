// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.api;

import org.bukkit.inventory.ItemStack;
import java.util.List;

public interface StorageCell {
    
    /**
     * Max capacity of items this cell can hold.
     */
    int getCapacity();
    
    /**
     * Current items stored in this cell.
     */
    List<ItemStack> getStoredItems();
    
    /**
     * Adds an item to the cell if possible.
     * @return remaining items that couldn't fit.
     */
    ItemStack insert(ItemStack item);
    
    /**
     * Extracts an item from the cell.
     */
    ItemStack extract(ItemStack matcher, int amount);
}
