// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.gui;

import me.alikuxac.vortexia.addon.storage.StorageAddon;
import me.alikuxac.vortexia.addon.storage.network.CloudDriveNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CloudConfigGUI {

    private final StorageAddon addon;

    public CloudConfigGUI(StorageAddon addon) {
        this.addon = addon;
    }

    public static class ConfigHolder implements org.bukkit.inventory.InventoryHolder {
        private final CloudDriveNode node;
        public ConfigHolder(CloudDriveNode node) { this.node = node; }
        @Override public Inventory getInventory() { return null; }
        public CloudDriveNode getNode() { return node; }
    }

    public void open(Player player, CloudDriveNode node) {
        Inventory inv = Bukkit.createInventory(new ConfigHolder(node), 27, addon.getLanguageManager().getMessage("gui.cloud_config_title"));

        // Background
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(Component.text(" "));
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        // Personal Mode Button
        Component personalStatus = node.getCloudId().startsWith("player:") ? 
            addon.getLanguageManager().getMessage("gui.cloud.status_active") : 
            addon.getLanguageManager().getMessage("gui.cloud.status_off");
        
        List<Component> personalLore = new ArrayList<>();
        for (String line : addon.getLanguageManager().getStringList("gui.cloud.personal_lore")) {
            personalLore.add(addon.getLanguageManager().parse(line).replaceText(config -> config.matchLiteral("%status%").replacement(personalStatus)));
        }

        inv.setItem(11, createItem(Material.PLAYER_HEAD, 
            addon.getLanguageManager().getMessage("gui.cloud.personal_title"), 
            personalLore));

        // Shared Channel Button
        Component sharedStatus = node.getCloudId().startsWith("channel:") ? 
            addon.getLanguageManager().getMessage("gui.cloud.status_active") : 
            addon.getLanguageManager().getMessage("gui.cloud.status_off");
        String channelId = node.getCloudId().startsWith("channel:") ? node.getCloudId().substring(8) : "None";

        List<Component> sharedLore = new ArrayList<>();
        for (String line : addon.getLanguageManager().getStringList("gui.cloud.shared_lore")) {
            sharedLore.add(addon.getLanguageManager().parse(line)
                .replaceText(config -> config.matchLiteral("%status%").replacement(sharedStatus))
                .replaceText(config -> config.matchLiteral("%id%").replacement(Component.text(channelId))));
        }

        inv.setItem(15, createItem(Material.ENDER_EYE, 
            addon.getLanguageManager().getMessage("gui.cloud.shared_title"), 
            sharedLore));

        // Create/Join Buttons
        List<Component> manageLore = new ArrayList<>();
        for (String line : addon.getLanguageManager().getStringList("gui.cloud.manage_lore")) {
            manageLore.add(addon.getLanguageManager().parse(line));
        }
        inv.setItem(22, createItem(Material.WRITABLE_BOOK, 
            addon.getLanguageManager().getMessage("gui.cloud.manage_title"), 
            manageLore));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        List<Component> loreComp = new ArrayList<>();
        for (Component c : lore) loreComp.add(c.decoration(TextDecoration.ITALIC, false));
        meta.lore(loreComp);
        item.setItemMeta(meta);
        return item;
    }
}
