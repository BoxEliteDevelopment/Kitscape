package tiie.kitscape.command;

import org.bukkit.entity.Player;
import tiie.kitscape.KitScape;

public class ReloadCommand {

    public static boolean exec(KitScape plugin, Player p) {
        plugin.reloadConfig();
        plugin.getConfigManager();
        plugin.getKitStorage().reload();
        p.sendMessage("§aAll Kitscape configurations reloaded.");
        return true;
    }
}
