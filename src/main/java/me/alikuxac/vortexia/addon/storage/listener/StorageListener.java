// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.listener;

import me.alikuxac.vortexia.addon.storage.StorageAddon;
import me.alikuxac.vortexia.addon.storage.network.StorageNode;
import me.alikuxac.vortexia.addon.storage.network.StorageNodeImpl;
import me.alikuxac.vortexia.addon.storage.network.StorageDriveNode;
import me.alikuxac.vortexia.api.VortexiaKeys;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class StorageListener implements Listener {

    private final StorageAddon addon;

    public StorageListener(StorageAddon addon) {
        this.addon = addon;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getItemMeta() == null) return;

        String id = item.getItemMeta().getPersistentDataContainer().get(VortexiaKeys.ITEM_ID, PersistentDataType.STRING);
        if (id == null) return;

        StorageNode.NodeType type = null;
        switch (id) {
            case "controller" -> type = StorageNode.NodeType.CONTROLLER;
            case "terminal" -> type = StorageNode.NodeType.TERMINAL;
            case "drive" -> type = StorageNode.NodeType.DRIVE;
            case "cloud_drive" -> type = StorageNode.NodeType.CLOUD_DRIVE;
            case "import_node" -> type = StorageNode.NodeType.IMPORT_NODE;
            case "export_node" -> type = StorageNode.NodeType.EXPORT_NODE;
        }

        if (type != null) {
            StorageNode node;
            if (type == StorageNode.NodeType.DRIVE) {
                node = new StorageDriveNode(addon, event.getBlock().getLocation());
            } else if (type == StorageNode.NodeType.CLOUD_DRIVE) {
                node = new me.alikuxac.vortexia.addon.storage.network.CloudDriveNode(event.getBlock().getLocation(), event.getPlayer().getUniqueId());
            } else if (type == StorageNode.NodeType.IMPORT_NODE) {
                node = new me.alikuxac.vortexia.addon.storage.network.ImportNode(event.getBlock().getLocation());
            } else if (type == StorageNode.NodeType.EXPORT_NODE) {
                node = new me.alikuxac.vortexia.addon.storage.network.ExportNode(event.getBlock().getLocation());
            } else {
                node = new StorageNodeImpl(event.getBlock().getLocation(), type);
            }
            addon.getNetworkManager().addNode(event.getBlock().getLocation(), node);
            event.getPlayer().sendMessage(addon.getLanguageManager().getMessage("messages.node_connected")
                .replaceText(config -> config.matchLiteral("%type%").replacement(Component.text(id))));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        addon.getNetworkManager().removeNode(event.getBlock().getLocation());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        StorageNode node = addon.getNetworkManager().getNode(block.getLocation());
        if (node == null) return;

        if (node.getType() == StorageNode.NodeType.TERMINAL) {
            event.setCancelled(true);
            addon.getTerminalGUI().open(event.getPlayer(), node);
        } else if (node.getType() == StorageNode.NodeType.DRIVE) {
            event.setCancelled(true);
            addon.getDriveGUI().open(event.getPlayer(), (StorageDriveNode) node);
        } else if (node.getType() == StorageNode.NodeType.CLOUD_DRIVE) {
            event.setCancelled(true);
            addon.getCloudConfigGUI().open(event.getPlayer(), (me.alikuxac.vortexia.addon.storage.network.CloudDriveNode) node);
        } else if (node.getType() == StorageNode.NodeType.EXPORT_NODE) {
            event.setCancelled(true);
            me.alikuxac.vortexia.addon.storage.network.ExportNode exportNode = (me.alikuxac.vortexia.addon.storage.network.ExportNode) node;
            ItemStack hand = event.getItem();
            if (hand != null && hand.getType() != Material.AIR) {
                exportNode.setFilter(hand);
                event.getPlayer().sendMessage(addon.getLanguageManager().getMessage("messages.filter_set")
                    .replaceText(config -> config.matchLiteral("%item%").replacement(Component.text(hand.getType().name()))));
            } else {
                exportNode.setFilter(null);
                event.getPlayer().sendMessage(addon.getLanguageManager().getMessage("messages.filter_cleared"));
            }
        }
    }

    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof me.alikuxac.vortexia.addon.storage.gui.CloudConfigGUI.ConfigHolder holder)) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        
        me.alikuxac.vortexia.addon.storage.network.CloudDriveNode node = holder.getNode();
        if (node == null) return;

        if (slot == 11) { // Personal
            node.setCloudId("player:" + player.getUniqueId());
            player.sendMessage(addon.getLanguageManager().getMessage("messages.cloud_switched_personal"));
            addon.getCloudConfigGUI().open(player, node);
        } else if (slot == 15) { // Shared
            // This just shows the current ID, clicking might prompt for join
            player.sendMessage(addon.getLanguageManager().getMessage("messages.cloud_use_manage"));
        } else if (slot == 22) { // Manage
            node.getCloudId(); // Just for safety
            addon.getCloudMemberGUI().open(player, node);
        }
    }

    @EventHandler
    public void onMemberInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof me.alikuxac.vortexia.addon.storage.gui.CloudMemberGUI.MemberHolder holder)) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        me.alikuxac.vortexia.addon.storage.network.CloudDriveNode node = holder.getNode();
        String channelId = node.getCloudId().substring(8);
        me.alikuxac.vortexia.addon.storage.network.CloudChannelManager.CloudChannel channel = addon.getChannelManager().getChannel(channelId);

        if (item.getType() == Material.PLAYER_HEAD) {
            org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) item.getItemMeta();
            if (meta.getOwningPlayer() == null) return;
            UUID targetId = meta.getOwningPlayer().getUniqueId();

            if (event.isLeftClick()) { // KICK
                channel.members.remove(targetId);
                addon.getChannelManager().save(channel);
                player.sendMessage(addon.getLanguageManager().getMessage("messages.player_kicked")
                    .replaceText(config -> config.matchLiteral("%player%").replacement(Component.text(meta.getOwningPlayer().getName() != null ? meta.getOwningPlayer().getName() : "Unknown"))));
                addon.getCloudMemberGUI().open(player, node);
            } else if (event.isRightClick()) { // BLOCK
                addon.getChannelManager().blockPlayer(channelId, player.getUniqueId(), targetId);
                player.sendMessage(addon.getLanguageManager().getMessage("messages.player_blocked")
                    .replaceText(config -> config.matchLiteral("%player%").replacement(Component.text(meta.getOwningPlayer().getName() != null ? meta.getOwningPlayer().getName() : "Unknown"))));
                addon.getCloudMemberGUI().open(player, node);
            }
        } else if (item.getType() == Material.ARROW) {
            addon.getCloudConfigGUI().open(player, node);
        }
    }
}
