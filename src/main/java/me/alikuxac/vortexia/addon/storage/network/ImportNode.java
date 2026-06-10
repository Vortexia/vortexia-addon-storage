// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ImportNode extends AbstractStorageNode implements StorageGrid.AutomationNode {

    public ImportNode(Location loc) {
        super(loc);
    }

    @Override
    public NodeType getType() {
        return NodeType.IMPORT_NODE;
    }

    @Override
    public void tick(StorageGrid grid) {
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
                for (int i = 0; i < inv.getSize(); i++) {
                    ItemStack item = inv.getItem(i);
                    if (item != null && item.getAmount() > 0) {
                        ItemStack toAdd = item.clone();
                        // Import 8 items at a time for balance
                        int amount = Math.min(item.getAmount(), 8);
                        toAdd.setAmount(amount);
                        
                        if (grid.canAddItem(toAdd)) {
                            grid.addItem(toAdd);
                            item.setAmount(item.getAmount() - amount);
                            inv.setItem(i, item.getAmount() <= 0 ? null : item);
                            return; // One import per tick
                        }
                    }
                }
            }
        }
    }
}
