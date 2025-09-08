package tiie.kitscape.tabcomplete;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import tiie.kitscape.KitScape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KitscapeTabCompleter implements TabCompleter {
    private final KitScape plugin;
    private final List<String> subs = List.of(
            "create", "confirm", "delete", "reload", "give", "list", "edit", "help", "author", "guiedit"
    );

    public KitscapeTabCompleter(KitScape plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,
                                      Command cmd,
                                      String alias,
                                      String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        if (args.length == 1) {
            return subs.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        String sub = args[0].toLowerCase();
        if (args.length == 2) {
            if (List.of("give", "delete", "edit").contains(sub)) {
                return plugin.getKitStorage().getAll().keySet().stream()
                        .filter(k -> k.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        if ("edit".equals(sub)) {
            if (args.length == 3) {
                return List.of("slot","displayname","material","lore","cooldown","particle").stream()
                        .filter(f -> f.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 4 && "lore".equalsIgnoreCase(args[2])) {
                return List.of("add","remove","set").stream()
                        .filter(o -> o.startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 5 && "lore".equalsIgnoreCase(args[2])
                    && List.of("remove","set").contains(args[3].toLowerCase())) {
                List<String> lore = plugin.getConfig()
                        .getStringList("kits." + args[1] + ".icon.lore");
                return lore.stream().map(String::valueOf)
                        .filter(idx -> idx.startsWith(args[4]))
                        .collect(Collectors.toList());
            }
            if (args.length == 4 && "cooldown".equalsIgnoreCase(args[2])) {
                return List.of(
                                "1s","1m","1h","1d","1w","1mth","1y","onetime"
                        ).stream()
                        .filter(opt -> opt.startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 4 && "particle".equalsIgnoreCase(args[2])) {
                return List.of("reset","title","actionbar","particles","sounds").stream()
                        .filter(opt -> opt.startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 5 && "particle".equalsIgnoreCase(args[2]) && "particles".equalsIgnoreCase(args[3])) {
                return List.of("EXPLOSION","FIREWORK","FLAME","SMOKE","CLOUD","SPARK","HEART","NOTE","WATER_DROP","LAVA",
                              "DRAGON_BREATH","END_ROD","MAGIC_CRIT","ENCHANTMENT_TABLE","PORTAL","REDSTONE","SLIME","SNOWBALL",
                              "VILLAGER_HAPPY","VILLAGER_ANGRY").stream()
                        .filter(opt -> opt.startsWith(args[4].toUpperCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 5 && "particle".equalsIgnoreCase(args[2]) && "sounds".equalsIgnoreCase(args[3])) {
                return List.of("ENTITY_PLAYER_LEVELUP","ENTITY_PLAYER_BURP","ENTITY_PLAYER_SPLASH",
                              "ENTITY_GENERIC_EXPLODE","BLOCK_ANVIL_LAND","BLOCK_GLASS_BREAK",
                              "BLOCK_ENCHANTMENT_TABLE_USE","ITEM_BOTTLE_FILL_DRAGONBREATH","ENTITY_ILLUSIONER_MIRROR_MOVE",
                              "UI_TOAST_IN","UI_TOAST_OUT").stream()
                        .filter(opt -> opt.startsWith(args[4].toUpperCase()))
                        .collect(Collectors.toList());
            }
        }

        if ("edit".equals(sub) && args.length == 4
                && "material".equalsIgnoreCase(args[2])) {

            return Arrays.stream(Material.values())
                    .map(Material::name)
                    .filter(m -> m.startsWith(args[3].toUpperCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
