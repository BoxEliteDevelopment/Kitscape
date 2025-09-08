package tiie.kitscape.command;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import tiie.kitscape.KitScape;

public class HelpCommand {

    public static boolean exec(KitScape plugin, Player p) {
        var cfg = plugin.getConfig();
        String logoHex  = cfg.getString("branding.logo_hex", "#FFD700");
        String logoText = cfg.getString("branding.logo_text", "K I T S C A P E");

        String logo = ChatColor.of(logoHex) + "" + ChatColor.BOLD + logoText + ChatColor.RESET;
        p.sendMessage(" ");
        p.sendMessage(logo);
        p.sendMessage(" ");

        // everyone
        p.sendMessage("§e/kits  §7Open kits GUI");
        p.sendMessage("§e/kitscape help  §7Show this help");
        p.sendMessage(" ");

        // admin block
        if (p.hasPermission("kitscape.admin")) {
            p.sendMessage("§6Admin:");
            p.sendMessage("§e/kitscape create <name>        §7Start create session");
            p.sendMessage("§e/kitscape confirm              §7Save current inv as kit");
            p.sendMessage("§e/kitscape remove <name>        §7Delete a kit");
            p.sendMessage("§e/kitscape reload               §7Reload configs");
            p.sendMessage("§e/kitscape list [kit]           §7List kits or lore indices");
            p.sendMessage("§e/kitscape give <kit> <player>  §7Give kit to player");
            p.sendMessage("§e/kitscape edit <kit> slot <n>  §7Move kit slot");
            p.sendMessage("§e/kitscape edit <kit> displayname <text>");
            p.sendMessage("§e/kitscape edit <kit> material <material>");
            p.sendMessage("§e/kitscape edit <kit> lore add|remove|set …");
            p.sendMessage("§e/kitscape edit <kit> cooldown 1s/1m/1h/1d/1w/1mth/1y/onetime");
        }
        p.sendMessage(" ");
        return true;
    }
}
