// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import org.bukkit.Location;

public class StorageNodeImpl extends AbstractStorageNode {

    private final NodeType type;

    public StorageNodeImpl(Location location, NodeType type) {
        super(location);
        this.type = type;
    }

    @Override
    public NodeType getType() {
        return type;
    }
}
