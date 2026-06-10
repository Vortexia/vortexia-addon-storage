// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage;

import me.alikuxac.vortexia.api.VortexiaProvider;
import me.alikuxac.vortexia.api.addon.VortexiaAddon;
import me.alikuxac.vortexia.addon.storage.network.StorageNetworkManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.List;

public class StorageAddon extends JavaPlugin implements VortexiaAddon {

    private StorageNetworkManager networkManager;
    private me.alikuxac.vortexia.addon.storage.gui.TerminalGUI terminalGUI;
    private me.alikuxac.vortexia.addon.storage.gui.DriveGUI driveGUI;
    private me.alikuxac.vortexia.addon.storage.gui.CloudConfigGUI cloudConfigGUI;
    private me.alikuxac.vortexia.addon.storage.gui.CloudMemberGUI cloudMemberGUI;
    private me.alikuxac.vortexia.addon.storage.network.StoragePersistenceManager persistenceManager;
    private me.alikuxac.vortexia.addon.storage.recipe.StorageRecipeManager recipeManager;
    private me.alikuxac.vortexia.addon.storage.network.CloudStorageManager cloudManager;
    private me.alikuxac.vortexia.addon.storage.network.CloudChannelManager channelManager;
    private me.alikuxac.vortexia.addon.storage.network.DatabaseManager databaseManager;
    private me.alikuxac.vortexia.addon.storage.config.LanguageManager languageManager;
    private me.alikuxac.vortexia.addon.storage.util.UpdateChecker updateChecker;

    private static final String MIN_CORE_VERSION = "0.2.0";

    @Override
    public void onEnable() {
        if (!checkCoreVersion()) {
            getLogger().severe("Core plugin version is too old! Required: " + MIN_CORE_VERSION);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // Register this addon to Vortexia Core
        VortexiaProvider.get().getAddonManager().registerAddon(this);
    }

    private boolean checkCoreVersion() {
        org.bukkit.plugin.Plugin core = getServer().getPluginManager().getPlugin("VortexiaCore");
        if (core == null) return false;
        
        String coreVersion = core.getPluginMeta().getVersion();
        return isVersionSufficient(coreVersion, MIN_CORE_VERSION);
    }

    private boolean isVersionSufficient(String current, String required) {
        String[] currentParts = current.split("[-.]");
        String[] requiredParts = required.split("[-.]");
        
        for (int i = 0; i < Math.min(currentParts.length, requiredParts.length); i++) {
            try {
                int c = Integer.parseInt(currentParts[i]);
                int r = Integer.parseInt(requiredParts[i]);
                if (c > r) return true;
                if (c < r) return false;
            } catch (NumberFormatException e) {
                // Ignore non-numeric parts for now
            }
        }
        return currentParts.length >= requiredParts.length;
    }

    @Override
    public void onAddonEnable() {
        getLogger().info("Storage Addon: Initializing Digital Storage Grid...");
        
        // Initialize managers and GUIs
        this.databaseManager = new me.alikuxac.vortexia.addon.storage.network.DatabaseManager(this);
        this.languageManager = new me.alikuxac.vortexia.addon.storage.config.LanguageManager(this);
        this.persistenceManager = new me.alikuxac.vortexia.addon.storage.network.StoragePersistenceManager(this);
        this.networkManager = new StorageNetworkManager(this);
        this.terminalGUI = new me.alikuxac.vortexia.addon.storage.gui.TerminalGUI(this);
        this.driveGUI = new me.alikuxac.vortexia.addon.storage.gui.DriveGUI(this);
        this.cloudConfigGUI = new me.alikuxac.vortexia.addon.storage.gui.CloudConfigGUI(this);
        this.cloudMemberGUI = new me.alikuxac.vortexia.addon.storage.gui.CloudMemberGUI(this);
        this.recipeManager = new me.alikuxac.vortexia.addon.storage.recipe.StorageRecipeManager(this);
        this.cloudManager = new me.alikuxac.vortexia.addon.storage.network.CloudStorageManager(this);
        this.channelManager = new me.alikuxac.vortexia.addon.storage.network.CloudChannelManager(this);

        // Register recipes
        this.recipeManager.registerRecipes();

        // Register Storage Solver to central GridManager
        VortexiaProvider.get().getGridManager().registerSolver("storage", new me.alikuxac.vortexia.addon.storage.network.StorageGridSolver(this));

        // Register addon items to Vortexia central registry
        registerAddonItems();

        // Register commands
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register("vstorage", "Main command for Vortexia Storage", List.of("storage", "vs"), new me.alikuxac.vortexia.addon.storage.command.StorageCommand(this));
        });

