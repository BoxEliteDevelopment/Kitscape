package tiie.kitscape.command;

import org.bukkit.entity.Player;
import tiie.kitscape.KitScape;
import tiie.kitscape.kit.Kit;

public class ListCommand {
    public static boolean exec(KitScape plugin, Player p) {
        var kits = plugin.getKitStorage().getAll();
        if (kits.isEmpty()) {
            p.sendMessage("§eNo kits available.");
            return true;
        }
        p.sendMessage("§6Available Kits:");
        for (Kit kit : kits.values()) {
            p.sendMessage(" - §f" + kit.getName());
        }
        return true;
    }
}
