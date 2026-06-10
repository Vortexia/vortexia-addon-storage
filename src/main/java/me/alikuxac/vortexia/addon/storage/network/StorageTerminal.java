// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import me.alikuxac.vortexia.addon.storage.StorageAddon;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class StorageTerminal extends AbstractStorageNode {

    public StorageTerminal(Location location) {
        super(location);
    }

    @Override
    public NodeType getType() {
        return NodeType.TERMINAL;
    }

    public void openInterface(Player player, StorageAddon addon) {
        if (grid == null) {
            player.sendMessage("§cTerminal này chưa được kết nối vào mạng lưới!");
            return;
        }
        addon.getTerminalGUI().open(player, this);
    }
}
