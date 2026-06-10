// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import me.alikuxac.vortexia.addon.storage.api.StorageCell;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class StorageDrive extends AbstractStorageNode {

    private final List<StorageCell> cells = new ArrayList<>();
    public StorageDrive(Location location) {
        super(location);
    }

    @Override
    public NodeType getType() {
        return NodeType.DRIVE;
    }

    public int getMaxCells() {
        int base = 10;
        if (hasUpgrade("upgrade_tier_copper")) base += 5;
        if (hasUpgrade("upgrade_tier_iron")) base += 10;
        return base;
    }

    public boolean addCell(StorageCell cell) {
        if (cells.size() < getMaxCells()) {
            cells.add(cell);
            return true;
        }
        return false;
    }

    public List<StorageCell> getCells() {
        return cells;
    }
}
