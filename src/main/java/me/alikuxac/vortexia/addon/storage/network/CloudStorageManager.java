// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.alikuxac.vortexia.addon.storage.StorageAddon;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.*;

public class CloudStorageManager {

    private final StorageAddon addon;
    private final Map<String, List<ItemStack>> cloudInventories = new HashMap<>();
    private final Gson gson = new GsonBuilder().create();

    public CloudStorageManager(StorageAddon addon) {
        this.addon = addon;
    }

    public List<ItemStack> getInventory(String storageId) {
        if (cloudInventories.containsKey(storageId)) {
            return cloudInventories.get(storageId);
        }

        // Try load from DB
        String json = addon.getDatabaseManager().getCloudData(storageId);
        List<ItemStack> items = new ArrayList<>();
        if (json != null) {
            Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
            List<Map<String, Object>> data = gson.fromJson(json, type);
            for (Map<String, Object> itemData : data) {
                items.add(ItemStack.deserialize(itemData));
            }
        }
        cloudInventories.put(storageId, items);
        return items;
    }

    public void addItem(String storageId, ItemStack item) {
        List<ItemStack> inv = getInventory(storageId);
        boolean stacked = false;
        for (ItemStack existing : inv) {
            if (existing.isSimilar(item)) {
                existing.setAmount(existing.getAmount() + item.getAmount());
                stacked = true;
                break;
            }
        }
        if (!stacked) inv.add(item.clone());
        save(storageId);
    }

    public ItemStack removeItem(String storageId, ItemStack template, int amount) {
        List<ItemStack> inv = getInventory(storageId);
        Iterator<ItemStack> it = inv.iterator();
        while (it.hasNext()) {
            ItemStack existing = it.next();
            if (existing.isSimilar(template)) {
                int toRemove = Math.min(existing.getAmount(), amount);
                ItemStack result = existing.clone();
                result.setAmount(toRemove);
                
                existing.setAmount(existing.getAmount() - toRemove);
                if (existing.getAmount() <= 0) it.remove();
                
                save(storageId);
                return result;
            }
        }
        return null;
    }

    private void save(String storageId) {
        List<ItemStack> inv = cloudInventories.get(storageId);
        if (inv == null) return;

        List<Map<String, Object>> serialized = new ArrayList<>();
        for (ItemStack stack : inv) {
            serialized.add(stack.serialize());
        }
        String json = gson.toJson(serialized);
        addon.getDatabaseManager().saveCloudData(storageId, json);
    }
}
