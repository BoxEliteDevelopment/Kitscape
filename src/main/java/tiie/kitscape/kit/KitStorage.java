package tiie.kitscape.kit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import tiie.kitscape.config.ConfigManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KitStorage {

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final Map<String,Kit> kits = new ConcurrentHashMap<>();

    public KitStorage(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        config.loadKitsAsync().thenRun(this::parseKits);
    }

    private void parseKits() {
        kits.clear();
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(config.getKitsFile());
        ConfigurationSection sec = cfg.getConfigurationSection("kits");
        if (sec == null) {
            plugin.getLogger().warning("No 'kits' section in kits.yml");
            return;
        }

        for (String key : sec.getKeys(false)) {
            String name = key.toLowerCase();
            ConfigurationSection k = sec.getConfigurationSection(key);
            if (k == null) continue;

            // 1. displayName
            String display = ChatColor.translateAlternateColorCodes(
                    '&', k.getString("displayName", name));

            // 2. items[] - Enhanced parsing with metadata support
            List<ItemStack> itemList = new ArrayList<>();
            List<Map<?, ?>> itemConfigs = k.getMapList("items");
            
            if (itemConfigs.isEmpty()) {
                // Fallback to old string format for backward compatibility
                for (String s : k.getStringList("items")) {
                    ItemStack item = parseLegacyItemString(s, name);
                    if (item != null) {
                        itemList.add(item);
                    }
                }
            } else {
                // New enhanced format
                for (Map<?, ?> itemConfig : itemConfigs) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> config = (Map<String, Object>) itemConfig;
                    ItemStack item = parseEnhancedItem(config, name);
                    if (item != null) {
                        itemList.add(item);
                    }
                }
            }
            
            ItemStack[] items = itemList.toArray(new ItemStack[0]);
            Map<String, Long> cd = new HashMap<>();
            ConfigurationSection cdSec = k.getConfigurationSection("cooldown");
            if (cdSec != null) {
                for (String unit : cdSec.getKeys(false)) {
                    cd.put(unit, cdSec.getLong(unit, 0L));
                }
            }

            int slot = k.getInt("slot", 0);
            String hex = k.getString("hexColor", "#FFFFFF");

            ItemStack icon;
            ConfigurationSection iSec = k.getConfigurationSection("icon");
            if (iSec != null) {
                Material mat = Material.matchMaterial(iSec.getString("material", "CHEST"));
                int amt = iSec.getInt("amount", 1);
                icon = new ItemStack(mat != null ? mat : Material.CHEST, amt);

                ItemMeta meta = icon.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes(
                        '&', iSec.getString("name", display)));
                meta.setLore(iSec.getStringList("lore").stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .toList());
                icon.setItemMeta(meta);
            } else {
                icon = new ItemStack(Material.CHEST);
                ItemMeta im = icon.getItemMeta();
                im.setDisplayName(ChatColor.translateAlternateColorCodes('&', display));
                icon.setItemMeta(im);
            }

            kits.put(name, new Kit(
                    name,
                    display,
                    items,
                    cd,
                    slot,
                    hex,
                    icon
            ));

            plugin.getLogger().info("Loaded kit '" + name + "' in slot " + slot);
        }
    }

    private ItemStack parseLegacyItemString(String itemString, String kitName) {
        try {
            String[] parts = itemString.split(":");
            String matName = parts[0].trim().toUpperCase();
            int amt = (parts.length > 1) ? Integer.parseInt(parts[1].trim()) : 1;

            Material mat = Material.matchMaterial(matName);
            if (mat == null || !mat.isItem()) {
                plugin.getLogger().warning("[Kitscape] Kit '" + kitName + "': skipping non-item '" + matName + "'");
                return null;
            }
            int clamped = Math.max(1, Math.min(amt, mat.getMaxStackSize()));
            return new ItemStack(mat, clamped);
        } catch (Exception ex) {
            plugin.getLogger().warning("[Kitscape] Kit '" + kitName + "': bad item entry '" + itemString + "' (" + ex.getMessage() + ")");
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private ItemStack parseEnhancedItem(Map<String, Object> itemConfig, String kitName) {
        try {
            // Basic item properties
            String materialName = (String) itemConfig.get("material");
            if (materialName == null) {
                plugin.getLogger().warning("[Kitscape] Kit '" + kitName + "': missing material property");
                return null;
            }

            Material material = Material.matchMaterial(materialName.toUpperCase());
            if (material == null || !material.isItem()) {
                plugin.getLogger().warning("[Kitscape] Kit '" + kitName + "': invalid material '" + materialName + "'");
                return null;
            }

            int amount = (int) itemConfig.getOrDefault("amount", 1);
            int clamped = Math.max(1, Math.min(amount, material.getMaxStackSize()));
            
            ItemStack item = new ItemStack(material, clamped);
            ItemMeta meta = item.getItemMeta();
            
            // Custom name
            if (itemConfig.containsKey("name")) {
                String name = ChatColor.translateAlternateColorCodes('&', (String) itemConfig.get("name"));
                meta.setDisplayName(name);
            }
            
            // Custom lore
            if (itemConfig.containsKey("lore")) {
                List<String> lore = (List<String>) itemConfig.get("lore");
                List<String> coloredLore = lore.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .toList();
                meta.setLore(coloredLore);
            }
            
            // Durability/damage
            if (itemConfig.containsKey("durability")) {
                short durability = ((Number) itemConfig.get("durability")).shortValue();
                item.setDurability(durability);
            }
            
            // Enchantments
            if (itemConfig.containsKey("enchantments")) {
                Map<String, Object> enchants = (Map<String, Object>) itemConfig.get("enchantments");
                for (Map.Entry<String, Object> entry : enchants.entrySet()) {
                    Enchantment enchant = Enchantment.getByName(entry.getKey().toUpperCase());
                    if (enchant != null) {
                        int level = ((Number) entry.getValue()).intValue();
                        meta.addEnchant(enchant, level, true);
                    }
                }
            }
            
            // Handle specific item types
            if (material.name().contains("POTION") || material.name().contains("SPLASH") || material.name().contains("LINGERING")) {
                meta = applyPotionMeta(meta, itemConfig);
            } else if (material == Material.ENCHANTED_BOOK) {
                meta = applyEnchantedBookMeta(meta, itemConfig);
            } else if (material.name().contains("SKULL") || material.name().contains("HEAD")) {
                meta = applySkullMeta(meta, itemConfig);
            } else if (material.name().contains("LEATHER_")) {
                meta = applyLeatherArmorMeta(meta, itemConfig);
            } else if (material == Material.WRITTEN_BOOK || material == Material.WRITABLE_BOOK) {
                meta = applyBookMeta(meta, itemConfig);
            } else if (material == Material.FIREWORK_ROCKET) {
                meta = applyFireworkMeta(meta, itemConfig);
            } else if (material.name().contains("BANNER")) {
                meta = applyBannerMeta(meta, itemConfig);
            } else if (material == Material.CROSSBOW) {
                meta = applyCrossbowMeta(meta, itemConfig);
            }
            
            item.setItemMeta(meta);
            return item;
            
        } catch (Exception ex) {
            plugin.getLogger().warning("[Kitscape] Kit '" + kitName + "': error parsing item config: " + ex.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private ItemMeta applyPotionMeta(ItemMeta meta, Map<String, Object> config) {
        if (!(meta instanceof PotionMeta potionMeta)) return meta;
        
        // Potion type
        if (config.containsKey("potionType")) {
            String potionTypeName = (String) config.get("potionType");
            PotionType potionType = PotionType.valueOf(potionTypeName.toUpperCase());
            potionMeta.setBasePotionType(potionType);
        }
        
        // Custom effects
        if (config.containsKey("effects")) {
            List<Map<String, Object>> effects = (List<Map<String, Object>>) config.get("effects");
            for (Map<String, Object> effectConfig : effects) {
                String effectTypeName = (String) effectConfig.get("type");
                PotionEffectType effectType = PotionEffectType.getByName(effectTypeName.toUpperCase());
                if (effectType != null) {
                    int duration = (int) effectConfig.getOrDefault("duration", 200);
                    int amplifier = (int) effectConfig.getOrDefault("amplifier", 0);
                    boolean ambient = (boolean) effectConfig.getOrDefault("ambient", false);
                    boolean particles = (boolean) effectConfig.getOrDefault("particles", true);
                    
                    PotionEffect effect = new PotionEffect(effectType, duration, amplifier, ambient, particles);
                    potionMeta.addCustomEffect(effect, true);
                }
            }
        }
        
        return potionMeta;
    }

    @SuppressWarnings("unchecked")
    private ItemMeta applyEnchantedBookMeta(ItemMeta meta, Map<String, Object> config) {
        if (!(meta instanceof EnchantmentStorageMeta bookMeta)) return meta;
        
        if (config.containsKey("storedEnchantments")) {
            Map<String, Object> enchants = (Map<String, Object>) config.get("storedEnchantments");
            for (Map.Entry<String, Object> entry : enchants.entrySet()) {
                Enchantment enchant = Enchantment.getByName(entry.getKey().toUpperCase());
                if (enchant != null) {
                    int level = ((Number) entry.getValue()).intValue();
                    bookMeta.addStoredEnchant(enchant, level, true);
                }
            }
        }
        
        return bookMeta;
    }

    private ItemMeta applySkullMeta(ItemMeta meta, Map<String, Object> config) {
        if (!(meta instanceof SkullMeta skullMeta)) return meta;
        
        if (config.containsKey("owner")) {
            String owner = (String) config.get("owner");
            skullMeta.setOwningPlayer(plugin.getServer().getOfflinePlayer(owner));
        }
        
        return skullMeta;
    }

    private ItemMeta applyLeatherArmorMeta(ItemMeta meta, Map<String, Object> config) {
        if (!(meta instanceof LeatherArmorMeta leatherMeta)) return meta;
        
        if (config.containsKey("color")) {
            String colorHex = (String) config.get("color");
            if (colorHex.startsWith("#")) {
                colorHex = colorHex.substring(1);
            }
            try {
                int rgb = Integer.parseInt(colorHex, 16);
                Color color = Color.fromRGB(rgb);
                leatherMeta.setColor(color);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid color format: " + colorHex);
            }
        }
        
        return leatherMeta;
    }

    @SuppressWarnings("unchecked")
    private ItemMeta applyBookMeta(ItemMeta meta, Map<String, Object> config) {
        if (!(meta instanceof BookMeta bookMeta)) return meta;
        
        if (config.containsKey("title")) {
            bookMeta.setTitle(ChatColor.translateAlternateColorCodes('&', (String) config.get("title")));
        }
        
        if (config.containsKey("author")) {
            bookMeta.setAuthor((String) config.get("author"));
        }
        
        if (config.containsKey("pages")) {
            List<String> pages = (List<String>) config.get("pages");
            List<String> coloredPages = pages.stream()
                .map(page -> ChatColor.translateAlternateColorCodes('&', page))
                .toList();
            bookMeta.setPages(coloredPages);
        }
        
        return bookMeta;
    }

    @SuppressWarnings("unchecked")
    private ItemMeta applyFireworkMeta(ItemMeta meta, Map<String, Object> config) {
        if (!(meta instanceof FireworkMeta fireworkMeta)) return meta;
        
        if (config.containsKey("power")) {
            int power = (int) config.get("power");
            fireworkMeta.setPower(power);
        }
        
        if (config.containsKey("effects")) {
            List<Map<String, Object>> effects = (List<Map<String, Object>>) config.get("effects");
            for (Map<String, Object> effectConfig : effects) {
                FireworkEffect.Builder builder = FireworkEffect.builder();
                
                if (effectConfig.containsKey("type")) {
                    String typeName = (String) effectConfig.get("type");
                    Type type = Type.valueOf(typeName.toUpperCase());
                    builder.with(type);
                }
                
                if (effectConfig.containsKey("colors")) {
                    List<String> colors = (List<String>) effectConfig.get("colors");
                    List<Color> colorList = new ArrayList<>();
                    for (String colorHex : colors) {
                        if (colorHex.startsWith("#")) {
                            colorHex = colorHex.substring(1);
                        }
                        try {
                            int rgb = Integer.parseInt(colorHex, 16);
                            colorList.add(Color.fromRGB(rgb));
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("Invalid color format: " + colorHex);
                        }
                    }
                    builder.withColor(colorList);
                }
                
                if (effectConfig.containsKey("fadeColors")) {
                    List<String> fadeColors = (List<String>) effectConfig.get("fadeColors");
                    List<Color> fadeColorList = new ArrayList<>();
                    for (String colorHex : fadeColors) {
                        if (colorHex.startsWith("#")) {
                            colorHex = colorHex.substring(1);
                        }
                        try {
                            int rgb = Integer.parseInt(colorHex, 16);
                            fadeColorList.add(Color.fromRGB(rgb));
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("Invalid color format: " + colorHex);
                        }
                    }
                    builder.withFade(fadeColorList);
                }
                
                if (effectConfig.containsKey("flicker")) {
                    builder.flicker((boolean) effectConfig.get("flicker"));
                }
                
                if (effectConfig.containsKey("trail")) {
                    builder.trail((boolean) effectConfig.get("trail"));
                }
                
                fireworkMeta.addEffect(builder.build());
            }
        }
        
        return fireworkMeta;
    }

    @SuppressWarnings("unchecked")
    private ItemMeta applyBannerMeta(ItemMeta meta, Map<String, Object> config) {
        if (!(meta instanceof BannerMeta bannerMeta)) return meta;
        
        if (config.containsKey("patterns")) {
            List<Map<String, Object>> patterns = (List<Map<String, Object>>) config.get("patterns");
            List<Pattern> patternList = new ArrayList<>();
            
            for (Map<String, Object> patternConfig : patterns) {
                String patternTypeName = (String) patternConfig.get("type");
                String colorName = (String) patternConfig.get("color");
                
                PatternType patternType = PatternType.valueOf(patternTypeName.toUpperCase());
                DyeColor color = DyeColor.valueOf(colorName.toUpperCase());
                
                patternList.add(new Pattern(color, patternType));
            }
            
            bannerMeta.setPatterns(patternList);
        }
        
        return bannerMeta;
    }

    @SuppressWarnings("unchecked")
    private ItemMeta applyCrossbowMeta(ItemMeta meta, Map<String, Object> config) {
        if (!(meta instanceof CrossbowMeta crossbowMeta)) return meta;
        
        if (config.containsKey("chargedProjectiles")) {
            List<Map<String, Object>> projectiles = (List<Map<String, Object>>) config.get("chargedProjectiles");
            for (Map<String, Object> projectileConfig : projectiles) {
                ItemStack projectile = parseEnhancedItem(projectileConfig, "crossbow");
                if (projectile != null) {
                    crossbowMeta.addChargedProjectile(projectile);
                }
            }
        }
        
        return crossbowMeta;
    }

    public Kit getBySlot(int slot) {
        return kits.values().stream()
                .filter(k -> k.getSlot() == slot)
                .findFirst().orElse(null);
    }

    public Kit getKit(String name) { return kits.get(name.toLowerCase()); }
    public Map<String,Kit> getAll()  { return kits; }

    public void reload() {
        kits.clear();
        parseKits();
    }
}

