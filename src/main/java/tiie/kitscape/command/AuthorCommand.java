package tiie.kitscape.command;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import tiie.kitscape.KitScape;

public class AuthorCommand {

    private static final String AUTHOR_NAME = "TiIe";
    private static final String DISCORD_TAG = "Adminesty";
    private static final String DISCORD_HEX = "#5865F2";         // Discord blurple
    private static final String LOGO_HEX    = "#FFD700";         // Bright gold

    public static boolean exec(KitScape plugin, Player p) {
        final String DC = ChatColor.of(DISCORD_HEX).toString();
        final String LG = ChatColor.of(LOGO_HEX).toString();
        final String B  = ChatColor.BOLD.toString();
        final String R  = ChatColor.RESET.toString();
        final String G2 = ChatColor.DARK_GRAY.toString();

        p.sendMessage(G2 + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + R);
        p.sendMessage(LG + B + "KitScape" + R + " " + G2 + "• a clean, professional kits plugin" + R);
        p.sendMessage(" ");
        p.sendMessage("§7Author: " + LG + AUTHOR_NAME + R);
        p.sendMessage(DC + B + "[DISCORD]" + R + " " + DC + DISCORD_TAG + R);
        p.sendMessage(G2 + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + R);
        return true;
    }
}
