package tiie.kitscape.command;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.block.banner.Pattern;
import tiie.kitscape.KitScape;
import tiie.kitscape.kit.KitStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import tiie.kitscape.kit.Kit;
import org.bukkit.ChatColor;

public class ConfirmCommand {

    public static boolean exec(KitScape plugin, Player p) {
        String kitName = plugin.getSessionMgr().confirm(p.getUniqueId());
        if (kitName == null) {
            p.sendMessage("§cNo pending kit creation or session expired.");
            return true;
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (ItemStack is : p.getInventory().getContents()) {
            if (is == null) continue;
            Material mat = is.getType();
            if (mat.isAir() || !mat.isItem()) continue;
            
            // Create enhanced item configuration
            Map<String, Object> itemConfig = createEnhancedItemConfig(is);
            items.add(itemConfig);
        }

        KitStorage storage = plugin.getKitStorage();
        List<Integer> used = storage.getAll().values().stream()
                .map(Kit::getSlot)
                .collect(Collectors.toList());
        int slot = 0;
        while (used.contains(slot) && slot < 54) slot++;
        if (slot >= 54) {
            p.sendMessage("§cCannot create more kits: GUI is full.");
            return true;
        }

        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection kitsSec = cfg.getConfigurationSection("kits");
            if (kitsSec == null) kitsSec = cfg.createSection("kits");

            ConfigurationSection newKit = kitsSec.createSection(kitName);
            newKit.set("displayName", "&e" + kitName);
            newKit.set("items", items);
            newKit.createSection("cooldown").set("seconds", 60L);
            newKit.set("slot", slot);
            newKit.set("hexColor", "#FF5500");

            ConfigurationSection iSec = newKit.createSection("icon");
            iSec.set("material", "CHEST");
            iSec.set("amount", 1);
            iSec.set("name", "&e" + kitName);
            iSec.set("lore", List.of("&7– Edit this kit's icon in the config –"));

            cfg.save(file);
            plugin.getKitStorage().reload();
            
            // Centered banner-style success message with green + indicator
            String kitNameFormatted = ChatColor.YELLOW + kitName + ChatColor.GOLD;
            p.sendMessage(ChatColor.GOLD + "-----------------------------");
            p.sendMessage(ChatColor.YELLOW + "        KitScape");
            p.sendMessage(ChatColor.GRAY + "  - Kit Creation " + kitNameFormatted + " " + ChatColor.GREEN + "+");
            p.sendMessage(ChatColor.GRAY + "  Kit has been successfully created");
            p.sendMessage(ChatColor.GOLD + "-----------------------------");

        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to save kit " + kitName + ": " + ex.getMessage());
            p.sendMessage("§cAn error occurred while saving the kit.");
        }
        return true;
    }

    private static Map<String, Object> createEnhancedItemConfig(ItemStack item) {
        Map<String, Object> config = new java.util.HashMap<>();
        
        // Basic properties
        config.put("material", item.getType().name());
        config.put("amount", item.getAmount());
        
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Custom name
            if (meta.hasDisplayName()) {
                config.put("name", meta.getDisplayName());
            }
            
            // Custom lore
            if (meta.hasLore()) {
                config.put("lore", meta.getLore());
            }
            
            // Durability/damage
            if (item.getDurability() > 0) {
                config.put("durability", item.getDurability());
            }
            
            // Enchantments
            if (meta.hasEnchants()) {
                Map<String, Integer> enchants = new java.util.HashMap<>();
                for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                    enchants.put(entry.getKey().getName(), entry.getValue());
                }
                config.put("enchantments", enchants);
            }
            
