// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.gui;

import me.alikuxac.vortexia.addon.storage.StorageAddon;
import me.alikuxac.vortexia.addon.storage.network.StorageDriveNode;
import me.alikuxac.vortexia.api.VortexiaKeys;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class DriveGUI implements Listener {

    private final StorageAddon addon;

    public DriveGUI(StorageAddon addon) {
        this.addon = addon;
        Bukkit.getPluginManager().registerEvents(this, addon);
    }

    public static class DriveHolder implements InventoryHolder {
        private final StorageDriveNode node;
        public DriveHolder(StorageDriveNode node) {
            this.node = node;
        }
        public StorageDriveNode getNode() { return node; }
        @Override public Inventory getInventory() { return node.getInventory(); }
    }

    public void open(Player player, StorageDriveNode node) {
        player.openInventory(new DriveHolder(node).getInventory());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof DriveHolder)) return;

        ItemStack clicked = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        // 1. Logic for Drive slots (Top Inventory)
        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            // Block non-cell items
            if (cursor.getType() != Material.AIR && !isStorageCell(cursor)) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(addon.getLanguageManager().getMessage("messages.drive_invalid_item"));
                return;
            }

            // Handle putting item from cursor
            if (cursor.getType() != Material.AIR && isStorageCell(cursor)) {
                // If slot is empty, take ONLY 1 from stack
                if (clicked == null || clicked.getType() == Material.AIR) {
                    ItemStack toPlace = cursor.clone();
                    toPlace.setAmount(1);
                    event.setCurrentItem(toPlace);
                    
                    cursor.setAmount(cursor.getAmount() - 1);
                    event.setCancelled(true);
                    return;
                } else {
                    // Slot already occupied, block everything
                    event.setCancelled(true);
                    return;
                }
            }
        } 
        
        // 2. Handle Shift-Click from player inventory
        else if (event.isShiftClick() && clicked != null && isStorageCell(clicked)) {
            event.setCancelled(true);
            Inventory top = event.getView().getTopInventory();
            
            // Try to find a free slot for ONLY ONE cell
            for (int i = 0; i < top.getSize(); i++) {
                ItemStack itemInSlot = top.getItem(i);
                if (itemInSlot == null || itemInSlot.getType() == Material.AIR) {
                    ItemStack toPlace = clicked.clone();
                    toPlace.setAmount(1);
                    top.setItem(i, toPlace);
                    
                    clicked.setAmount(clicked.getAmount() - 1);
                    break;
                }
            }
        }
    }

    private boolean isStorageCell(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return false;
        String id = item.getItemMeta().getPersistentDataContainer().get(VortexiaKeys.ITEM_ID, PersistentDataType.STRING);
        return "cell".equals(id);
    }
}
