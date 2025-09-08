package tiie.kitscape.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tiie.kitscape.KitScape;

public class CreateCommand {

    private static final long DEFAULT_TIMEOUT = 30; // seconds

    public static boolean exec(KitScape plugin, CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Only players can create kits.");
            return true;
        }
        if (args.length < 2) {
            p.sendMessage("Usage: /kitscape create <name>");
            return true;
        }
        String kitName = args[1].toLowerCase();
        plugin.getSessionMgr().start(p.getUniqueId(), kitName, DEFAULT_TIMEOUT);
        
        // Centered banner-style creation message
        String kitNameFormatted = ChatColor.YELLOW + kitName + ChatColor.GOLD;
        String timeoutFormatted = ChatColor.YELLOW + String.valueOf(DEFAULT_TIMEOUT) + ChatColor.GOLD;
        
        p.sendMessage(ChatColor.GOLD + "-----------------------------");
        p.sendMessage(ChatColor.YELLOW + "        KitScape");
        p.sendMessage(ChatColor.GRAY + "  - Kit Creation " + kitNameFormatted);
        p.sendMessage(ChatColor.GRAY + "  Run " + ChatColor.YELLOW + "/kitscape confirm" + ChatColor.GRAY + " within " + timeoutFormatted + "s to finalize");
        p.sendMessage(ChatColor.GOLD + "-----------------------------");
        
        return true;
    }
}
