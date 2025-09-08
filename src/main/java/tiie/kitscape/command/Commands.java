package tiie.kitscape.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tiie.kitscape.KitScape;

public class Commands implements CommandExecutor {
    private final KitScape plugin;

    public Commands(KitScape plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!(s instanceof Player p)) {
            s.sendMessage("Only players.");
            return true;
        }




        if (args.length<1) { plugin.getGui().open(p); return true; }

        if (args[0].equalsIgnoreCase("help"))   return HelpCommand.exec(plugin, p);
        if (args[0].equalsIgnoreCase("author")) return AuthorCommand.exec(plugin, p);



        String sub = args[0].toLowerCase();
        if (!p.hasPermission("kitscape.admin")) {
            p.sendMessage("§cYou lack permission (kitscape.admin).");
            return true;
        }


        switch (sub) {
            case "create":   return CreateCommand.exec(plugin, p, args);
            case "confirm":  return ConfirmCommand.exec(plugin, p);
            case "delete":   return RemoveCommand.exec(plugin, p, args);
            case "reload":   return ReloadCommand.exec(plugin, p);
            case "give":     return GiveCommand.exec(plugin, p, args);
            case "edit":  return EditCommand.exec(plugin, p, args);
            case "list":     return ListCommand.exec(plugin, p);
            case "guiedit":  return GuiEditCommand.exec(plugin, p, args);

            //  case "inventoryrollback":
              //  return Rollback.exec(plugin, p, args);
            default: return false;
        }
    }
}
