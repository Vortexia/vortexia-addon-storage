// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.listener;

import me.alikuxac.vortexia.addon.storage.StorageAddon;
import me.alikuxac.vortexia.addon.storage.gui.TerminalGUI;
import me.alikuxac.vortexia.addon.storage.network.StorageItems;
import me.alikuxac.vortexia.addon.storage.network.StorageNode;
import me.alikuxac.vortexia.addon.storage.network.StorageNetworkManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class RemoteListener implements Listener {

    private final StorageAddon addon;
    private final TerminalGUI terminalGUI;

    public RemoteListener(StorageAddon addon, TerminalGUI terminalGUI) {
        this.addon = addon;
        this.terminalGUI = terminalGUI;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getItemMeta() == null) return;
        String itemId = item.getItemMeta().getPersistentDataContainer().get(me.alikuxac.vortexia.api.VortexiaKeys.ITEM_ID, PersistentDataType.STRING);
        if (!"remote".equals(itemId)) return;

        Block block = event.getClickedBlock();
        
        // Binding Logic
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block != null) {
            StorageNode node = addon.getNetworkManager().getNode(block.getLocation());
            if (node != null && node.getType().name().contains("TERMINAL")) {
                bindRemote(player, item, block.getLocation());
                event.setCancelled(true);
                return;
            }
        }

        // Access Logic
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemMeta meta = item.getItemMeta();
            if (meta.getPersistentDataContainer().has(StorageItems.REMOTE_LINK, PersistentDataType.STRING)) {
                String locStr = meta.getPersistentDataContainer().get(StorageItems.REMOTE_LINK, PersistentDataType.STRING);
                Location loc = deserializeLocation(locStr);
                
                if (loc == null) {
                    player.sendMessage(addon.getLanguageManager().getMessage("messages.remote_invalid_loc"));
                    return;
                }

                StorageNode node = addon.getNetworkManager().getNode(loc);
                if (node == null) {
                    player.sendMessage(addon.getLanguageManager().getMessage("messages.remote_no_network"));
                    return;
                }

                terminalGUI.open(player, node);
                event.setCancelled(true);
            } else {
                player.sendMessage(addon.getLanguageManager().getMessage("messages.remote_not_bound"));
            }
        }
    }

    private void bindRemote(Player player, ItemStack item, Location loc) {
        ItemMeta meta = item.getItemMeta();
        String locStr = serializeLocation(loc);
        meta.getPersistentDataContainer().set(StorageItems.REMOTE_LINK, PersistentDataType.STRING, locStr);
        
        List<Component> lore = meta.lore();
        if (lore != null) {
            // Update status line (index 6 based on StorageItems.createRemote)
            if (lore.size() > 6) {
                lore.set(6, addon.getLanguageManager().getMessage("messages.remote_status_linked").decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
            }
            lore.add(addon.getLanguageManager().getMessage("items.wireless_remote.lore_world")
                .append(Component.text(loc.getWorld().getName())).decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
            lore.add(addon.getLanguageManager().getMessage("items.wireless_remote.lore_pos")
                .append(Component.text(loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ())).decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        }
        
        meta.lore(lore);
        item.setItemMeta(meta);
        player.sendMessage(addon.getLanguageManager().getMessage("messages.remote_bound_success")
            .replaceText(config -> config.matchLiteral("%x%").replacement(Component.text(loc.getBlockX())))
            .replaceText(config -> config.matchLiteral("%y%").replacement(Component.text(loc.getBlockY())))
            .replaceText(config -> config.matchLiteral("%z%").replacement(Component.text(loc.getBlockZ()))));
    }

    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }

    private Location deserializeLocation(String s) {
        try {
            String[] parts = s.split(",");
            return new Location(Bukkit.getWorld(parts[0]), 
                Double.parseDouble(parts[1]), 
                Double.parseDouble(parts[2]), 
                Double.parseDouble(parts[3]));
        } catch (Exception e) {
            return null;
        }
    }
}
