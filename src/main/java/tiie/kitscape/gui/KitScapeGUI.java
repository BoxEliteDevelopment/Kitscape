package tiie.kitscape.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import tiie.kitscape.KitScape;
import tiie.kitscape.cooldown.CooldownManager;
import tiie.kitscape.kit.Kit;
import tiie.kitscape.kit.KitStorage;
import tiie.kitscape.session.SessionManager;
import tiie.kitscape.utils.ParticleEffects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KitScapeGUI implements Listener {
    private final KitScape plugin;
    private final SessionManager sessions;
    private final CooldownManager cooldowns;

    private final String msgClaim, msgPreview, msgCooldown, msgNoPerm;
    private final String loreClaim, lorePreview, guiCooldownLine;

    private final boolean lockedShow;
    private final String lockedMatName;
    private final String lockedName;
    private final List<String> lockedLore;

    private final String guiTitle;
    public KitScapeGUI(KitScape plugin,
                       KitStorage kits,
                       SessionManager sessions,
                       CooldownManager cooldowns) {
        this.plugin    = plugin;
        this.sessions  = sessions;
        this.cooldowns = cooldowns;

        var cfg = plugin.getConfig();
        guiTitle        = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("gui.title", "&4Kitscape"));
        loreClaim       = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("lore.claimHint"));
        lorePreview     = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("lore.previewHint"));
        guiCooldownLine = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("gui.cooldownLine"));

        msgClaim        = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("messages.claim"));
        msgPreview      = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("messages.preview"));
        msgCooldown     = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("messages.cooldown"));
        msgNoPerm       = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("messages.no_permission", "&cYou need &f%perm% &cto use this kit."));

        lockedShow      = cfg.getBoolean("gui.locked.show", true);
        lockedMatName   = cfg.getString("gui.locked.item.material", "GRAY_STAINED_GLASS_PANE");
        lockedName      = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("gui.locked.item.name", "&c&lLocked"));
        lockedLore      = cfg.getStringList("gui.locked.item.lore").stream()
                .map(s -> ChatColor.translateAlternateColorCodes('&', s)).toList();
    }



    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, guiTitle);

        for (Kit kit : plugin.getKitStorage().getAll().values()) {
            String perm    = "kitscape.kit." + kit.getName();
            boolean canUse = p.hasPermission(perm) || p.hasPermission("kitscape.admin");

            ItemStack icon;
            ItemMeta meta;

            if (canUse) {
                icon = kit.getIcon().clone();
                meta = icon.getItemMeta();

                List<String> lore = (meta != null && meta.hasLore() && meta.getLore() != null)
                        ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

                if (loreClaim != null)   lore.add(loreClaim);
                if (lorePreview != null) lore.add(lorePreview);

                long left = cooldowns.timeLeft(p.getUniqueId(), kit.getName());
                if (left > 0) {
                    if (guiCooldownLine != null)
                        lore.add(guiCooldownLine.replace("%time%", formatRemaining(left)));
                    if (meta != null) meta.setDisplayName(ChatColor.GRAY + meta.getDisplayName());
                }

                if (meta != null) {
                    meta.setLore(lore);
                    icon.setItemMeta(meta);
                }
            } else {
                if (!lockedShow) continue; // hide entirely if configured

                Material mat = Material.matchMaterial(lockedMatName);
                if (mat == null || !mat.isItem()) mat = Material.GRAY_STAINED_GLASS_PANE;
                icon = new ItemStack(mat);
                meta = icon.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(lockedName);
                    List<String> lore = new ArrayList<>();
                    for (String line : lockedLore) lore.add(line.replace("%perm%", perm));
                    meta.setLore(lore);
                    icon.setItemMeta(meta);
                }
            }

            int slot = kit.getSlot();
            if (slot >= 0 && slot < inv.getSize()) inv.setItem(slot, icon);
        }

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        Player p     = (Player)e.getWhoClicked();
        int raw      = e.getRawSlot();
        Inventory top= e.getView().getTopInventory();

        if (title.equals(guiTitle)) {
            e.setCancelled(true);
            if (raw < 0 || raw >= top.getSize()) return;

            Kit kit = plugin.getKitStorage().getBySlot(raw);
            if (kit == null) return;

            String perm = "kitscape.kit." + kit.getName();
            boolean admin = p.hasPermission("kitscape.admin");
            if (!p.hasPermission(perm) && !admin) {
                p.sendMessage(msgNoPerm.replace("%perm%", perm));
                return;
            }

            if (e.getClick() == ClickType.LEFT) {
                boolean bypassCd = admin || p.hasPermission("kitscape.bypass.cooldown");
                long left = bypassCd ? 0L : cooldowns.timeLeft(p.getUniqueId(), kit.getName());
                if (left > 0) {
                    p.sendMessage(msgCooldown
                            .replace("%time%", formatRemaining(left))
                            .replace("%kit%", kit.getDisplayName()));
                    return;
                }

                int missing = additionalSlotsNeeded(p, kit.getItems());
                if (missing > 0) {
                    String msg = ChatColor.translateAlternateColorCodes('&',
                                    plugin.getConfig().getString("messages.not_enough_space",
                                            "&cPlease clear &f%slots% &cmore slots to claim %kit%."))
                            .replace("%slots%", String.valueOf(missing))
                            .replace("%kit%", kit.getDisplayName());
                    p.sendMessage(msg);
                    return;
                }

                p.closeInventory();

                for (ItemStack is : kit.getItems()) p.getInventory().addItem(is);

                // Get particle configuration from the kit
                ConfigurationSection particleConfig = null;
                try {
                    var cfg = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(plugin.getConfigManager().getKitsFile());
                    var kitsSec = cfg.getConfigurationSection("kits");
                    if (kitsSec != null) {
                        var kitSec = kitsSec.getConfigurationSection(kit.getName());
                        if (kitSec != null) {
                            particleConfig = kitSec.getConfigurationSection("particles");
                        }
                    }
                } catch (Exception ex) {
                    // Use default effects if config loading fails
                }

                // Play custom particle effects
                ParticleEffects.playKitClaimEffects(p, particleConfig);

                if (!bypassCd) {
                    cooldowns.setCooldown(p.getUniqueId(), kit.getName(), computeMillis(kit.getCooldowns()));
                    cooldowns.saveAll();
                }
                return;
            }

            if (e.getClick() == ClickType.RIGHT) {
                p.sendMessage(msgPreview.replace("%kit%", kit.getDisplayName()));
                openPreview(p, kit);
            }
            return;
        }

        if (title.startsWith("Preview: ")) {
            e.setCancelled(true);
            if (raw == 49) plugin.getGui().open(p);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        String title = e.getView().getTitle();
        if (title.equals(guiTitle) || title.startsWith("Preview: ")) e.setCancelled(true);
    }

    private void openPreview(Player p, Kit kit) {
        Inventory prev = Bukkit.createInventory(null, 54, "Preview: " + kit.getDisplayName());
        ItemStack[] its = kit.getItems();
        for (int i = 0; i < its.length && i < prev.getSize(); i++) prev.setItem(i, its[i]);
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta m     = back.getItemMeta();
        m.setDisplayName(ChatColor.RED + "Back");
        back.setItemMeta(m);
        prev.setItem(49, back);
        p.openInventory(prev);
    }

    private long computeMillis(Map<String,Long> cd) {
        long total = 0;
        for (var e : cd.entrySet()) {
            switch (e.getKey()) {
                case "seconds":     total += e.getValue() * 1000; break;
                case "minutes":     total += e.getValue() * 60 * 1000; break;
                case "hours":       total += e.getValue() * 3600 * 1000; break;
                case "days":        total += e.getValue() * 86400 * 1000; break;
                case "months":      total += e.getValue() * 30 * 86400 * 1000; break;
                case "years":       total += e.getValue() * 365 * 86400 * 1000; break;
                case "oneTimeUse":  if (e.getValue() > 0) return Long.MAX_VALUE;
            }
        }
        return total;
    }

    /** Format ms into "[Hh ][Mm ]Ss" */
    private String formatRemaining(long ms) {
        long totalSeconds = ms / 1000;
        long hours   = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0)   sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString();
    }

    private int additionalSlotsNeeded(Player p, ItemStack[] items) {
        if (items == null || items.length == 0) return 0;

        ItemStack[] storage = p.getInventory().getStorageContents(); // 36 slots
        int free = 0;
        List<ItemStack> partials = new ArrayList<>();
        for (ItemStack s : storage) {
            if (s == null || s.getType().isAir()) { free++; continue; }
            if (s.getAmount() < s.getMaxStackSize()) partials.add(s.clone()); // simulate fill
        }

        int needed = 0;
        for (ItemStack src : items) {
            if (src == null || src.getType().isAir()) continue;
            int remaining = src.getAmount();

            // fill partial stacks first
            for (ItemStack ps : partials) {
                if (remaining <= 0) break;
                if (ps.isSimilar(src)) {
                    int can = ps.getMaxStackSize() - ps.getAmount();
                    if (can > 0) {
                        int used = Math.min(can, remaining);
                        ps.setAmount(ps.getAmount() + used); // simulate
                        remaining -= used;
                    }
                }
            }
            if (remaining > 0) {
                int stacks = (int)Math.ceil(remaining / (double)src.getMaxStackSize());
                needed += stacks;
            }
        }
        int extra = needed - free;
        return Math.max(0, extra);
    }
}
