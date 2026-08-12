// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import me.alikuxac.vortexia.addon.storage.api.StorageCell;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StorageCellImpl implements StorageCell {

    private final ItemStack cellItem;
    private final NamespacedKey dataKey;
    private final int capacity;

    public StorageCellImpl(ItemStack item, NamespacedKey dataKey, int capacity) {
        this.cellItem = item;
        this.dataKey = dataKey;
        this.capacity = capacity;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public List<ItemStack> getStoredItems() {
        ItemMeta meta = cellItem.getItemMeta();
        if (meta == null) return new ArrayList<>();
        
        String encodedItems = meta.getPersistentDataContainer().get(dataKey, PersistentDataType.STRING);
        if (encodedItems == null || encodedItems.isEmpty()) return new ArrayList<>();

        try {
            return decodeItems(encodedItems);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public ItemStack insert(ItemStack item) {
        List<ItemStack> items = getStoredItems();
        ItemStack remaining = item.clone();

        // Check if we already have this item
        for (ItemStack stored : items) {
            if (stored.isSimilar(remaining)) {
                int canAdd = Math.min(remaining.getAmount(), 64 - stored.getAmount()); // Simple stack limit for now
                stored.setAmount(stored.getAmount() + canAdd);
                remaining.setAmount(remaining.getAmount() - canAdd);
                if (remaining.getAmount() <= 0) break;
            }
        }

        if (remaining.getAmount() > 0) {
            // Add as new entry if capacity allows (implement capacity logic later)
            items.add(remaining.clone());
            remaining.setAmount(0);
        }

        saveItems(items);
        return remaining;
    }

    @Override
    public ItemStack extract(ItemStack matcher, int amount) {
        List<ItemStack> items = getStoredItems();
        ItemStack extracted = null;

        for (ItemStack stored : items) {
            if (stored.isSimilar(matcher)) {
                int toTake = Math.min(amount, stored.getAmount());
                extracted = stored.clone();
                extracted.setAmount(toTake);
                stored.setAmount(stored.getAmount() - toTake);
                break;
            }
        }

        items.removeIf(i -> i.getAmount() <= 0);
        saveItems(items);
        return extracted;
    }

    private void saveItems(List<ItemStack> items) {
        try {
            String encoded = encodeItems(items);
            ItemMeta meta = cellItem.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(dataKey, PersistentDataType.STRING, encoded);
                cellItem.setItemMeta(meta);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String encodeItems(List<ItemStack> items) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        dataOutput.writeInt(items.size());
        for (ItemStack item : items) {
            dataOutput.writeObject(item);
        }
        dataOutput.close();
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    private List<ItemStack> decodeItems(String data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        int size = dataInput.readInt();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            items.add((ItemStack) dataInput.readObject());
        }
        dataInput.close();
        return items;
    }
}