            // Handle specific item types
            if (item.getType().name().contains("POTION") || item.getType().name().contains("SPLASH") || item.getType().name().contains("LINGERING")) {
                addPotionMeta(config, meta);
            } else if (item.getType() == Material.ENCHANTED_BOOK) {
                addEnchantedBookMeta(config, meta);
            } else if (item.getType().name().contains("SKULL") || item.getType().name().contains("HEAD")) {
                addSkullMeta(config, meta);
            } else if (item.getType().name().contains("LEATHER_")) {
                addLeatherArmorMeta(config, meta);
            } else if (item.getType() == Material.WRITTEN_BOOK || item.getType() == Material.WRITABLE_BOOK) {
                addBookMeta(config, meta);
            } else if (item.getType() == Material.FIREWORK_ROCKET) {
                addFireworkMeta(config, meta);
            } else if (item.getType().name().contains("BANNER")) {
                addBannerMeta(config, meta);
            } else if (item.getType() == Material.CROSSBOW) {
                addCrossbowMeta(config, meta);
            }
        }
        
        return config;
    }

    private static void addPotionMeta(Map<String, Object> config, ItemMeta meta) {
        if (!(meta instanceof PotionMeta potionMeta)) return;
        
        // Potion type
        if (potionMeta.getBasePotionType() != null) {
            config.put("potionType", potionMeta.getBasePotionType().name());
        }
        
        // Custom effects
        if (!potionMeta.getCustomEffects().isEmpty()) {
            List<Map<String, Object>> effects = new ArrayList<>();
            for (PotionEffect effect : potionMeta.getCustomEffects()) {
                Map<String, Object> effectConfig = new java.util.HashMap<>();
                effectConfig.put("type", effect.getType().getName());
                effectConfig.put("duration", effect.getDuration());
                effectConfig.put("amplifier", effect.getAmplifier());
                effectConfig.put("ambient", effect.isAmbient());
                effectConfig.put("particles", effect.hasParticles());
                effects.add(effectConfig);
            }
            config.put("effects", effects);
        }
    }

    private static void addEnchantedBookMeta(Map<String, Object> config, ItemMeta meta) {
        if (!(meta instanceof EnchantmentStorageMeta bookMeta)) return;
        
        if (!bookMeta.getStoredEnchants().isEmpty()) {
            Map<String, Integer> storedEnchants = new java.util.HashMap<>();
            for (Map.Entry<Enchantment, Integer> entry : bookMeta.getStoredEnchants().entrySet()) {
                storedEnchants.put(entry.getKey().getName(), entry.getValue());
            }
            config.put("storedEnchantments", storedEnchants);
        }
    }

    private static void addSkullMeta(Map<String, Object> config, ItemMeta meta) {
        if (!(meta instanceof SkullMeta skullMeta)) return;
        
        if (skullMeta.getOwningPlayer() != null) {
            config.put("owner", skullMeta.getOwningPlayer().getName());
        }
    }

    private static void addLeatherArmorMeta(Map<String, Object> config, ItemMeta meta) {
        if (!(meta instanceof LeatherArmorMeta leatherMeta)) return;
        
        if (leatherMeta.getColor() != null) {
            Color color = leatherMeta.getColor();
            String hexColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
            config.put("color", hexColor);
        }
    }

    private static void addBookMeta(Map<String, Object> config, ItemMeta meta) {
        if (!(meta instanceof BookMeta bookMeta)) return;
        
        if (bookMeta.getTitle() != null) {
            config.put("title", bookMeta.getTitle());
        }
        
        if (bookMeta.getAuthor() != null) {
            config.put("author", bookMeta.getAuthor());
        }
        
        if (bookMeta.getPageCount() > 0) {
            config.put("pages", bookMeta.getPages());
        }
    }

    private static void addFireworkMeta(Map<String, Object> config, ItemMeta meta) {
        if (!(meta instanceof FireworkMeta fireworkMeta)) return;
        
        config.put("power", fireworkMeta.getPower());
        
        if (!fireworkMeta.getEffects().isEmpty()) {
            List<Map<String, Object>> effects = new ArrayList<>();
            for (FireworkEffect effect : fireworkMeta.getEffects()) {
                Map<String, Object> effectConfig = new java.util.HashMap<>();
                effectConfig.put("type", effect.getType().name());
                
                if (!effect.getColors().isEmpty()) {
                    List<String> colors = new ArrayList<>();
                    for (Color color : effect.getColors()) {
                        colors.add(String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
                    }
                    effectConfig.put("colors", colors);
                }
                
                if (!effect.getFadeColors().isEmpty()) {
                    List<String> fadeColors = new ArrayList<>();
                    for (Color color : effect.getFadeColors()) {
                        fadeColors.add(String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
                    }
                    effectConfig.put("fadeColors", fadeColors);
                }
                
                effectConfig.put("flicker", effect.hasFlicker());
                effectConfig.put("trail", effect.hasTrail());
                
                effects.add(effectConfig);
            }
            config.put("effects", effects);
        }
    }

    private static void addBannerMeta(Map<String, Object> config, ItemMeta meta) {
        if (!(meta instanceof BannerMeta bannerMeta)) return;
        
        if (!bannerMeta.getPatterns().isEmpty()) {
            List<Map<String, Object>> patterns = new ArrayList<>();
            for (Pattern pattern : bannerMeta.getPatterns()) {
                Map<String, Object> patternConfig = new java.util.HashMap<>();
                patternConfig.put("type", pattern.getPattern().name());
                patternConfig.put("color", pattern.getColor().name());
                patterns.add(patternConfig);
            }
            config.put("patterns", patterns);
        }
    }

    private static void addCrossbowMeta(Map<String, Object> config, ItemMeta meta) {
        if (!(meta instanceof CrossbowMeta crossbowMeta)) return;
        
        if (!crossbowMeta.getChargedProjectiles().isEmpty()) {
            List<Map<String, Object>> projectiles = new ArrayList<>();
            for (ItemStack projectile : crossbowMeta.getChargedProjectiles()) {
                projectiles.add(createEnhancedItemConfig(projectile));
            }
            config.put("chargedProjectiles", projectiles);
        }
    }
}
