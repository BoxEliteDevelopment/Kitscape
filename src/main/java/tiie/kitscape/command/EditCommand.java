package tiie.kitscape.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import tiie.kitscape.KitScape;
import tiie.kitscape.kit.Kit;
import tiie.kitscape.kit.KitStorage;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

public class EditCommand {

    private static final Pattern DURATION = Pattern.compile("^(\\d+)(s|m|h|d|w|mth|y)$");

    public static boolean exec(KitScape plugin, Player p, String[] args) {
        if (args.length < 4) {
            p.sendMessage("§cUsage: /kitscape edit <kit> <field> <...>");
            return true;
        }
        String kitName = args[1].toLowerCase();
        KitStorage storage = plugin.getKitStorage();
        Kit kit = storage.getKit(kitName);
        if (kit == null) {
            p.sendMessage("§cKit '" + kitName + "' not found.");
            return true;
        }

        String field = args[2].toLowerCase();
        File file = plugin.getConfigManager().getKitsFile();
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection kitsSec = cfg.getConfigurationSection("kits");
        ConfigurationSection sec = kitsSec.getConfigurationSection(kitName);

        switch (field) {


            case "slot" -> {
                int slot;
                try { slot = Integer.parseInt(args[3]); }
                catch (Exception e) { p.sendMessage("§cInvalid slot."); return true; }
                // warn if in use
                for (String other : kitsSec.getKeys(false)) {
                    if (!other.equalsIgnoreCase(kitName)
                            && kitsSec.getConfigurationSection(other).getInt("slot") == slot) {
                        p.sendMessage("§eWarning: slot " + slot + " used by " + other);
                        break;
                    }
                }
                sec.set("slot", slot);
            }
            case "displayname" -> {
                String disp = String.join(" ", List.of(args).subList(3, args.length));
                sec.set("icon.name", disp);
            }
            case "material" -> {
                String matStr = args[3].toUpperCase();
                Material mat = Material.matchMaterial(matStr);
                if (mat == null) { p.sendMessage("§cUnknown material."); return true; }
                ConfigurationSection icon = sec.getConfigurationSection("icon");
                if (icon == null) icon = sec.createSection("icon");
                icon.set("material", matStr);
            }
            case "lore" -> {
                if (args.length < 5) { p.sendMessage("§cUsage: lore add|remove|set ..."); return true; }
                String op = args[3].toLowerCase();
                ConfigurationSection icon = sec.getConfigurationSection("icon");
                if (icon == null) icon = sec.createSection("icon");
                List<String> lore = icon.getStringList("lore");
                switch (op) {
                    case "add" -> {
                        String text = String.join(" ", List.of(args).subList(4, args.length));
                        lore.add(text);
                    }
                    case "remove" -> {
                        int idx;
                        try { idx = Integer.parseInt(args[4]); }
                        catch (Exception e) { p.sendMessage("§cInvalid index."); return true; }
                        if (idx < 0 || idx >= lore.size()) {
                            p.sendMessage("§cIndex out of range."); return true;
                        }
                        lore.remove(idx);
                    }
                    case "set" -> {
                        int idx;
                        try { idx = Integer.parseInt(args[4]); }
                        catch (Exception e) { p.sendMessage("§cInvalid index."); return true; }
                        if (idx < 0 || idx >= lore.size()) {
                            p.sendMessage("§cIndex out of range."); return true;
                        }
                        String text = String.join(" ", List.of(args).subList(5, args.length));
                        lore.set(idx, text);
                    }
                    default -> {
                        p.sendMessage("§cUsage: lore add|remove|set"); return true;
                    }
                }
                icon.set("lore", lore);
            }

            case "cooldown" -> {
                String dur = args[3].toLowerCase();
                ConfigurationSection cdSec = sec.getConfigurationSection("cooldown");
                if (cdSec == null) cdSec = sec.createSection("cooldown");
                // clear existing cooldown keys
                for (String key : List.copyOf(cdSec.getKeys(false))) {
                    cdSec.set(key, null);
                }
                if (dur.equals("onetime")) {
                    cdSec.set("oneTimeUse", 1);
                } else {
                    Matcher m = DURATION.matcher(dur);
                    if (!m.matches()) {
                        p.sendMessage("§cInvalid duration. Use e.g. 1s,1m,1h,1d,1w,1mth,1y or onetime");
                        return true;
                    }
                    long amount = Long.parseLong(m.group(1));
                    String unit = m.group(2);
                    switch (unit) {
                        case "s"   -> cdSec.set("seconds", amount);
                        case "m"   -> cdSec.set("minutes", amount);
                        case "h"   -> cdSec.set("hours", amount);
                        case "d"   -> cdSec.set("days", amount);
                        case "w"   -> cdSec.set("weeks", amount);
                        case "mth" -> cdSec.set("months", amount);
                        case "y"   -> cdSec.set("years", amount);
                    }
                }
            }

            case "particle" -> {
                if (args.length < 4) { 
                    p.sendMessage("§cUsage: particle <subcommand> [options]"); 
                    p.sendMessage("§7Subcommands: reset, title, actionbar, particles, sounds");
                    return true; 
                }
                
                String subCommand = args[3].toLowerCase();
                ConfigurationSection particleSec = sec.getConfigurationSection("particles");
                if (particleSec == null) {
                    particleSec = sec.createSection("particles");
                }
                
                switch (subCommand) {
                    case "reset" -> {
                        particleSec.set("enabled", true);
                        // Create default particle effects
                        tiie.kitscape.utils.ParticleEffects.createDefaultParticleConfig(sec);
                        p.sendMessage("§aParticle effects reset to default!");
                    }
                    case "title" -> {
                        if (args.length < 5) {
                            p.sendMessage("§cUsage: particle title <title> [subtitle]");
                            return true;
                        }
                        String title = String.join(" ", List.of(args).subList(4, args.length));
                        ConfigurationSection titleSec = particleSec.getConfigurationSection("title");
                        if (titleSec == null) titleSec = particleSec.createSection("title");
                        titleSec.set("enabled", true);
                        titleSec.set("title", title);
                    }
                    case "actionbar" -> {
                        if (args.length < 5) {
                            p.sendMessage("§cUsage: particle actionbar <message>");
                            return true;
                        }
                        String message = String.join(" ", List.of(args).subList(4, args.length));
                        ConfigurationSection actionBarSec = particleSec.getConfigurationSection("actionbar");
                        if (actionBarSec == null) actionBarSec = particleSec.createSection("actionbar");
                        actionBarSec.set("enabled", true);
                        actionBarSec.set("message", message);
                    }
                    case "particles" -> {
                        if (args.length < 6) {
                            p.sendMessage("§cUsage: particle particles <type> <count> [offsetX] [offsetY] [offsetZ] [speed]");
                            p.sendMessage("§7Example: particle particles EXPLOSION 10 0.5 0.5 0.5 0.1");
                            return true;
                        }
                        String particleType = args[4].toUpperCase();
                        int count = Integer.parseInt(args[5]);
                        double offsetX = args.length > 6 ? Double.parseDouble(args[6]) : 0.5;
                        double offsetY = args.length > 7 ? Double.parseDouble(args[7]) : 0.5;
                        double offsetZ = args.length > 8 ? Double.parseDouble(args[8]) : 0.5;
                        double speed = args.length > 9 ? Double.parseDouble(args[9]) : 0.1;
                        
                        List<Map<?, ?>> effects = particleSec.getMapList("particles.effects");
                        Map<String, Object> newEffect = Map.of(
                            "type", particleType,
                            "count", count,
                            "offsetX", offsetX,
                            "offsetY", offsetY,
                            "offsetZ", offsetZ,
                            "speed", speed
                        );
                        effects.add(newEffect);
                        particleSec.set("particles.effects", effects);
                    }
                    case "sounds" -> {
                        if (args.length < 6) {
                            p.sendMessage("§cUsage: particle sounds <sound> <volume> [pitch]");
                            p.sendMessage("§7Example: particle sounds ENTITY_PLAYER_LEVELUP 1.0 1.0");
                            return true;
                        }
                        String soundType = args[4].toUpperCase();
                        float volume = Float.parseFloat(args[5]);
                        float pitch = args.length > 6 ? Float.parseFloat(args[6]) : 1.0f;
                        
                        List<Map<?, ?>> effects = particleSec.getMapList("sounds.effects");
                        Map<String, Object> newEffect = Map.of(
                            "type", soundType,
                            "volume", volume,
                            "pitch", pitch
                        );
                        effects.add(newEffect);
                        particleSec.set("sounds.effects", effects);
                    }
                    default -> {
                        p.sendMessage("§cUnknown particle subcommand. Use: reset, title, actionbar, particles, sounds");
                        return true;
                    }
                }
            }

            default -> {
                p.sendMessage("§cUnknown field. Use slot, displayname, material, lore, cooldown, or particle.");
                return true;
            }
        }



        try {
            cfg.save(file);
            storage.reload();
            
            // Centered banner-style update message
            String kitNameFormatted = ChatColor.YELLOW + kitName + ChatColor.GOLD;
            String fieldFormatted = ChatColor.YELLOW + field + ChatColor.GOLD;
            p.sendMessage(ChatColor.GOLD + "-----------------------------");
            p.sendMessage(ChatColor.YELLOW + "        KitScape");
            p.sendMessage(ChatColor.GRAY + "  - Kit Creation " + kitNameFormatted);
            p.sendMessage(ChatColor.GRAY + "  Field '" + fieldFormatted + "' has been modified!");
            p.sendMessage(ChatColor.GOLD + "-----------------------------");
            
        } catch (Exception ex) {
            plugin.getLogger().severe("Error saving kits.yml: " + ex.getMessage());
            p.sendMessage("§cFailed to save config.");
        }
        return true;
    }
}
