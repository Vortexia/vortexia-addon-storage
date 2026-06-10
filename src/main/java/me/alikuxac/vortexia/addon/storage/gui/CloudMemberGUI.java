// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.gui;

import me.alikuxac.vortexia.addon.storage.StorageAddon;
import me.alikuxac.vortexia.addon.storage.network.CloudChannelManager;
import me.alikuxac.vortexia.addon.storage.network.CloudDriveNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CloudMemberGUI {

    private final StorageAddon addon;

    public CloudMemberGUI(StorageAddon addon) {
        this.addon = addon;
    }

    public static class MemberHolder implements InventoryHolder {
        private final CloudDriveNode node;
        public MemberHolder(CloudDriveNode node) { this.node = node; }
        @Override public Inventory getInventory() { return null; }
        public CloudDriveNode getNode() { return node; }
    }

    public void open(Player player, CloudDriveNode node) {
        String cloudId = node.getCloudId();
        if (!cloudId.startsWith("channel:")) {
            player.sendMessage(addon.getLanguageManager().getMessage("messages.cloud_not_connected"));
            return;
        }

        String channelId = cloudId.substring(8);
        CloudChannelManager.CloudChannel channel = addon.getChannelManager().getChannel(channelId);
        
        if (channel == null || !channel.owner.equals(player.getUniqueId())) {
            player.sendMessage(addon.getLanguageManager().getMessage("messages.cloud_owner_only"));
            return;
        }

        Inventory inv = Bukkit.createInventory(new MemberHolder(node), 54, 
            addon.getLanguageManager().getMessage("gui.cloud_member_title")
                .replaceText(config -> config.matchLiteral("%name%").replacement(Component.text(channel.name))));

        int slot = 0;
        for (UUID memberId : channel.members) {
            if (memberId.equals(player.getUniqueId())) continue; // Skip owner
            if (slot >= 45) break;

            OfflinePlayer op = Bukkit.getOfflinePlayer(memberId);
            inv.setItem(slot++, createMemberHead(op));
        }

        // Back button
        inv.setItem(49, createItem(Material.ARROW, 
            addon.getLanguageManager().getMessage("gui.back_button"), 
            List.of(addon.getLanguageManager().getMessage("gui.back_button_lore"))));

        player.openInventory(inv);
    }

    private ItemStack createMemberHead(OfflinePlayer player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        meta.displayName(Component.text(player.getName() != null ? player.getName() : "Unknown", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            addon.getLanguageManager().getMessage("gui.members.click_kick").decoration(TextDecoration.ITALIC, false),
            addon.getLanguageManager().getMessage("gui.members.click_ban").decoration(TextDecoration.ITALIC, false),
            Component.text(" "),
            Component.text("UUID: " + player.getUniqueId(), NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
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
