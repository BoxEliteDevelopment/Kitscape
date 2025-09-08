package tiie.kitscape.command;

import org.bukkit.entity.Player;
import tiie.kitscape.KitScape;

public class GuiEditCommand {
    
    public static boolean exec(KitScape plugin, Player p, String[] args) {
        if (!p.hasPermission("kitscape.admin")) {
            p.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        
        // Open the main kit selection GUI
        plugin.getKitEditGUI().openMainGUI(p);
        return true;
    }
}
