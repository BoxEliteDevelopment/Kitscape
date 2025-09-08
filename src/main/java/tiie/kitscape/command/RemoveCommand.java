package tiie.kitscape.command;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import tiie.kitscape.KitScape;

import java.io.File;

public class RemoveCommand {
    public static boolean exec(KitScape plugin, Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cUsage: /kitscape remove <kitName>");
            return true;
        }

        String kitName = args[1].toLowerCase();
        File file = plugin.getConfigManager().getKitsFile();
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        var kitsSec = cfg.getConfigurationSection("kits");

        if (kitsSec == null || !kitsSec.isSet(kitName)) {
            p.sendMessage("§cKit §f" + kitName + "§c does not exist.");
            return true;
        }

        kitsSec.set(kitName, null);
        try {
            cfg.save(file);
            plugin.getKitStorage().reload();
            
            // Centered banner-style deletion message with red - indicator
            String kitNameFormatted = ChatColor.YELLOW + kitName + ChatColor.GOLD;
            p.sendMessage(ChatColor.GOLD + "-----------------------------");
            p.sendMessage(ChatColor.YELLOW + "        KitScape");
            p.sendMessage(ChatColor.GRAY + "  - Kit Creation " + kitNameFormatted + " " + ChatColor.RED + "-");
            p.sendMessage(ChatColor.GRAY + "  Kit has been successfully removed!");
            p.sendMessage(ChatColor.GOLD + "-----------------------------");
            
        } catch (Exception ex) {
            plugin.getLogger().severe("Error removing kit " + kitName + ": " + ex.getMessage());
            p.sendMessage("§cAn error occurred while removing the kit.");
        }
        return true;
    }
}
