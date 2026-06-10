// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.recipe;

import me.alikuxac.vortexia.addon.storage.StorageAddon;
import me.alikuxac.vortexia.addon.storage.network.StorageItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

public class StorageRecipeManager {

    private final StorageAddon addon;

    public StorageRecipeManager(StorageAddon addon) {
        this.addon = addon;
    }

    public void registerRecipes() {
        // ME Controller Recipe
        registerShaped(new NamespacedKey(addon, "controller"), StorageItems.getController(addon), 
            "IGI", "GRG", "IGI", 
            'I', Material.IRON_INGOT, 'G', Material.GOLD_INGOT, 'R', Material.REDSTONE_BLOCK);

        // ME Drive Recipe
        registerShaped(new NamespacedKey(addon, "drive"), StorageItems.getDrive(addon), 
            "III", "RCR", "III", 
            'I', Material.IRON_INGOT, 'R', Material.REDSTONE, 'C', Material.CHEST);

        // ME Terminal Recipe
        registerShaped(new NamespacedKey(addon, "terminal"), StorageItems.getTerminal(addon), 
            "GGG", "RPR", "III", 
            'G', Material.GLASS, 'R', Material.REDSTONE, 'P', Material.GLASS_PANE, 'I', Material.IRON_INGOT);
            
        // --- STORAGE CELLS PROGRESSION ---

        // 1k Cell (Copper)
        registerShaped(new NamespacedKey(addon, "cell_copper"), StorageItems.getCellCopper(addon), 
            "CCC", "CGC", "CCC", 
            'C', Material.COPPER_INGOT, 'G', Material.GLASS);

        // 4k Cell (Iron) - Requires Copper Cell
        registerCellUpgrade(new NamespacedKey(addon, "cell_iron"), StorageItems.getCellIron(addon), 
            StorageItems.getCellCopper(addon), Material.IRON_INGOT);

        // 16k Cell (Gold) - Requires Iron Cell
        registerCellUpgrade(new NamespacedKey(addon, "cell_gold"), StorageItems.getCellGold(addon), 
            StorageItems.getCellIron(addon), Material.GOLD_INGOT);

        // 64k Cell (Diamond) - Requires Gold Cell
        registerCellUpgrade(new NamespacedKey(addon, "cell_diamond"), StorageItems.getCellDiamond(addon), 
            StorageItems.getCellGold(addon), Material.DIAMOND);

        // 256k Cell (Netherite) - Requires Diamond Cell
        registerCellUpgrade(new NamespacedKey(addon, "cell_netherite"), StorageItems.getCellNetherite(addon), 
            StorageItems.getCellDiamond(addon), Material.NETHERITE_INGOT);

        // Crafting Upgrade
        registerShaped(new NamespacedKey(addon, "upgrade_crafting"), StorageItems.getCraftingUpgrade(addon),
            "WWW", "WCW", "WWW", 'W', Material.OAK_PLANKS, 'C', Material.CRAFTING_TABLE);

        // Custom 5x5 Mechanical Crafting Recipe for ME Controller
        RecipeChoice[][] ingredients5x5 = new RecipeChoice[5][5];
        ingredients5x5[0][0] = new RecipeChoice.MaterialChoice(Material.IRON_INGOT);
        ingredients5x5[0][2] = new RecipeChoice.MaterialChoice(Material.IRON_INGOT);
        ingredients5x5[0][4] = new RecipeChoice.MaterialChoice(Material.IRON_INGOT);
        ingredients5x5[4][0] = new RecipeChoice.MaterialChoice(Material.IRON_INGOT);
        ingredients5x5[4][2] = new RecipeChoice.MaterialChoice(Material.IRON_INGOT);
        ingredients5x5[4][4] = new RecipeChoice.MaterialChoice(Material.IRON_INGOT);
        
        ingredients5x5[1][1] = new RecipeChoice.MaterialChoice(Material.GOLD_INGOT);
        ingredients5x5[1][2] = new RecipeChoice.MaterialChoice(Material.GOLD_INGOT);
        ingredients5x5[1][3] = new RecipeChoice.MaterialChoice(Material.GOLD_INGOT);
        ingredients5x5[2][1] = new RecipeChoice.MaterialChoice(Material.GOLD_INGOT);
        ingredients5x5[2][3] = new RecipeChoice.MaterialChoice(Material.GOLD_INGOT);
        ingredients5x5[3][1] = new RecipeChoice.MaterialChoice(Material.GOLD_INGOT);
        ingredients5x5[3][2] = new RecipeChoice.MaterialChoice(Material.GOLD_INGOT);
        ingredients5x5[3][3] = new RecipeChoice.MaterialChoice(Material.GOLD_INGOT);
        
        ingredients5x5[2][2] = new RecipeChoice.MaterialChoice(Material.REDSTONE_BLOCK);

        me.alikuxac.vortexia.api.VortexiaProvider.get().getCustomRecipeManager().registerRecipe(
            me.alikuxac.vortexia.api.recipe.CustomRecipe.of(
                new NamespacedKey(addon, "controller_5x5"),
                5, 5,
                ingredients5x5,
                StorageItems.getController(addon)
            )
        );
    }

    private void registerCellUpgrade(NamespacedKey key, org.bukkit.inventory.ItemStack result, org.bukkit.inventory.ItemStack previous, Material material) {
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("MMM", "MPM", "MMM");
        recipe.setIngredient('M', material);
        recipe.setIngredient('P', new RecipeChoice.ExactChoice(previous));
        Bukkit.addRecipe(recipe);
    }

    private void registerShaped(NamespacedKey key, org.bukkit.inventory.ItemStack result, String row1, String row2, String row3, Object... ingredients) {
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(row1, row2, row3);
        for (int i = 0; i < ingredients.length; i += 2) {
            recipe.setIngredient((Character) ingredients[i], (Material) ingredients[i + 1]);
        }
        Bukkit.addRecipe(recipe);
    }
}
