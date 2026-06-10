// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import org.bukkit.Location;

public interface StorageNode extends me.alikuxac.vortexia.api.grid.GridNode {
    
    Location getLocation();
    
    NodeType getType();
    
    enum NodeType {
        CONTROLLER,
        TERMINAL,
        DRIVE,
        CLOUD_DRIVE,
        CABLE,
        IMPORT_NODE,
        EXPORT_NODE
    }
}
