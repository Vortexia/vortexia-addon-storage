// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import org.bukkit.Location;

public class StorageController extends AbstractStorageNode {

    public StorageController(Location location) {
        super(location);
    }

    @Override
    public NodeType getType() {
        return NodeType.CONTROLLER;
    }
}
