// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ExportNode extends AbstractStorageNode implements StorageGrid.AutomationNode {

    private ItemStack filter;

    public ExportNode(Location loc) {
        super(loc);
    }

    public void setFilter(ItemStack filter) {
        this.filter = filter != null ? filter.clone() : null;
        if (this.filter != null) this.filter.setAmount(1);
    }

    public ItemStack getFilter() {
        return filter;
    }

    @Override
    public NodeType getType() {
        return NodeType.EXPORT_NODE;
    }

    @Override
    public void tick(StorageGrid grid) {
        if (filter == null) return;

        // Check all sides for inventories
        Location[] sides = {
            location.clone().add(1, 0, 0), location.clone().add(-1, 0, 0),
            location.clone().add(0, 1, 0), location.clone().add(0, -1, 0),
            location.clone().add(0, 0, 1), location.clone().add(0, 0, -1)
        };

        for (Location side : sides) {
            Block block = side.getBlock();
            if (block.getState() instanceof Container container) {
                Inventory inv = container.getInventory();
                
                // Try to take from grid
                ItemStack result = grid.removeItem(filter, 8); // Export 8 items at a time
                if (result != null) {
                    // Try to add to inventory
                    java.util.HashMap<Integer, ItemStack> leftover = inv.addItem(result);
                    if (!leftover.isEmpty()) {
                        // Put leftover back to grid
                        for (ItemStack stack : leftover.values()) {
                            grid.addItem(stack);
                        }
                    }
                    return; // One export per tick
                }
            }
        }
    }
}