        // Register listeners
        getServer().getPluginManager().registerEvents(new me.alikuxac.vortexia.addon.storage.listener.StorageListener(this), this);
        getServer().getPluginManager().registerEvents(new me.alikuxac.vortexia.addon.storage.listener.RemoteListener(this, this.terminalGUI), this);
        getServer().getPluginManager().registerEvents(this.terminalGUI, this);

        this.updateChecker = new me.alikuxac.vortexia.addon.storage.util.UpdateChecker(this);
        this.updateChecker.checkAsync();
        getServer().getPluginManager().registerEvents(new me.alikuxac.vortexia.addon.storage.listener.UpdateNotifyListener(this), this);
    }

    @Override
    public void onAddonDisable() {
        getLogger().info("Storage Addon: Saving grid state and shutting down...");
        if (networkManager != null && persistenceManager != null) {
            persistenceManager.save(networkManager.getGrids());
            networkManager.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    @Override
    public String getAddonName() {
        return "Storage";
    }

    @Override
    public String getVersion() {
        return "0.1.0";
    }

    @Override
    public String getAuthor() {
        return "alikuxac";
    }

    public StorageNetworkManager getNetworkManager() {
        return networkManager;
    }

    public me.alikuxac.vortexia.addon.storage.gui.TerminalGUI getTerminalGUI() {
        return terminalGUI;
    }

    public me.alikuxac.vortexia.addon.storage.gui.DriveGUI getDriveGUI() {
        return driveGUI;
    }

    public me.alikuxac.vortexia.addon.storage.gui.CloudConfigGUI getCloudConfigGUI() {
        return cloudConfigGUI;
    }

    public me.alikuxac.vortexia.addon.storage.gui.CloudMemberGUI getCloudMemberGUI() {
        return cloudMemberGUI;
    }

    public me.alikuxac.vortexia.addon.storage.network.CloudStorageManager getCloudManager() {
        return cloudManager;
    }

    public me.alikuxac.vortexia.addon.storage.network.CloudChannelManager getChannelManager() {
        return channelManager;
    }

    public me.alikuxac.vortexia.addon.storage.network.DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public me.alikuxac.vortexia.addon.storage.config.LanguageManager getLanguageManager() {
        return languageManager;
    }

    public me.alikuxac.vortexia.addon.storage.util.UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    private void registerAddonItems() {
        me.alikuxac.vortexia.api.item.ItemRegistry registry = VortexiaProvider.get().getItemRegistry();
        if (registry == null) return;

        // 1. Controller
        ItemStack controllerStack = me.alikuxac.vortexia.addon.storage.network.StorageItems.getController(this);
        ItemStack[] controllerRecipe = new ItemStack[] {
            new ItemStack(Material.IRON_INGOT), new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.IRON_INGOT),
            new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.REDSTONE_BLOCK), new ItemStack(Material.GOLD_INGOT),
            new ItemStack(Material.IRON_INGOT), new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.IRON_INGOT)
        };
        registry.registerItem(new me.alikuxac.vortexia.api.item.VortexiaItem("Storage", "controller", controllerStack, controllerRecipe));

        // 2. Drive
        ItemStack driveStack = me.alikuxac.vortexia.addon.storage.network.StorageItems.getDrive(this);
        ItemStack[] driveRecipe = new ItemStack[] {
            new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT),
            new ItemStack(Material.REDSTONE), new ItemStack(Material.CHEST), new ItemStack(Material.REDSTONE),
            new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT)
        };
        registry.registerItem(new me.alikuxac.vortexia.api.item.VortexiaItem("Storage", "drive", driveStack, driveRecipe));

        // 3. Terminal
        ItemStack terminalStack = me.alikuxac.vortexia.addon.storage.network.StorageItems.getTerminal(this);
        ItemStack[] terminalRecipe = new ItemStack[] {
            new ItemStack(Material.GLASS), new ItemStack(Material.GLASS), new ItemStack(Material.GLASS),
            new ItemStack(Material.REDSTONE), new ItemStack(Material.GLASS_PANE), new ItemStack(Material.REDSTONE),
            new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT)
        };
        registry.registerItem(new me.alikuxac.vortexia.api.item.VortexiaItem("Storage", "terminal", terminalStack, terminalRecipe));

        // 4. Cell Copper
        ItemStack cellCopperStack = me.alikuxac.vortexia.addon.storage.network.StorageItems.getCellCopper(this);
        ItemStack[] cellCopperRecipe = new ItemStack[] {
            new ItemStack(Material.COPPER_INGOT), new ItemStack(Material.COPPER_INGOT), new ItemStack(Material.COPPER_INGOT),
            new ItemStack(Material.COPPER_INGOT), new ItemStack(Material.GLASS), new ItemStack(Material.COPPER_INGOT),
            new ItemStack(Material.COPPER_INGOT), new ItemStack(Material.COPPER_INGOT), new ItemStack(Material.COPPER_INGOT)
        };
        registry.registerItem(new me.alikuxac.vortexia.api.item.VortexiaItem("Storage", "cell_copper", cellCopperStack, cellCopperRecipe));

        // 5. Cell Iron
        ItemStack cellIronStack = me.alikuxac.vortexia.addon.storage.network.StorageItems.getCellIron(this);
        ItemStack[] cellIronRecipe = new ItemStack[] {
            new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT),
            new ItemStack(Material.IRON_INGOT), cellCopperStack, new ItemStack(Material.IRON_INGOT),
            new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT), new ItemStack(Material.IRON_INGOT)
        };
        registry.registerItem(new me.alikuxac.vortexia.api.item.VortexiaItem("Storage", "cell_iron", cellIronStack, cellIronRecipe));

        // 6. Cell Gold
        ItemStack cellGoldStack = me.alikuxac.vortexia.addon.storage.network.StorageItems.getCellGold(this);
        ItemStack[] cellGoldRecipe = new ItemStack[] {
            new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.GOLD_INGOT),
            new ItemStack(Material.GOLD_INGOT), cellIronStack, new ItemStack(Material.GOLD_INGOT),
            new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.GOLD_INGOT)
        };
        registry.registerItem(new me.alikuxac.vortexia.api.item.VortexiaItem("Storage", "cell_gold", cellGoldStack, cellGoldRecipe));

        // 7. Cell Diamond
        ItemStack cellDiamondStack = me.alikuxac.vortexia.addon.storage.network.StorageItems.getCellDiamond(this);
        ItemStack[] cellDiamondRecipe = new ItemStack[] {
            new ItemStack(Material.DIAMOND), new ItemStack(Material.DIAMOND), new ItemStack(Material.DIAMOND),
            new ItemStack(Material.DIAMOND), cellGoldStack, new ItemStack(Material.DIAMOND),
            new ItemStack(Material.DIAMOND), new ItemStack(Material.DIAMOND), new ItemStack(Material.DIAMOND)
        };
        registry.registerItem(new me.alikuxac.vortexia.api.item.VortexiaItem("Storage", "cell_diamond", cellDiamondStack, cellDiamondRecipe));

        // 8. Cell Netherite
        ItemStack cellNetheriteStack = me.alikuxac.vortexia.addon.storage.network.StorageItems.getCellNetherite(this);
        ItemStack[] cellNetheriteRecipe = new ItemStack[] {
            new ItemStack(Material.NETHERITE_INGOT), new ItemStack(Material.NETHERITE_INGOT), new ItemStack(Material.NETHERITE_INGOT),
            new ItemStack(Material.NETHERITE_INGOT), cellDiamondStack, new ItemStack(Material.NETHERITE_INGOT),
            new ItemStack(Material.NETHERITE_INGOT), new ItemStack(Material.NETHERITE_INGOT), new ItemStack(Material.NETHERITE_INGOT)
        };
        registry.registerItem(new me.alikuxac.vortexia.api.item.VortexiaItem("Storage", "cell_netherite", cellNetheriteStack, cellNetheriteRecipe));

        // 9. Crafting Upgrade
        ItemStack craftingUpgradeStack = me.alikuxac.vortexia.addon.storage.network.StorageItems.getCraftingUpgrade(this);
        ItemStack[] craftingUpgradeRecipe = new ItemStack[] {
            new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS),
            new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.CRAFTING_TABLE), new ItemStack(Material.OAK_PLANKS),
            new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS)
        };
        registry.registerItem(new me.alikuxac.vortexia.api.item.VortexiaItem("Storage", "upgrade_crafting", craftingUpgradeStack, craftingUpgradeRecipe));
    }
}
