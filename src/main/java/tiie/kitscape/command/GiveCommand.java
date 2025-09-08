package tiie.kitscape.command;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tiie.kitscape.KitScape;
import tiie.kitscape.kit.Kit;

public class GiveCommand {
    public static boolean exec(KitScape plugin, CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /kitscape give <kitName> <player>");
            return true;
        }

        String kitName = args[1].toLowerCase();
        Kit kit = plugin.getKitStorage().getKit(kitName);
        if (kit == null) {
            sender.sendMessage("§cKit §f" + kitName + "§c not found.");
            return true;
        }

        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage("§cPlayer §f" + args[2] + "§c not online.");
            return true;
        }

        // give the items
        for (var is : kit.getItems()) {
            target.getInventory().addItem(is.clone());
        }

        // configurable message (from config.yml messages.claim, replacing %kit% and %player%)
        String msg = plugin.getConfig().getString("messages.claim", "&aYou claimed &f%kit%&a!");
        msg = ChatColor.translateAlternateColorCodes('&', msg)
                .replace("%kit%", kit.getDisplayName())
                .replace("%player%", target.getName());
        sender.sendMessage(msg);
        return true;
    }
}
