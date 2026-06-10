// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import me.alikuxac.vortexia.addon.storage.StorageAddon;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class StorageGrid implements me.alikuxac.vortexia.api.grid.Grid {

    private final me.alikuxac.vortexia.api.grid.Grid coreGrid;
    private final StorageAddon addon;

    public StorageGrid(StorageAddon addon, me.alikuxac.vortexia.api.grid.Grid coreGrid) {
        this.addon = addon;
        this.coreGrid = coreGrid;
    }

    @Override
    public UUID getId() {
        return coreGrid.getId();
    }

    @Override
    public String getNetworkType() {
        return "storage";
    }

    @Override
    public Collection<me.alikuxac.vortexia.api.grid.GridNode> getNodes() {
        return coreGrid.getNodes();
    }

    @Override
    public void addNode(me.alikuxac.vortexia.api.grid.GridNode node) {
        coreGrid.addNode(node);
    }

    @Override
    public void removeNode(me.alikuxac.vortexia.api.grid.GridNode node) {
        coreGrid.removeNode(node);
    }

    private List<StorageDriveNode> getDrives() {
        List<StorageDriveNode> drives = new ArrayList<>();
        for (me.alikuxac.vortexia.api.grid.GridNode node : coreGrid.getNodes()) {
            if (node instanceof StorageDriveNode drive) {
                drives.add(drive);
            }
        }
        return drives;
    }

    public List<ItemStack> getItems() {
        List<ItemStack> allItems = new ArrayList<>();

        // 1. Local storage drives
        for (StorageDriveNode drive : getDrives()) {
            for (ItemStack cell : drive.getInventory().getContents()) {
                if (cell == null || cell.getItemMeta() == null) continue;
                String data = cell.getItemMeta().getPersistentDataContainer().get(StorageItems.CELL_DATA_KEY, org.bukkit.persistence.PersistentDataType.STRING);
                if (data != null) {
                    allItems.addAll(CellSerializer.deserialize(data));
                }
            }
        }

        // 2. Cloud drives
        for (me.alikuxac.vortexia.api.grid.GridNode node : coreGrid.getNodes()) {
            if (node instanceof CloudDriveNode cloudNode) {
                allItems.addAll(addon.getCloudManager().getInventory(cloudNode.getCloudId()));
            }
        }

        return allItems;
    }

    public List<ItemStack> getAggregatedItems() {
        List<ItemStack> all = getItems();
        List<ItemStack> aggregated = new ArrayList<>();

        for (ItemStack item : all) {
            boolean found = false;
            for (ItemStack agg : aggregated) {
                if (agg.isSimilar(item)) {
                    agg.setAmount(agg.getAmount() + item.getAmount());
                    found = true;
                    break;
                }
            }
            if (!found) {
                aggregated.add(item.clone());
            }
        }
        return aggregated;
    }

    public int getTotalCapacity() {
        int total = 0;
        for (StorageDriveNode drive : getDrives()) {
            total += drive.getTotalCapacity();
        }
        return total;
    }

    public int getCurrentItemCount() {
        int count = 0;
        for (ItemStack item : getItems()) {
            // Count all items in network
            count += item.getAmount();
        }
        return count;
    }

    public boolean canAddItem(ItemStack item) {
        return getCurrentItemCount() + item.getAmount() <= getTotalCapacity();
    }

    public void addItem(ItemStack item) {
        int available = getTotalCapacity() - getCurrentItemCount();
        if (available <= 0) return;

        int toAddTotal = Math.min(item.getAmount(), available);
        if (toAddTotal <= 0) return;

        // 1. Try cloud stacking first
        for (me.alikuxac.vortexia.api.grid.GridNode node : coreGrid.getNodes()) {
            if (node instanceof CloudDriveNode cloudNode) {
                addon.getCloudManager().addItem(cloudNode.getCloudId(), item);
                if (item.getAmount() <= 0) return;
            }
        }

        // 2. Local stacking in existing slots of cells
        for (StorageDriveNode drive : getDrives()) {
            for (ItemStack cell : drive.getInventory().getContents()) {
                if (cell == null || cell.getItemMeta() == null) continue;
                Integer capacity = cell.getItemMeta().getPersistentDataContainer().get(StorageItems.CELL_CAPACITY_KEY, org.bukkit.persistence.PersistentDataType.INTEGER);
                if (capacity == null) continue;

                String data = cell.getItemMeta().getPersistentDataContainer().get(StorageItems.CELL_DATA_KEY, org.bukkit.persistence.PersistentDataType.STRING);
                List<ItemStack> cellItems = CellSerializer.deserialize(data);
                int cellCount = cellItems.stream().mapToInt(ItemStack::getAmount).sum();

                if (cellCount >= capacity) continue;

                // Try to stack in existing items
                for (ItemStack cellItem : cellItems) {
                    if (cellItem.isSimilar(item)) {
                        int space = Math.min(64 - cellItem.getAmount(), capacity - cellCount);
                        int added = Math.min(item.getAmount(), space);
                        if (added > 0) {
                            cellItem.setAmount(cellItem.getAmount() + added);
                            item.setAmount(item.getAmount() - added);
                            cellCount += added;
                            saveCell(cell, cellItems);
                            if (item.getAmount() <= 0) return;
                        }
                    }
                }

                // If still has items, add as new stack in this cell
                if (cellCount < capacity) {
                    int space = capacity - cellCount;
                    int added = Math.min(item.getAmount(), space);
                    if (added > 0) {
                        ItemStack cloned = item.clone();
                        cloned.setAmount(added);
                        cellItems.add(cloned);
                        item.setAmount(item.getAmount() - added);
                        saveCell(cell, cellItems);
                        if (item.getAmount() <= 0) return;
                    }
                }
            }
        }
    }

    public ItemStack removeItem(ItemStack template, int amount) {
        if (amount <= 0) return null;
        
        ItemStack result = null;
        int remaining = amount;
        
        // 1. Try local first
        for (StorageDriveNode drive : getDrives()) {
            for (ItemStack cell : drive.getInventory().getContents()) {
                if (cell == null || cell.getItemMeta() == null) continue;
                String data = cell.getItemMeta().getPersistentDataContainer().get(StorageItems.CELL_DATA_KEY, org.bukkit.persistence.PersistentDataType.STRING);
                if (data != null) {
                    List<ItemStack> cellItems = CellSerializer.deserialize(data);
                    boolean modified = false;
                    Iterator<ItemStack> it = cellItems.iterator();
                    while (it.hasNext() && remaining > 0) {
                        ItemStack cellItem = it.next();
                        if (cellItem.isSimilar(template)) {
                            int toRemove = Math.min(cellItem.getAmount(), remaining);
                            if (result == null) {
                                result = cellItem.clone();
                                result.setAmount(toRemove);
                            } else {
                                result.setAmount(result.getAmount() + toRemove);
                            }
                            
                            cellItem.setAmount(cellItem.getAmount() - toRemove);
                            remaining -= toRemove;
                            modified = true;
                            
                            if (cellItem.getAmount() <= 0) {
                                it.remove();
                            }
                        }
                    }
                    if (modified) {
                        saveCell(cell, cellItems);
                    }
                    if (remaining <= 0) {
                        return result;
                    }
                }
            }
        }

        // 2. Try cloud if still needed
        if (remaining > 0) {
            for (me.alikuxac.vortexia.api.grid.GridNode node : coreGrid.getNodes()) {
                if (node instanceof CloudDriveNode cloudNode) {
                    ItemStack cloudResult = addon.getCloudManager().removeItem(cloudNode.getCloudId(), template, remaining);
                    if (cloudResult != null) {
                        remaining -= cloudResult.getAmount();
                        if (result == null) {
                            result = cloudResult;
                        } else {
                            result.setAmount(result.getAmount() + cloudResult.getAmount());
                        }
                        if (remaining <= 0) {
                            return result;
                        }
                    }
                }
            }
        }

        return result;
    }

    private void saveCell(ItemStack cell, List<ItemStack> cellItems) {
        org.bukkit.inventory.meta.ItemMeta meta = cell.getItemMeta();
        if (meta == null) return;

        String data = CellSerializer.serialize(cellItems);
        meta.getPersistentDataContainer().set(StorageItems.CELL_DATA_KEY, org.bukkit.persistence.PersistentDataType.STRING, data);

        int count = cellItems.stream().mapToInt(ItemStack::getAmount).sum();
        Integer capacity = meta.getPersistentDataContainer().get(StorageItems.CELL_CAPACITY_KEY, org.bukkit.persistence.PersistentDataType.INTEGER);
        if (capacity == null) capacity = 0;

        java.util.List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(net.kyori.adventure.text.Component.text("Storage: ", net.kyori.adventure.text.format.NamedTextColor.GRAY)
            .append(net.kyori.adventure.text.Component.text(count + " / " + capacity, net.kyori.adventure.text.format.NamedTextColor.GREEN))
            .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        meta.lore(lore);

        cell.setItemMeta(meta);
    }

    @Override
    public void tick() {
        for (me.alikuxac.vortexia.api.grid.GridNode node : new ArrayList<>(coreGrid.getNodes())) {
            if (node instanceof AutomationNode automation) {
                automation.tick(this);
            }
        }
    }

    public interface AutomationNode extends StorageNode {
        void tick(StorageGrid grid);
    }
}
