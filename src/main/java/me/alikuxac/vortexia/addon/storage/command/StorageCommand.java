// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.command;

import me.alikuxac.vortexia.addon.storage.StorageAddon;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class StorageCommand implements BasicCommand {

    private final StorageAddon addon;

    public StorageCommand(StorageAddon addon) {
        this.addon = addon;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(addon.getLanguageManager().getMessage("messages.player_only"));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("creative")) {
            if (!player.hasPermission("vortexia.storage.creative")) {
                player.sendMessage(addon.getLanguageManager().getMessage("no_permission"));
                return;
            }
            me.alikuxac.vortexia.api.VortexiaProvider.get().openGuide(player, "Storage");
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("getremote")) {
            if (!player.hasPermission("vortexia.storage.admin")) {
                player.sendMessage(addon.getLanguageManager().getMessage("no_permission"));
                return;
            }
            player.getInventory().addItem(me.alikuxac.vortexia.addon.storage.network.StorageItems.getWirelessRemote(addon));
            player.sendMessage(addon.getLanguageManager().getMessage("prefix").append(addon.getLanguageManager().getMessage("messages.remote_receive_success")));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("cloud")) {
            if (args.length >= 4 && args[1].equalsIgnoreCase("create")) {
                String name = args[2];
                String pass = args[3];
                me.alikuxac.vortexia.addon.storage.network.CloudChannelManager.CloudChannel channel = addon.getChannelManager().createChannel(name, player.getUniqueId(), pass);
                player.sendMessage(addon.getLanguageManager().getMessage("prefix").append(addon.getLanguageManager().getMessage("messages.cloud_channel_created").replaceText(config -> config.matchLiteral("%name%").replacement(Component.text(name))).replaceText(config -> config.matchLiteral("%id%").replacement(Component.text(channel.id)))));
                return;
            }
            if (args.length >= 3 && args[1].equalsIgnoreCase("join")) {
                String id = args[2];
                String pass = args.length > 3 ? args[3] : "";
                if (addon.getChannelManager().canAccess(id, player.getUniqueId(), pass)) {
                    if (player.hasMetadata("vortexia_editing_node")) {
                        org.bukkit.Location loc = (org.bukkit.Location) player.getMetadata("vortexia_editing_node").get(0).value();
                        me.alikuxac.vortexia.addon.storage.network.CloudDriveNode node = (me.alikuxac.vortexia.addon.storage.network.CloudDriveNode) addon.getNetworkManager().getNode(loc);
                        if (node != null) {
                            node.setCloudId("channel:" + id);
                            addon.getChannelManager().addMember(id, player.getUniqueId());
                            player.sendMessage(addon.getLanguageManager().getMessage("prefix").append(addon.getLanguageManager().getMessage("messages.cloud_channel_connected").replaceText(config -> config.matchLiteral("%id%").replacement(Component.text(id)))));
                            return;
                        }
                    }
                    player.sendMessage(addon.getLanguageManager().getMessage("messages.cloud_gui_required"));
                } else {
                    player.sendMessage(addon.getLanguageManager().getMessage("messages.cloud_auth_failed"));
                }
                return;
            }
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("update")) {
            me.alikuxac.vortexia.addon.storage.util.UpdateChecker checker = addon.getUpdateChecker();
            if (checker == null) {
                player.sendMessage(addon.getLanguageManager().getMessage("update.disabled"));
                return;
            }
            player.sendMessage(addon.getLanguageManager().getMessage("update.checking"));
            checker.checkAsync();
            org.bukkit.Bukkit.getScheduler().runTaskLater(addon, () -> {
                if (!checker.hasChecked()) {
                    player.sendMessage(addon.getLanguageManager().getMessage("update.check_failed"));
                    return;
                }
                if (checker.isUpdateAvailable()) {
                    player.sendMessage(addon.getLanguageManager().getMessage("update.notify")
                            .replaceText(config -> config.matchLiteral("%latest%").replacement(Component.text(checker.getLatestVersion())))
                            .replaceText(config -> config.matchLiteral("%current%").replacement(Component.text(checker.getCurrentVersion()))));
                    player.sendMessage(addon.getLanguageManager().getMessage("update.download"));
                } else {
                    player.sendMessage(addon.getLanguageManager().getMessage("update.up_to_date")
                            .replaceText(config -> config.matchLiteral("%version%").replacement(Component.text(checker.getCurrentVersion()))));
                }
            }, 60L);
            return;
        }

        player.sendMessage(addon.getLanguageManager().getMessage("commands.help_header").replaceText(config -> config.matchLiteral("%version%").replacement(Component.text(addon.getVersion()))));
        player.sendMessage(addon.getLanguageManager().getMessage("commands.creative"));
        player.sendMessage(addon.getLanguageManager().getMessage("commands.getremote"));
        player.sendMessage(addon.getLanguageManager().getMessage("commands.cloud_create"));
        player.sendMessage(addon.getLanguageManager().getMessage("commands.cloud_join"));
        player.sendMessage(addon.getLanguageManager().getMessage("commands.update"));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("creative", "cloud", "getremote", "update");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("cloud")) {
            return List.of("create", "join");
        }
        return List.of();
    }
}
