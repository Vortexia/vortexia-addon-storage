// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import me.alikuxac.vortexia.addon.storage.StorageAddon;
import me.alikuxac.vortexia.api.VortexiaKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class StorageItems {

    private static me.alikuxac.vortexia.addon.storage.config.LanguageManager lang(StorageAddon addon) {
        return addon.getLanguageManager();
    }

    public static final NamespacedKey CELL_CAPACITY_KEY = new NamespacedKey("vortexia", "cell_capacity");
    public static final NamespacedKey CELL_DATA_KEY = new NamespacedKey("vortexia", "cell_data");

    public static ItemStack getController(StorageAddon addon) {
        return createItem(addon, Material.PURPLE_GLAZED_TERRACOTTA, lang(addon).getRaw("items.nodes.controller"), "controller");
    }

    public static ItemStack getDrive(StorageAddon addon) {
        return createItem(addon, Material.SMOOTH_STONE_SLAB, lang(addon).getRaw("items.nodes.drive"), "drive");
    }

    public static ItemStack getCloudDrive(StorageAddon addon) {
        return createItem(addon, Material.ENDER_CHEST, lang(addon).getRaw("items.nodes.cloud_drive"), "cloud_drive");
    }

    public static ItemStack getTerminal(StorageAddon addon) {
        return createItem(addon, Material.GLASS_PANE, lang(addon).getRaw("items.nodes.terminal"), "terminal");
    }

    public static ItemStack getImportNode(StorageAddon addon) {
        return createItem(addon, Material.HOPPER, lang(addon).getRaw("items.nodes.import_node"), "import_node");
    }

    public static ItemStack getExportNode(StorageAddon addon) {
        return createItem(addon, Material.DROPPER, lang(addon).getRaw("items.nodes.export_node"), "export_node");
    }

    public static final NamespacedKey REMOTE_LINK = new NamespacedKey("vortexia", "remote_link");

    public static ItemStack getWirelessRemote(StorageAddon addon) {
        ItemStack item = createItem(addon, Material.RECOVERY_COMPASS, lang(addon).getRaw("items.wireless_remote.name"), "remote");
        ItemMeta meta = item.getItemMeta();
        java.util.List<Component> lore = new java.util.ArrayList<>();
        lore.add(lang(addon).getMessage("items.wireless_remote.lore_desc").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(lang(addon).getMessage("items.wireless_remote.lore_usage_header").decoration(TextDecoration.ITALIC, false));
        lore.add(lang(addon).getMessage("items.wireless_remote.lore_usage_bind").decoration(TextDecoration.ITALIC, false));
        lore.add(lang(addon).getMessage("items.wireless_remote.lore_usage_open").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text(lang(addon).getFormatted("items.wireless_remote.status_prefix") + lang(addon).getFormatted("items.wireless_remote.status_unlinked")).decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // --- TIERED STORAGE CELLS ---

    public static ItemStack getCellCopper(StorageAddon addon) {
        return createCell(addon, 1000, lang(addon).getRaw("items.cells.copper"), NamedTextColor.GOLD);
    }

    public static ItemStack getCellIron(StorageAddon addon) {
        return createCell(addon, 4000, lang(addon).getRaw("items.cells.iron"), NamedTextColor.GRAY);
    }

    public static ItemStack getCellGold(StorageAddon addon) {
        return createCell(addon, 16000, lang(addon).getRaw("items.cells.gold"), NamedTextColor.YELLOW);
    }

    public static ItemStack getCellDiamond(StorageAddon addon) {
        return createCell(addon, 64000, lang(addon).getRaw("items.cells.diamond"), NamedTextColor.AQUA);
    }

    public static ItemStack getCellNetherite(StorageAddon addon) {
        return createCell(addon, 256000, lang(addon).getRaw("items.cells.netherite"), NamedTextColor.DARK_PURPLE);
    }

    // --- UPGRADES ---

    public static ItemStack getCraftingUpgrade(StorageAddon addon) {
        return createItem(addon, Material.CRAFTING_TABLE, lang(addon).getRaw("items.upgrades.crafting"), "upgrade_crafting");
    }

    private static ItemStack createItem(StorageAddon addon, Material material, String name, String id) {
        return createItem(addon, material, name, id, NamedTextColor.LIGHT_PURPLE);
    }

    private static ItemStack createItem(StorageAddon addon, Material material, String name, String id, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
            
            // Standard Vortexia Tags
            meta.getPersistentDataContainer().set(VortexiaKeys.VORTEXIA_ITEM, PersistentDataType.INTEGER, 1);
            meta.getPersistentDataContainer().set(VortexiaKeys.ADDON_ID, PersistentDataType.STRING, "storage");
            meta.getPersistentDataContainer().set(VortexiaKeys.ITEM_ID, PersistentDataType.STRING, id);
            
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createCell(StorageAddon addon, int capacity, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            
            // Standard Vortexia Tags
            meta.getPersistentDataContainer().set(VortexiaKeys.VORTEXIA_ITEM, PersistentDataType.INTEGER, 1);
            meta.getPersistentDataContainer().set(VortexiaKeys.ADDON_ID, PersistentDataType.STRING, "storage");
            meta.getPersistentDataContainer().set(VortexiaKeys.ITEM_ID, PersistentDataType.STRING, "cell");
            
            // Set CustomModelData based on capacity for texture support
            int modelData = switch(capacity) {
                case 1000 -> 1001;
                case 4000 -> 1002;
                case 16000 -> 1003;
                case 64000 -> 1004;
                case 256000 -> 1005;
                default -> 1000;
            };
            meta.setCustomModelData(modelData);
            
            meta.getPersistentDataContainer().set(CELL_CAPACITY_KEY, PersistentDataType.INTEGER, capacity);
            item.setItemMeta(meta);
        }
        return item;
    }
}
