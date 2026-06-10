// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.gui;

import me.alikuxac.vortexia.addon.storage.StorageAddon;
import me.alikuxac.vortexia.addon.storage.network.StorageGrid;
import me.alikuxac.vortexia.api.VortexiaKeys;
import me.alikuxac.vortexia.addon.storage.network.StorageNode;
import me.alikuxac.vortexia.addon.storage.network.AbstractStorageNode;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class TerminalGUI implements Listener {

    private final StorageAddon addon;
    private final java.util.Map<java.util.UUID, TerminalHolder> searchWaiters = new java.util.HashMap<>();

    public TerminalGUI(StorageAddon addon) {
        this.addon = addon;
        Bukkit.getPluginManager().registerEvents(this, addon);
    }

    public static class TerminalHolder implements InventoryHolder {
        private final StorageGrid grid;
        private final StorageNode node;
        private String searchQuery = "";
        private SortMode sortMode = SortMode.AMOUNT;

        public TerminalHolder(StorageGrid grid, StorageNode node) { 
            this.grid = grid; 
            this.node = node;
        }
        public StorageGrid getGrid() { return grid; }
        public StorageNode getNode() { return node; }
        public String getSearchQuery() { return searchQuery; }
        public void setSearchQuery(String q) { this.searchQuery = q; }
        public SortMode getSortMode() { return sortMode; }
        public void setSortMode(SortMode m) { this.sortMode = m; }
        @Override public Inventory getInventory() { return null; }
    }

    public enum SortMode {
        NAME, AMOUNT, TYPE
    }

    public void open(Player player, StorageNode terminalNode) {
        StorageGrid grid = ((AbstractStorageNode) terminalNode).getGrid();
        Inventory inv = Bukkit.createInventory(new TerminalHolder(grid, terminalNode), 54, addon.getLanguageManager().getMessage("gui.terminal_title"));
        
        setupLayout(inv, terminalNode);
        refreshDisplay(inv, grid);

        player.openInventory(inv);
    }

    private void setupLayout(Inventory inv, StorageNode node) {
        AbstractStorageNode abstractNode = (AbstractStorageNode) node;
        
        // Borders and fillers
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        bm.displayName(Component.text(" "));
        border.setItemMeta(bm);

        // Wait, let's just fill everything then override
        for (int i = 0; i < 54; i++) inv.setItem(i, border);

        // Storage Slots (Cols 1-5)
        for (int r = 0; r < 6; r++) {
            for (int c = 1; c <= 5; c++) {
                inv.setItem(r * 9 + c, null);
            }
        }

        // Upgrade Slots (Col 0)
        int[] upgradeSlots = {0, 9, 18, 27};
        List<ItemStack> currentUpgrades = abstractNode.getUpgrades();
        for (int i = 0; i < 4; i++) {
            final int index = i + 1;
            if (i < currentUpgrades.size()) {
                inv.setItem(upgradeSlots[i], currentUpgrades.get(i));
            } else {
                ItemStack slot = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                ItemMeta sm = slot.getItemMeta();
                sm.displayName(addon.getLanguageManager().getMessage("gui.terminal.upgrade_slot")
                    .replaceText(config -> config.matchLiteral("%index%").replacement(Component.text(index))));
                slot.setItemMeta(sm);
                inv.setItem(upgradeSlots[i], slot);
            }
        }

        // Crafting Grid Area if upgrade present
        if (abstractNode.hasUpgrade("upgrade_crafting")) {
            int[] craftingSlots = {6, 7, 8, 15, 16, 17, 24, 25, 26};
            for (int s : craftingSlots) inv.setItem(s, null);
            
            ItemStack resultSlot = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta rm = resultSlot.getItemMeta();
            rm.displayName(addon.getLanguageManager().getMessage("gui.terminal.output"));
            resultSlot.setItemMeta(rm);
            inv.setItem(35, resultSlot);

            ItemStack craftBtn = new ItemStack(Material.CRAFTING_TABLE);
            ItemMeta cm = craftBtn.getItemMeta();
            cm.displayName(addon.getLanguageManager().getMessage("gui.terminal.craft_button"));
            craftBtn.setItemMeta(cm);
            inv.setItem(44, craftBtn);
        }

        // Search Button (Slot 41)
        ItemStack searchBtn = new ItemStack(Material.OAK_SIGN);
        ItemMeta searchMeta = searchBtn.getItemMeta();
        searchMeta.displayName(addon.getLanguageManager().getMessage("gui.terminal.search_button"));
        inv.setItem(41, searchBtn);

        // Sort Button (Slot 40)
        ItemStack sortBtn = new ItemStack(Material.HOPPER);
        ItemMeta sortMeta = sortBtn.getItemMeta();
        sortMeta.displayName(addon.getLanguageManager().getMessage("gui.terminal.sort_button"));
        inv.setItem(40, sortBtn);

        // Network Status Item (Top Right)
        ItemStack status = new ItemStack(Material.BEACON);
        ItemMeta smStatus = status.getItemMeta();
        smStatus.displayName(addon.getLanguageManager().getMessage("gui.terminal.status_title"));
        java.util.List<Component> lore = new java.util.ArrayList<>();
        lore.add(addon.getLanguageManager().getMessage("gui.terminal.status_items")
            .replaceText(config -> config.matchLiteral("%current%").replacement(Component.text(abstractNode.getGrid().getCurrentItemCount())))
            .replaceText(config -> config.matchLiteral("%max%").replacement(Component.text(abstractNode.getGrid().getTotalCapacity()))));
        lore.add(addon.getLanguageManager().getMessage("gui.terminal.status_nodes")
            .replaceText(config -> config.matchLiteral("%count%").replacement(Component.text(abstractNode.getGrid().getNodes().size()))));
        smStatus.lore(lore);
        status.setItemMeta(smStatus);
        inv.setItem(8, status);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TerminalHolder holder)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        Inventory clickedInv = event.getClickedInventory();

        if (clickedInv == null) return;

        StorageGrid grid = holder.getGrid();
        AbstractStorageNode node = (AbstractStorageNode) holder.getNode();

        // 1. Handle clicking in the Top Inventory (Terminal)
        if (clickedInv.equals(event.getView().getTopInventory())) {
            int slot = event.getSlot();

            // Upgrade Slots (Col 0)
            if (slot == 0 || slot == 9 || slot == 18 || slot == 27) {
                handleUpgradeClick(event, node, slot);
                return;
            }

            // Storage Area (Cols 1-5)
            if (isStorageSlot(slot)) {
                handleStorageWithdraw(player, grid, clicked);
            }

            // Crafting Grid (Cols 6-8)
            if (isCraftingSlot(slot)) {
                event.setCancelled(false); 
            }

            // Sort Button
            if (slot == 40) {
                SortMode next = SortMode.values()[(holder.getSortMode().ordinal() + 1) % SortMode.values().length];
                holder.setSortMode(next);
                refreshDisplay(event.getInventory(), grid);
                return;
            }

            // Search Button
            if (slot == 41) {
                player.closeInventory();
                searchWaiters.put(player.getUniqueId(), holder);
                player.sendMessage(addon.getLanguageManager().getMessage("messages.search_prompt"));
                return;
            }

            // Craft Button
            if (slot == 44) {
                player.sendMessage(addon.getLanguageManager().getMessage("messages.autocraft_soon"));
            }
        } 
        // 2. Handle clicking in Player Inventory
        else {
            if (clicked == null || clicked.getType() == Material.AIR) return;

            // Priority: If it's an upgrade and we shift-click, try to put in upgrade slot
            if (event.isShiftClick() && isUpgrade(clicked)) {
                if (tryPlaceUpgrade(node, clicked)) {
                    clicked.setAmount(0);
                    player.sendMessage(addon.getLanguageManager().getMessage("messages.upgrade_attached"));
                    setupLayout(event.getInventory(), node);
                    return;
                }
            }

            // Otherwise, deposit into grid
            int originalAmount = clicked.getAmount();
            grid.addItem(clicked);
            int stored = originalAmount - clicked.getAmount();
            if (stored > 0) {
                player.sendMessage(addon.getLanguageManager().getMessage("messages.item_stored"));
                if (clicked.getAmount() <= 0) {
                    clicked.setAmount(0);
                }
            } else {
                player.sendMessage(addon.getLanguageManager().getMessage("messages.storage_full"));
            }
            player.updateInventory();
        }

        refreshDisplay(event.getInventory(), grid);
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
    public void onPlayerChat(io.papermc.paper.event.player.AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!searchWaiters.containsKey(player.getUniqueId())) return;

        event.setCancelled(true);
        TerminalHolder holder = searchWaiters.remove(player.getUniqueId());
        String message = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.message());

        if (message.equalsIgnoreCase("clear")) {
            holder.setSearchQuery("");
        } else {
            holder.setSearchQuery(message);
        }

        // Re-open GUI on main thread
        Bukkit.getScheduler().runTask(addon, () -> {
            Inventory inv = Bukkit.createInventory(holder, 54, addon.getLanguageManager().getMessage("gui.terminal_title"));
            setupLayout(inv, holder.getNode());
            refreshDisplay(inv, holder.getGrid());
            player.openInventory(inv);
        });
    }

    private boolean isUpgrade(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return false;
        String id = item.getItemMeta().getPersistentDataContainer().get(VortexiaKeys.ITEM_ID, PersistentDataType.STRING);
        return id != null && id.startsWith("upgrade_");
    }

    private boolean tryPlaceUpgrade(AbstractStorageNode node, ItemStack upgrade) {
        if (node.getUpgrades().size() < 4) {
            node.getUpgrades().add(upgrade.clone());
            return true;
        }
        return false;
    }

    private void handleUpgradeClick(InventoryClickEvent event, AbstractStorageNode node, int slot) {
        ItemStack cursor = event.getCursor();
        
        // If holding an upgrade, place it
        if (isUpgrade(cursor)) {
            if (tryPlaceUpgrade(node, cursor)) {
                event.getView().setCursor(null);
                event.getWhoClicked().sendMessage(addon.getLanguageManager().getMessage("messages.upgrade_attached"));
                setupLayout(event.getInventory(), node);
            } else {
                event.getWhoClicked().sendMessage(addon.getLanguageManager().getMessage("messages.upgrade_full"));
            }
        } 
        // If clicking an existing upgrade slot without holding anything, remove it
        else {
            int index = getUpgradeIndex(slot);
            if (index < node.getUpgrades().size()) {
                ItemStack removed = node.getUpgrades().remove(index);
                event.getView().setCursor(removed);
                event.getWhoClicked().sendMessage(addon.getLanguageManager().getMessage("messages.upgrade_removed"));
                setupLayout(event.getInventory(), node);
            }
        }
    }

    private int getUpgradeIndex(int slot) {
        return switch (slot) {
            case 0 -> 0;
            case 9 -> 1;
            case 18 -> 2;
            case 27 -> 3;
            default -> -1;
        };
    }

    private void handleStorageWithdraw(Player player, StorageGrid grid, ItemStack clicked) {
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        
        // Use a clone for checking, strip GUI lore first
        ItemStack template = clicked.clone();
        ItemMeta meta = template.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<Component> lore = meta.lore();
            if (lore != null && lore.size() >= 3) {
                // Remove the last 3 lines added by refreshDisplay (empty line, count, click info)
                lore.remove(lore.size() - 1);
                lore.remove(lore.size() - 1);
                lore.remove(lore.size() - 1);
                meta.lore(lore);
                template.setItemMeta(meta);
            }
        }

        int toWithdraw = Math.min(clicked.getAmount(), template.getMaxStackSize());
        
        ItemStack result = grid.removeItem(template, toWithdraw);
        if (result != null) {
            if (!player.getInventory().addItem(result).isEmpty()) {
                grid.addItem(result);
                player.sendMessage(addon.getLanguageManager().getMessage("messages.inventory_full"));
            }
            player.updateInventory();
        }
    }

    private boolean isStorageSlot(int slot) {
        int col = slot % 9;
        return col >= 1 && col <= 5;
    }

    private boolean isCraftingSlot(int slot) {
        int col = slot % 9;
        return col >= 6 && col <= 8 && slot / 9 < 3;
    }

    private void refreshDisplay(Inventory inv, StorageGrid grid) {
        TerminalHolder holder = (TerminalHolder) inv.getHolder();
        String query = holder.getSearchQuery().toLowerCase();
        SortMode mode = holder.getSortMode();

        // 1. Clear and refill storage slots
        for (int i = 0; i < 54; i++) {
            if (isStorageSlot(i)) inv.setItem(i, null);
        }
        
        List<ItemStack> items = grid.getAggregatedItems();
        
        // Apply Filter
        if (!query.isEmpty()) {
            items.removeIf(item -> {
                String name = item.getType().name().toLowerCase();
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    name = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName()).toLowerCase();
                }
                return !name.contains(query);
            });
        }

        // Apply Sort
        items.sort((a, b) -> {
            switch (mode) {
                case NAME:
                    return a.getType().name().compareTo(b.getType().name());
                case AMOUNT:
                    return Integer.compare(b.getAmount(), a.getAmount());
                case TYPE:
                    return a.getType().compareTo(b.getType());
                default:
                    return 0;
            }
        });

        int slotIndex = 0;
        for (int i = 0; i < 54; i++) {
            if (isStorageSlot(i) && slotIndex < items.size()) {
                ItemStack item = items.get(slotIndex++).clone();
                int total = item.getAmount();
                
                // Add quantity lore
                ItemMeta meta = item.getItemMeta();
                List<Component> lore = meta.hasLore() ? meta.lore() : new java.util.ArrayList<>();
                lore.add(Component.text(""));
                lore.add(addon.getLanguageManager().getMessage("gui.terminal.item_count").replaceText(config -> config.matchLiteral("%count%").replacement(Component.text(String.format("%,d", total)))));
                lore.add(addon.getLanguageManager().getMessage("gui.terminal.item_withdraw_hint"));
                meta.lore(lore);
                item.setItemMeta(meta);
                
                item.setAmount(Math.min(total, 64));
                inv.setItem(i, item);
            }
        }

        // 2. Refresh Status and Buttons
        ItemStack status = inv.getItem(8);
        if (status != null && status.getType() == Material.BEACON) {
            ItemMeta sm = status.getItemMeta();
            java.util.List<Component> lore = new java.util.ArrayList<>();
            lore.add(addon.getLanguageManager().getMessage("gui.terminal.status_items")
                .replaceText(config -> config.matchLiteral("%current%").replacement(Component.text(grid.getCurrentItemCount())))
                .replaceText(config -> config.matchLiteral("%max%").replacement(Component.text(grid.getTotalCapacity()))));
            lore.add(addon.getLanguageManager().getMessage("gui.terminal.status_nodes")
                .replaceText(config -> config.matchLiteral("%count%").replacement(Component.text(grid.getNodes().size()))));
            lore.add(Component.text(""));
            lore.add(addon.getLanguageManager().getMessage("gui.terminal.status_search")
                .replaceText(config -> config.matchLiteral("%query%").replacement(Component.text(query.isEmpty() ? "None" : query))));
            lore.add(addon.getLanguageManager().getMessage("gui.terminal.status_sort")
                .replaceText(config -> config.matchLiteral("%mode%").replacement(Component.text(mode.name()))));
            sm.lore(lore);
            status.setItemMeta(sm);
            inv.setItem(8, status);
        }

        // Refresh Sort Button Lore
        ItemStack sortBtn = inv.getItem(40);
        if (sortBtn != null) {
            ItemMeta sm = sortBtn.getItemMeta();
            sm.lore(java.util.List.of(addon.getLanguageManager().getMessage("gui.terminal.sort_current")
                .replaceText(config -> config.matchLiteral("%mode%").replacement(Component.text(mode.name())))));
            sortBtn.setItemMeta(sm);
        }
    }
}
