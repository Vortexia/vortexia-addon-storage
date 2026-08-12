// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class CellSerializer {

    public static String serialize(List<ItemStack> items) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("items", items);
        return config.saveToString();
    }

    public static List<ItemStack> deserialize(String data) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(data);
            List<?> rawList = config.getList("items");
            if (rawList == null) return new ArrayList<>();
            List<ItemStack> items = new ArrayList<>();
            for (Object obj : rawList) {
                if (obj instanceof ItemStack stack) {
                    items.add(stack);
                }
            }
            return items;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
