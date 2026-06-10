// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.listener;

import me.alikuxac.vortexia.addon.storage.StorageAddon;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateNotifyListener implements Listener {

    private final StorageAddon addon;

    public UpdateNotifyListener(StorageAddon addon) {
        this.addon = addon;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("vortexia.storage.admin")) return;

        me.alikuxac.vortexia.addon.storage.util.UpdateChecker checker = addon.getUpdateChecker();
        if (checker == null || !checker.hasChecked() || !checker.isUpdateAvailable()) return;

        player.sendMessage(addon.getLanguageManager().getMessage("update.notify")
                .replaceText(config -> config.matchLiteral("%latest%").replacement(Component.text(checker.getLatestVersion())))
                .replaceText(config -> config.matchLiteral("%current%").replacement(Component.text(checker.getCurrentVersion()))));
        player.sendMessage(addon.getLanguageManager().getMessage("update.download"));
    }
}
