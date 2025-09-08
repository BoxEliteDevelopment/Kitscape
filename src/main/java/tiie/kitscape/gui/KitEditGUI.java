package tiie.kitscape.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.Particle;


import tiie.kitscape.KitScape;
import tiie.kitscape.kit.Kit;
import tiie.kitscape.kit.KitStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KitEditGUI implements Listener {
    
    private final KitScape plugin;
    private final Map<UUID, EditSession> editSessions = new HashMap<>();
    
    // GUI Titles
    private static final String MAIN_GUI_TITLE = ChatColor.DARK_PURPLE + "KitScape Editor";
    private static final String KIT_EDIT_TITLE = ChatColor.GOLD + "Kit Editor";
    private static final String DELETE_CONFIRM_TITLE = ChatColor.RED + "Confirm Deletion";
    private static final String COOLDOWN_EDIT_TITLE = ChatColor.BLUE + "Cooldown Editor";
    private static final String PARTICLE_EDIT_TITLE = ChatColor.LIGHT_PURPLE + "Particle Editor";
    private static final String DISPLAY_NAME_EDIT_TITLE = ChatColor.GREEN + "Display Name Editor";
    private static final String ITEM_SELECTOR_TITLE = ChatColor.AQUA + "Item Selector";
    private static final String SEARCH_TITLE = ChatColor.YELLOW + "Search Items";
    private static final String LORE_EDIT_TITLE = ChatColor.GOLD + "Lore Editor";
    
    public KitEditGUI(KitScape plugin) {
        this.plugin = plugin;
    }
    
    public void openMainGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MAIN_GUI_TITLE);
        
        // Add all kits to the GUI in their configured slots
        for (Kit kit : plugin.getKitStorage().getAll().values()) {
            // Get fresh kit data from storage
            Kit freshKit = plugin.getKitStorage().getKit(kit.getName());
            if (freshKit != null) {
                ItemStack kitItem = createKitDisplayItem(freshKit);
                // Use the kit's configured slot instead of sequential assignment
                inv.setItem(freshKit.getSlot(), kitItem);
            }
        }
        
        // Add navigation buttons
        ItemStack closeButton = createButton(Material.BARRIER, ChatColor.RED + "Close", 
            List.of(ChatColor.GRAY + "Click to close the editor"));
        inv.setItem(49, closeButton);
        
        player.openInventory(inv);
    }
    
    private static ItemStack createKitDisplayItem(Kit kit) {
        ItemStack item = kit.getIcon().clone();
        ItemMeta meta = item.getItemMeta();
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Display Name: " + ChatColor.WHITE + kit.getDisplayName());
        lore.add(ChatColor.YELLOW + "Slot: " + ChatColor.WHITE + kit.getSlot());
        lore.add(ChatColor.YELLOW + "Items: " + ChatColor.WHITE + kit.getItems().length);
        
        // Add cooldown info
        Map<String, Long> cooldowns = kit.getCooldowns();
        if (!cooldowns.isEmpty()) {
            lore.add(ChatColor.YELLOW + "Cooldown:");
            for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
                lore.add(ChatColor.GRAY + "  " + entry.getKey() + ": " + entry.getValue());
            }
        }
        
        lore.add("");
        lore.add(ChatColor.GREEN + "Left Click: " + ChatColor.WHITE + "Edit Kit");
        lore.add(ChatColor.RED + "Right Click: " + ChatColor.WHITE + "Delete Kit");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private static ItemStack createButton(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    public void openKitEditGUI(Player player, Kit kit) {
        Inventory inv = Bukkit.createInventory(null, 54, KIT_EDIT_TITLE + " - " + kit.getName());
        
        // Edit options
        inv.setItem(10, createEditOption(Material.NAME_TAG, ChatColor.GREEN + "Display Name", 
            List.of(ChatColor.GRAY + "Current: " + kit.getDisplayName(),
                    ChatColor.YELLOW + "Click to edit display name")));
        
        inv.setItem(12, createEditOption(Material.CLOCK, ChatColor.BLUE + "Cooldown", 
            List.of(ChatColor.GRAY + "Click to edit cooldown settings")));
        
        inv.setItem(14, createEditOption(Material.FIREWORK_ROCKET, ChatColor.LIGHT_PURPLE + "Particles", 
            List.of(ChatColor.GRAY + "Click to edit particle effects")));
        
        // Lore function disabled for now
        // inv.setItem(16, createEditOption(Material.BOOK, ChatColor.GOLD + "Lore", 
        //     List.of(ChatColor.GRAY + "Click to edit kit lore")));
        
        // Current kit stats display (bottom middle)
        inv.setItem(49, createKitStatsDisplay(kit));
        
        // Navigation buttons
        inv.setItem(45, createButton(Material.ARROW, ChatColor.YELLOW + "Back", 
            List.of(ChatColor.GRAY + "Return to kit list")));
        
        inv.setItem(53, createButton(Material.BARRIER, ChatColor.RED + "Close", 
            List.of(ChatColor.GRAY + "Close the editor")));
        
        // Store the edit session
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.KIT_EDIT));
        
        player.openInventory(inv);
    }
    
    private static ItemStack createEditOption(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private static ItemStack createKitStatsDisplay(Kit kit) {
        ItemStack item = kit.getIcon().clone();
        ItemMeta meta = item.getItemMeta();
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "=== Kit Statistics ===");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Name: " + ChatColor.WHITE + kit.getName());
        lore.add(ChatColor.YELLOW + "Display: " + ChatColor.WHITE + kit.getDisplayName());
        lore.add(ChatColor.YELLOW + "Slot: " + ChatColor.WHITE + kit.getSlot());
        lore.add(ChatColor.YELLOW + "Items: " + ChatColor.WHITE + kit.getItems().length);
        
        // Add cooldown info
        Map<String, Long> cooldowns = kit.getCooldowns();
        if (!cooldowns.isEmpty()) {
            lore.add(ChatColor.YELLOW + "Cooldown:");
            for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
                if (entry.getValue() > 0) {
                    lore.add(ChatColor.GRAY + "  " + entry.getKey() + ": " + entry.getValue());
                }
            }
        } else {
            lore.add(ChatColor.YELLOW + "Cooldown: " + ChatColor.GREEN + "None");
        }
        
        lore.add("");
        lore.add(ChatColor.GRAY + "This shows current kit stats");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    public void openCooldownEditGUI(Player player, Kit kit) {
        Inventory inv = Bukkit.createInventory(null, 54, COOLDOWN_EDIT_TITLE + " - " + kit.getName());
        
        Map<String, Long> cooldowns = kit.getCooldowns();
        
        // Time unit buttons
        inv.setItem(10, createTimeButton(Material.CLOCK, "Seconds", cooldowns.getOrDefault("seconds", 0L)));
        inv.setItem(12, createTimeButton(Material.CLOCK, "Minutes", cooldowns.getOrDefault("minutes", 0L)));
        inv.setItem(14, createTimeButton(Material.CLOCK, "Hours", cooldowns.getOrDefault("hours", 0L)));
        inv.setItem(16, createTimeButton(Material.CLOCK, "Days", cooldowns.getOrDefault("days", 0L)));
        inv.setItem(28, createTimeButton(Material.CLOCK, "Weeks", cooldowns.getOrDefault("weeks", 0L)));
        inv.setItem(30, createTimeButton(Material.CLOCK, "Months", cooldowns.getOrDefault("months", 0L)));
        inv.setItem(32, createTimeButton(Material.CLOCK, "Years", cooldowns.getOrDefault("years", 0L)));
        
        // Special options
        inv.setItem(34, createSpecialOption(Material.REDSTONE, "One Time Use", 
            cooldowns.containsKey("oneTimeUse") && cooldowns.get("oneTimeUse") > 0));
        
        // Info paper showing total cooldown time
        inv.setItem(22, createCooldownInfoPaper(cooldowns));
        
        // Navigation
        inv.setItem(45, createButton(Material.ARROW, ChatColor.YELLOW + "Back", 
            List.of(ChatColor.GRAY + "Return to kit editor")));
        
        inv.setItem(49, createButton(Material.BARRIER, ChatColor.RED + "Close", 
            List.of(ChatColor.GRAY + "Close the editor")));
        
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.COOLDOWN_EDIT));
        player.openInventory(inv);
    }
    
    private static ItemStack createTimeButton(Material material, String unit, Long value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + unit);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Current: " + ChatColor.WHITE + value);
        lore.add("");
        lore.add(ChatColor.GREEN + "Right Click: " + ChatColor.WHITE + "Add 1");
        lore.add(ChatColor.BLUE + "Shift + Right Click: " + ChatColor.WHITE + "Add 5");
        lore.add(ChatColor.RED + "Left Click: " + ChatColor.WHITE + "Remove 1");
        lore.add(ChatColor.DARK_RED + "Shift + Left Click: " + ChatColor.WHITE + "Remove 5");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private static ItemStack createSpecialOption(Material material, String name, boolean enabled) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + name);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Status: " + (enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        lore.add("");
        lore.add(ChatColor.GREEN + "Click to toggle");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private static ItemStack createCooldownInfoPaper(Map<String, Long> cooldowns) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Cooldown Information");
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Current Cooldown Settings:");
        lore.add("");
        
        if (cooldowns.containsKey("oneTimeUse") && cooldowns.get("oneTimeUse") > 0) {
            lore.add(ChatColor.RED + "• One Time Use: " + ChatColor.WHITE + "Enabled");
        } else {
            long totalMillis = 0;
            boolean hasCooldown = false;
            
            for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
                if (entry.getValue() > 0) {
                    hasCooldown = true;
                    String unit = entry.getKey();
                    long value = entry.getValue();
                    
                    switch (unit) {
                        case "seconds" -> {
                            lore.add(ChatColor.GRAY + "• Seconds: " + ChatColor.WHITE + value);
                            totalMillis += value * 1000;
                        }
                        case "minutes" -> {
                            lore.add(ChatColor.GRAY + "• Minutes: " + ChatColor.WHITE + value);
                            totalMillis += value * 60 * 1000;
                        }
                        case "hours" -> {
                            lore.add(ChatColor.GRAY + "• Hours: " + ChatColor.WHITE + value);
                            totalMillis += value * 3600 * 1000;
                        }
                        case "days" -> {
                            lore.add(ChatColor.GRAY + "• Days: " + ChatColor.WHITE + value);
                            totalMillis += value * 86400 * 1000;
                        }
                        case "weeks" -> {
                            lore.add(ChatColor.GRAY + "• Weeks: " + ChatColor.WHITE + value);
                            totalMillis += value * 7 * 86400 * 1000;
                        }
                        case "months" -> {
                            lore.add(ChatColor.GRAY + "• Months: " + ChatColor.WHITE + value);
                            totalMillis += value * 30 * 86400 * 1000;
                        }
                        case "years" -> {
                            lore.add(ChatColor.GRAY + "• Years: " + ChatColor.WHITE + value);
                            totalMillis += value * 365 * 86400 * 1000;
                        }
                    }
                }
            }
            
            if (!hasCooldown) {
                lore.add(ChatColor.GRAY + "• No cooldown set");
            } else {
                lore.add("");
                lore.add(ChatColor.GREEN + "Total Time: " + ChatColor.WHITE + formatDuration(totalMillis));
            }
        }
        
        lore.add("");
        lore.add(ChatColor.YELLOW + "Use the clock buttons to adjust");
        lore.add(ChatColor.YELLOW + "cooldown values.");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private static String formatDuration(long milliseconds) {
        if (milliseconds == 0) return "No cooldown";
        
        long totalSeconds = milliseconds / 1000;
        long years = totalSeconds / (365 * 24 * 3600);
        long months = (totalSeconds % (365 * 24 * 3600)) / (30 * 24 * 3600);
        long days = (totalSeconds % (30 * 24 * 3600)) / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        StringBuilder result = new StringBuilder();
        if (years > 0) result.append(years).append("y ");
        if (months > 0) result.append(months).append("mo ");
        if (days > 0) result.append(days).append("d ");
        if (hours > 0) result.append(hours).append("h ");
        if (minutes > 0) result.append(minutes).append("m ");
        if (seconds > 0) result.append(seconds).append("s");
        
        return result.toString().trim();
    }
    
    public void openDeleteConfirmGUI(Player player, Kit kit) {
        Inventory inv = Bukkit.createInventory(null, 27, DELETE_CONFIRM_TITLE + " - " + kit.getName());
        
        // Warning message
        ItemStack warning = createButton(Material.RED_STAINED_GLASS_PANE, 
            ChatColor.RED + "⚠ WARNING ⚠", 
            List.of(ChatColor.RED + "You are about to delete kit:",
                    ChatColor.YELLOW + kit.getName(),
                    "",
                    ChatColor.RED + "This action cannot be undone!"));
        
        for (int i = 9; i < 18; i++) {
            inv.setItem(i, warning);
        }
        
        // Confirm/Deny buttons
        inv.setItem(11, createButton(Material.LIME_WOOL, ChatColor.GREEN + "Confirm Delete", 
            List.of(ChatColor.RED + "Click to permanently delete the kit")));
        
        inv.setItem(15, createButton(Material.RED_WOOL, ChatColor.RED + "Cancel", 
            List.of(ChatColor.GRAY + "Click to cancel deletion")));
        
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.DELETE_CONFIRM));
        player.openInventory(inv);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        int slot = event.getRawSlot();
        
        if (title.equals(MAIN_GUI_TITLE)) {
            event.setCancelled(true);
            handleMainGUIClick(player, slot, event.isRightClick());
        } else if (title.startsWith(KIT_EDIT_TITLE)) {
            event.setCancelled(true);
            handleKitEditGUIClick(player, slot);
        } else if (title.startsWith(COOLDOWN_EDIT_TITLE)) {
            event.setCancelled(true);
            handleCooldownEditGUIClick(player, slot, event.isRightClick(), event.isShiftClick());
        } else if (title.startsWith(DELETE_CONFIRM_TITLE)) {
            event.setCancelled(true);
            handleDeleteConfirmGUIClick(player, slot);
        } else if (title.startsWith(DISPLAY_NAME_EDIT_TITLE)) {
            event.setCancelled(true);
            handleDisplayNameEditGUIClick(player, slot);
        } else if (title.startsWith(ITEM_SELECTOR_TITLE)) {
            event.setCancelled(true);
            handleItemSelectorGUIClick(player, slot, event.isRightClick());
        } else if (title.startsWith(SEARCH_TITLE)) {
            event.setCancelled(true);
            handleSearchGUIClick(player, slot);
        } else if (title.startsWith(ChatColor.GOLD + "Edit Display Name")) {
            event.setCancelled(true);
            handleAnvilClick(player, slot, event);
        } else if (title.startsWith(ChatColor.GREEN + "Items") || title.startsWith(ChatColor.BLUE + "Blocks") || 
                   title.startsWith(ChatColor.RED + "Mobs") || title.startsWith(ChatColor.GOLD + "Food")) {
            event.setCancelled(true);
            handleCategorySearchGUIClick(player, slot);
        } else if (title.startsWith(LORE_EDIT_TITLE)) {
            event.setCancelled(true);
            handleLoreEditGUIClick(player, slot, event.isRightClick(), event.isShiftClick());
        } else if (title.startsWith(ChatColor.GOLD + "Slot Editor")) {
            event.setCancelled(true);
            handleSlotEditorGUIClick(player, slot);
        } else if (title.startsWith(PARTICLE_EDIT_TITLE)) {
            event.setCancelled(true);
            handleParticleEditGUIClick(player, slot, event.isRightClick(), event.isShiftClick());
        } else if (title.startsWith(ChatColor.LIGHT_PURPLE + "Particle List")) {
            event.setCancelled(true);
            handleParticleListGUIClick(player, slot);
        } else if (title.startsWith(ChatColor.LIGHT_PURPLE + "Configure")) {
            event.setCancelled(true);
            handleParticleConfigGUIClick(player, slot, event.isRightClick(), event.isShiftClick());
        } else if (title.startsWith(ChatColor.GOLD + "Current Particles")) {
            event.setCancelled(true);
            handleCurrentParticlesGUIClick(player, slot, event.isRightClick());
        } else if (title.startsWith(ChatColor.GOLD + "Edit") && title.contains("Settings")) {
            event.setCancelled(true);
            handleTextSettingsGUIClick(player, slot, event.isRightClick(), event.isShiftClick());
        } else {
            // Handle lore paper clicks in player inventory (legacy support)
            handleLorePaperClick(player, event);
        }
    }
    
    private void handleLorePaperClick(Player player, InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() != Material.PAPER) return;
        
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasLore()) return;
        
        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) return;
        
        // Check if this is a lore paper (has our specific lore)
        boolean isLorePaper = false;
        for (String line : lore) {
            if (line.contains("Left Click:") && line.contains("Delete")) {
                isLorePaper = true;
                break;
            }
        }
        
        if (!isLorePaper) return;
        
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null || session.getMode() != EditMode.LORE_EDIT) return;
        
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) return;
        
        String loreText = meta.getDisplayName();
        if (loreText.equals(ChatColor.GRAY + "Empty Line")) {
            loreText = "";
        }
        
        if (event.isLeftClick()) {
            // Delete the lore paper
            event.setCancelled(true);
            player.getInventory().removeItem(clickedItem);
            player.sendMessage(ChatColor.RED + "Lore paper deleted.");
        } else if (event.isRightClick()) {
            event.setCancelled(true);
            if (event.isShiftClick()) {
                // Set index
                openLoreIndexInput(player, kit, loreText);
            } else {
                // Set lore
                setKitLore(player, kit, loreText);
            }
        }
    }
    
    private void openLoreIndexInput(Player player, Kit kit, String loreText) {
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "Please type the index number (0-9) for this lore:");
        player.sendMessage(ChatColor.GRAY + "Current lore: " + loreText);
        player.sendMessage(ChatColor.RED + "Type 'cancel' to cancel");
        
        // Store the session for index editing
        Map<String, Object> data = new HashMap<>();
        data.put("editingLoreIndex", true);
        data.put("loreText", loreText);
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.LORE_EDIT, data));
    }
    
    private void setKitLore(Player player, Kit kit, String loreText) {
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection == null) return;
            
            // Get or create the icon section
            ConfigurationSection iconSection = kitSection.getConfigurationSection("icon");
            if (iconSection == null) {
                iconSection = kitSection.createSection("icon");
                iconSection.set("material", kit.getIcon().getType().name());
                iconSection.set("amount", 1);
                iconSection.set("name", kit.getDisplayName());
            }
            
            // Get current lore or create new list
            List<String> currentLore = iconSection.getStringList("lore");
            currentLore.add(loreText);
            iconSection.set("lore", currentLore);
            
            cfg.save(file);
            plugin.getKitStorage().reload();
            
            player.sendMessage(ChatColor.GREEN + "Added lore: " + loreText);
            
            // Remove the lore paper from session data
            EditSession session = editSessions.get(player.getUniqueId());
            if (session != null && session.getMode() == EditMode.LORE_EDIT) {
                Map<String, Object> data = session.getData();
                data.remove("lorePaper");
                data.remove("loreText");
                editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.LORE_EDIT, data));
            }
            
            // Reopen the GUI to refresh the display
            openLoreEditGUI(player, kit);
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error setting lore: " + e.getMessage());
        }
    }
    
    private void handleMainGUIClick(Player player, int slot, boolean isRightClick) {
        if (slot == 49) { // Close button
            player.closeInventory();
            return;
        }
        
        if (slot >= 45) return; // Navigation area
        
        // Find the kit at this slot
        Kit kit = getKitAtSlot(slot);
        if (kit == null) return;
        
        if (isRightClick) {
            openDeleteConfirmGUI(player, kit);
        } else {
            openKitEditGUI(player, kit);
        }
    }
    
    private void handleKitEditGUIClick(Player player, int slot) {
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null) return;
        
        // Refresh kit data from storage to get current values
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }
        
        switch (slot) {
            case 10: // Display Name
                openDisplayNameEditGUI(player, kit);
                break;
            case 12: // Cooldown
                openCooldownEditGUI(player, kit);
                break;
            case 14: // Particles
                openParticleEditGUI(player, kit);
                break;
            // Lore function disabled for now
            // case 16: // Lore
            //     openLoreEditGUI(player, kit);
            //     break;
            case 45: // Back
                openMainGUI(player);
                break;
            case 49: // Close
                player.closeInventory();
                break;
        }
    }
    
    private void handleCooldownEditGUIClick(Player player, int slot, boolean isRightClick, boolean isShiftClick) {
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null) return;
        
        // Refresh kit data from storage to get current values
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }
        
        String timeUnit = getTimeUnitFromSlot(slot);
        
        if (timeUnit != null) {
            updateCooldown(player, kit, timeUnit, isRightClick, isShiftClick);
        } else if (slot == 34) { // One Time Use
            toggleOneTimeUse(player, kit);
        } else if (slot == 45) { // Back
            openKitEditGUI(player, kit);
        } else if (slot == 49) { // Close
            player.closeInventory();
        }
    }
    
    private void handleDeleteConfirmGUIClick(Player player, int slot) {
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null) return;
        
        // Refresh kit data from storage to get current values
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }
        
        if (slot == 11) { // Confirm Delete
            deleteKit(player, kit);
        } else if (slot == 15) { // Cancel
            openMainGUI(player);
        }
    }
    
    private Kit getKitAtSlot(int slot) {
        // Find the kit that is configured to be at this specific slot
        for (Kit kit : plugin.getKitStorage().getAll().values()) {
            if (kit.getSlot() == slot) {
                return kit;
            }
        }
        return null;
    }
    
    private String getTimeUnitFromSlot(int slot) {
        switch (slot) {
            case 10: return "seconds";
            case 12: return "minutes";
            case 14: return "hours";
            case 16: return "days";
            case 28: return "weeks";
            case 30: return "months";
            case 32: return "years";
            default: return null;
        }
    }
    
    private void updateCooldown(Player player, Kit kit, String timeUnit, boolean isRightClick, boolean isShiftClick) {
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection == null) return;
            
            ConfigurationSection cooldownSection = kitSection.getConfigurationSection("cooldown");
            if (cooldownSection == null) {
                cooldownSection = kitSection.createSection("cooldown");
            }
            
            long currentValue = cooldownSection.getLong(timeUnit, 0);
            long change = isShiftClick ? 5 : 1;
            
            if (isRightClick) {
                currentValue += change;
            } else {
                currentValue = Math.max(0, currentValue - change);
            }
            
            cooldownSection.set(timeUnit, currentValue);
            cfg.save(file);
            
            // Reload storage
            plugin.getKitStorage().reload();
            
            player.sendMessage(ChatColor.GREEN + "Updated " + timeUnit + " to " + currentValue);
            
            // Read the updated cooldown data directly from the saved config
            YamlConfiguration updatedCfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection updatedKitSection = updatedCfg.getConfigurationSection("kits." + kit.getName());
            Map<String, Long> updatedCooldowns = new HashMap<>();
            
            if (updatedKitSection != null) {
                ConfigurationSection updatedCooldownSection = updatedKitSection.getConfigurationSection("cooldown");
                if (updatedCooldownSection != null) {
                    for (String key : updatedCooldownSection.getKeys(false)) {
                        updatedCooldowns.put(key, updatedCooldownSection.getLong(key));
                    }
                }
            }
            
            // Refresh the GUI with the updated cooldown data
            openCooldownEditGUIWithData(player, kit, updatedCooldowns);
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error updating cooldown: " + e.getMessage());
        }
    }
    
    private void toggleOneTimeUse(Player player, Kit kit) {
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection == null) return;
            
            ConfigurationSection cooldownSection = kitSection.getConfigurationSection("cooldown");
            if (cooldownSection == null) {
                cooldownSection = kitSection.createSection("cooldown");
            }
            
            boolean isOneTime = cooldownSection.getLong("oneTimeUse", 0) > 0;
            
            if (isOneTime) {
                cooldownSection.set("oneTimeUse", null);
                player.sendMessage(ChatColor.GREEN + "Disabled one-time use for " + kit.getName());
            } else {
                cooldownSection.set("oneTimeUse", 1);
                player.sendMessage(ChatColor.GREEN + "Enabled one-time use for " + kit.getName());
            }
            
            cfg.save(file);
            
            // Reload storage
            plugin.getKitStorage().reload();
            
            // Read the updated cooldown data directly from the saved config
            YamlConfiguration updatedCfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection updatedKitSection = updatedCfg.getConfigurationSection("kits." + kit.getName());
            Map<String, Long> updatedCooldowns = new HashMap<>();
            
            if (updatedKitSection != null) {
                ConfigurationSection updatedCooldownSection = updatedKitSection.getConfigurationSection("cooldown");
                if (updatedCooldownSection != null) {
                    for (String key : updatedCooldownSection.getKeys(false)) {
                        updatedCooldowns.put(key, updatedCooldownSection.getLong(key));
                    }
                }
            }
            
            // Refresh the GUI with the updated cooldown data
            openCooldownEditGUIWithData(player, kit, updatedCooldowns);
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error toggling one-time use: " + e.getMessage());
        }
    }
    
    private void deleteKit(Player player, Kit kit) {
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitsSection = cfg.getConfigurationSection("kits");
            
            if (kitsSection != null) {
                kitsSection.set(kit.getName(), null);
                cfg.save(file);
                plugin.getKitStorage().reload();
                
                player.sendMessage(ChatColor.GREEN + "Successfully deleted kit: " + kit.getName());
                openMainGUI(player);
            }
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error deleting kit: " + e.getMessage());
        }
    }
    
    private void openCooldownEditGUIWithData(Player player, Kit kit, Map<String, Long> cooldowns) {
        Inventory inv = Bukkit.createInventory(null, 54, COOLDOWN_EDIT_TITLE + " - " + kit.getName());
        
        // Time unit buttons
        inv.setItem(10, createTimeButton(Material.CLOCK, "Seconds", cooldowns.getOrDefault("seconds", 0L)));
        inv.setItem(12, createTimeButton(Material.CLOCK, "Minutes", cooldowns.getOrDefault("minutes", 0L)));
        inv.setItem(14, createTimeButton(Material.CLOCK, "Hours", cooldowns.getOrDefault("hours", 0L)));
        inv.setItem(16, createTimeButton(Material.CLOCK, "Days", cooldowns.getOrDefault("days", 0L)));
        inv.setItem(28, createTimeButton(Material.CLOCK, "Weeks", cooldowns.getOrDefault("weeks", 0L)));
        inv.setItem(30, createTimeButton(Material.CLOCK, "Months", cooldowns.getOrDefault("months", 0L)));
        inv.setItem(32, createTimeButton(Material.CLOCK, "Years", cooldowns.getOrDefault("years", 0L)));
        
        // Special options
        inv.setItem(34, createSpecialOption(Material.REDSTONE, "One Time Use", 
            cooldowns.containsKey("oneTimeUse") && cooldowns.get("oneTimeUse") > 0));
        
        // Info paper showing total cooldown time
        inv.setItem(22, createCooldownInfoPaper(cooldowns));
        
        // Navigation
        inv.setItem(45, createButton(Material.ARROW, ChatColor.YELLOW + "Back", 
            List.of(ChatColor.GRAY + "Return to kit editor")));
        
        inv.setItem(49, createButton(Material.BARRIER, ChatColor.RED + "Close", 
            List.of(ChatColor.GRAY + "Close the editor")));
        
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.COOLDOWN_EDIT));
        player.openInventory(inv);
    }
    
    public void openDisplayNameEditGUI(Player player, Kit kit) {
        Inventory inv = Bukkit.createInventory(null, 27, DISPLAY_NAME_EDIT_TITLE + " - " + kit.getName());
        
        // Current display name info
        ItemStack currentName = createButton(Material.NAME_TAG, ChatColor.GREEN + "Current Display Name", 
            List.of(ChatColor.GRAY + "Current: " + kit.getDisplayName(),
                    "",
                    ChatColor.YELLOW + "Click to edit with anvil"));
        inv.setItem(10, currentName);
        
        // Item selector
        ItemStack itemSelector = createButton(Material.GRASS_BLOCK, ChatColor.BLUE + "Select Item", 
            List.of(ChatColor.GRAY + "Browse all items and blocks",
                    ChatColor.YELLOW + "Click to open item selector"));
        inv.setItem(12, itemSelector);
        
        // Slot editor
        ItemStack slotEditor = createButton(Material.SUNFLOWER, ChatColor.GOLD + "Slot Editor", 
            List.of(ChatColor.GRAY + "Current slot: " + kit.getSlot(),
                    ChatColor.YELLOW + "Click to change kit position"));
        inv.setItem(14, slotEditor);
        
        // Navigation
        inv.setItem(22, createButton(Material.ARROW, ChatColor.YELLOW + "Back", 
            List.of(ChatColor.GRAY + "Return to kit editor")));
        
        inv.setItem(26, createButton(Material.BARRIER, ChatColor.RED + "Close", 
            List.of(ChatColor.GRAY + "Close the editor")));
        
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.DISPLAY_NAME_EDIT));
        player.openInventory(inv);
    }
    
    public void openItemSelectorGUI(Player player, Kit kit, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, ITEM_SELECTOR_TITLE + " - " + kit.getName() + " (Page " + (page + 1) + ")");
        
        // Get all materials
        Material[] materials = Material.values();
        int itemsPerPage = 45; // Leave space for navigation
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, materials.length);
        
        // Add materials to GUI
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Material material = materials[i];
            if (material.isItem()) {
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.WHITE + material.name());
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Click to select this item");
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inv.setItem(slot++, item);
            }
        }
        
        // Navigation buttons
        if (page > 0) {
            inv.setItem(45, createButton(Material.ARROW, ChatColor.YELLOW + "Previous Page", 
                List.of(ChatColor.GRAY + "Go to page " + page)));
        }
        
        if (endIndex < materials.length) {
            inv.setItem(53, createButton(Material.ARROW, ChatColor.YELLOW + "Next Page", 
                List.of(ChatColor.GRAY + "Go to page " + (page + 2))));
        }
        
        // Search button
        inv.setItem(49, createButton(Material.COMPASS, ChatColor.GREEN + "Search", 
            List.of(ChatColor.GRAY + "Search for specific items")));
        
        // Back button
        inv.setItem(46, createButton(Material.ARROW, ChatColor.YELLOW + "Back", 
            List.of(ChatColor.GRAY + "Return to display name editor")));
        
        // Store session with page info
        Map<String, Object> data = new HashMap<>();
        data.put("currentPage", page);
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.ITEM_SELECTOR, data));
        player.openInventory(inv);
    }
    
    public void openSearchGUI(Player player, Kit kit) {
        Inventory inv = Bukkit.createInventory(null, 36, SEARCH_TITLE + " - " + kit.getName());
        
        // Search categories
        inv.setItem(10, createButton(Material.DIAMOND_SWORD, ChatColor.GREEN + "Search Items", 
            List.of(ChatColor.GRAY + "Search for tools, weapons, etc.")));
        
        inv.setItem(12, createButton(Material.STONE, ChatColor.BLUE + "Search Blocks", 
            List.of(ChatColor.GRAY + "Search for building materials")));
        
        inv.setItem(14, createButton(Material.ZOMBIE_HEAD, ChatColor.RED + "Search Mobs", 
            List.of(ChatColor.GRAY + "Search for mob drops and spawn eggs")));
        
        inv.setItem(16, createButton(Material.APPLE, ChatColor.GOLD + "Search Food", 
            List.of(ChatColor.GRAY + "Search for food items")));
        
        // Back button
        inv.setItem(31, createButton(Material.ARROW, ChatColor.YELLOW + "Back", 
            List.of(ChatColor.GRAY + "Return to item selector")));
        
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.SEARCH_ITEMS));
        player.openInventory(inv);
    }
    
    private void handleDisplayNameEditGUIClick(Player player, int slot) {
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null) return;
        
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }
        
        switch (slot) {
            case 10: // Display Name - Open anvil
                openAnvilForDisplayName(player, kit);
                break;
            case 12: // Item Selector
                openItemSelectorGUI(player, kit, 0);
                break;
            case 14: // Slot Editor
                openSlotEditorGUI(player, kit);
                break;
            case 22: // Back
                openKitEditGUI(player, kit);
                break;
            case 26: // Close
                player.closeInventory();
                break;
        }
    }
    
    private void handleItemSelectorGUIClick(Player player, int slot, boolean isRightClick) {
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null) return;
        
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }
        
        // Get current page from session data
        int currentPage = (int) session.getData().getOrDefault("currentPage", 0);
        
        if (slot == 45) { // Previous Page
            if (currentPage > 0) {
                openItemSelectorGUI(player, kit, currentPage - 1);
            }
        } else if (slot == 46) { // Back
            openDisplayNameEditGUI(player, kit);
        } else if (slot == 49) { // Search
            openSearchGUI(player, kit);
        } else if (slot == 53) { // Next Page
            openItemSelectorGUI(player, kit, currentPage + 1);
        } else if (slot < 45) { // Item selection
            ItemStack clickedItem = player.getOpenInventory().getItem(slot);
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                selectItemForKit(player, kit, clickedItem.getType());
            }
        }
    }
    
    private void handleSearchGUIClick(Player player, int slot) {
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null) return;
        
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }
        
        switch (slot) {
            case 10: // Search Items
                openCategorySearchGUI(player, kit, "ITEMS", 0);
                break;
            case 12: // Search Blocks
                openCategorySearchGUI(player, kit, "BLOCKS", 0);
                break;
            case 14: // Search Mobs
                openCategorySearchGUI(player, kit, "MOBS", 0);
                break;
            case 16: // Search Food
                openCategorySearchGUI(player, kit, "FOOD", 0);
                break;
            case 31: // Back
                openItemSelectorGUI(player, kit, 0);
                break;
        }
    }
    
    private void openCategorySearchGUI(Player player, Kit kit, String category, int page) {
        String title = getCategoryTitle(category);
        Inventory inv = Bukkit.createInventory(null, 54, title + " - " + kit.getName() + " (Page " + (page + 1) + ")");
        
        // Get materials for this category
        List<Material> materials = getMaterialsForCategory(category);
        int itemsPerPage = 45; // Leave space for navigation
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, materials.size());
        
        // Add materials to GUI
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Material material = materials.get(i);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.WHITE + material.name());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Click to select this item");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }
        
        // Navigation buttons
        if (page > 0) {
            inv.setItem(45, createButton(Material.ARROW, ChatColor.YELLOW + "Previous Page", 
                List.of(ChatColor.GRAY + "Go to page " + page)));
        }
        
        if (endIndex < materials.size()) {
            inv.setItem(53, createButton(Material.ARROW, ChatColor.YELLOW + "Next Page", 
                List.of(ChatColor.GRAY + "Go to page " + (page + 2))));
        }
        
        // Back button
        inv.setItem(46, createButton(Material.ARROW, ChatColor.YELLOW + "Back", 
            List.of(ChatColor.GRAY + "Return to search categories")));
        
        // Store session with category and page info
        Map<String, Object> data = new HashMap<>();
        data.put("category", category);
        data.put("currentPage", page);
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.CATEGORY_SEARCH, data));
        player.openInventory(inv);
    }
    
    private String getCategoryTitle(String category) {
        switch (category) {
            case "ITEMS": return ChatColor.GREEN + "Items";
            case "BLOCKS": return ChatColor.BLUE + "Blocks";
            case "MOBS": return ChatColor.RED + "Mobs";
            case "FOOD": return ChatColor.GOLD + "Food";
            default: return ChatColor.WHITE + "Category";
        }
    }
    
    private List<Material> getMaterialsForCategory(String category) {
        List<Material> materials = new ArrayList<>();
        
        switch (category) {
            case "ITEMS":
                for (Material material : Material.values()) {
                    if (material.isItem() && !material.isBlock() && !isFood(material) && !isMobRelated(material)) {
                        materials.add(material);
                    }
                }
                break;
            case "BLOCKS":
                for (Material material : Material.values()) {
                    if (material.isBlock()) {
                        materials.add(material);
                    }
                }
                break;
            case "MOBS":
                for (Material material : Material.values()) {
                    if (isMobRelated(material)) {
                        materials.add(material);
                    }
                }
                break;
            case "FOOD":
                for (Material material : Material.values()) {
                    if (isFood(material)) {
                        materials.add(material);
                    }
                }
                break;
        }
        
        return materials;
    }
    
    private boolean isFood(Material material) {
        return material.isEdible() || material.name().contains("FOOD") || 
               material.name().contains("APPLE") || material.name().contains("BREAD") ||
               material.name().contains("COOKIE") || material.name().contains("CAKE") ||
               material.name().contains("PIE") || material.name().contains("STEW") ||
               material.name().contains("SOUP") || material.name().contains("MUSHROOM") ||
               material.name().contains("CARROT") || material.name().contains("POTATO") ||
               material.name().contains("BEETROOT") || material.name().contains("SWEET_BERRIES") ||
               material.name().contains("GLOW_BERRIES") || material.name().contains("CHORUS_FRUIT") ||
               material.name().contains("DRIED_KELP") || material.name().contains("HONEY_BOTTLE") ||
               material.name().contains("MILK_BUCKET") || material.name().contains("PUMPKIN_PIE") ||
               material.name().contains("RABBIT_STEW") || material.name().contains("BEETROOT_SOUP") ||
               material.name().contains("SUSPICIOUS_STEW") || material.name().contains("GOLDEN_APPLE") ||
               material.name().contains("ENCHANTED_GOLDEN_APPLE");
    }
    
    private boolean isMobRelated(Material material) {
        return material.name().contains("SPAWN_EGG") || material.name().contains("HEAD") ||
               material.name().contains("SKULL") || material.name().contains("BANNER") ||
               material.name().contains("LEATHER") || material.name().contains("ROTTEN_FLESH") ||
               material.name().contains("BONE") || material.name().contains("STRING") ||
               material.name().contains("SPIDER_EYE") || material.name().contains("GUNPOWDER") ||
               material.name().contains("SLIME_BALL") || material.name().contains("BLAZE_ROD") ||
               material.name().contains("GHAST_TEAR") || material.name().contains("ENDER_PEARL") ||
               material.name().contains("PRISMARINE_CRYSTALS") || material.name().contains("PRISMARINE_SHARD") ||
               material.name().contains("NAUTILUS_SHELL") || material.name().contains("HEART_OF_THE_SEA") ||
               material.name().contains("SCUTE") || material.name().contains("TURTLE_EGG") ||
               material.name().contains("PHANTOM_MEMBRANE") || material.name().contains("RABBIT_HIDE") ||
               material.name().contains("RABBIT_FOOT") || material.name().contains("RABBIT") ||
               material.name().contains("FEATHER") || material.name().contains("EGG") ||
               material.name().contains("INK_SAC") || material.name().contains("GLOW_INK_SAC") ||
               material.name().contains("COCOA_BEANS") || material.name().contains("LAPIS_LAZULI") ||
               material.name().contains("QUARTZ") || material.name().contains("AMETHYST_SHARD") ||
               material.name().contains("COPPER_INGOT") || material.name().contains("IRON_INGOT") ||
               material.name().contains("GOLD_INGOT") || material.name().contains("DIAMOND") ||
               material.name().contains("EMERALD") || material.name().contains("NETHERITE_INGOT") ||
               material.name().contains("EXPERIENCE_BOTTLE") || material.name().contains("ENDER_EYE") ||
               material.name().contains("NETHER_STAR") || material.name().contains("DRAGON_EGG") ||
               material.name().contains("ELYTRA") || material.name().contains("TOTEM_OF_UNDYING") ||
               material.name().contains("TRIDENT") || material.name().contains("CROSSBOW") ||
               material.name().contains("SHIELD") || material.name().contains("BOW") ||
               material.name().contains("FISHING_ROD") || material.name().contains("SHEARS") ||
               material.name().contains("FLINT_AND_STEEL") || material.name().contains("COMPASS") ||
               material.name().contains("CLOCK") || material.name().contains("MAP") ||
               material.name().contains("BOOK") || material.name().contains("WRITABLE_BOOK") ||
               material.name().contains("WRITTEN_BOOK") || material.name().contains("NAME_TAG") ||
               material.name().contains("LEAD") || material.name().contains("SADDLE") ||
               material.name().contains("CARROT_ON_A_STICK") || material.name().contains("WARPED_FUNGUS_ON_A_STICK") ||
               material.name().contains("BUCKET") || material.name().contains("WATER_BUCKET") ||
               material.name().contains("LAVA_BUCKET") || material.name().contains("POWDER_SNOW_BUCKET") ||
               material.name().contains("AXOLOTL_BUCKET") || material.name().contains("TADPOLE_BUCKET") ||
               material.name().contains("COD_BUCKET") || material.name().contains("SALMON_BUCKET") ||
               material.name().contains("PUFFERFISH_BUCKET") || material.name().contains("TROPICAL_FISH_BUCKET") ||
               material.name().contains("GOAT_HORN") || material.name().contains("MUSIC_DISC") ||
               material.name().contains("RECORD") || material.name().contains("PAINTING") ||
               material.name().contains("ITEM_FRAME") || material.name().contains("GLOW_ITEM_FRAME") ||
               material.name().contains("ARMOR_STAND") || material.name().contains("MINECART") ||
               material.name().contains("BOAT") || material.name().contains("CHEST_BOAT") ||
               material.name().contains("BED") || material.name().contains("BANNER") ||
               material.name().contains("BANNER_PATTERN") || material.name().contains("BANNER_PATTERN_ITEM");
    }
    
    private void handleCategorySearchGUIClick(Player player, int slot) {
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null || session.getMode() != EditMode.CATEGORY_SEARCH) return;
        
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }
        
        // Get current page and category from session data
        int currentPage = (int) session.getData().getOrDefault("currentPage", 0);
        String category = (String) session.getData().getOrDefault("category", "ITEMS");
        
        if (slot == 45) { // Previous Page
            if (currentPage > 0) {
                openCategorySearchGUI(player, kit, category, currentPage - 1);
            }
        } else if (slot == 46) { // Back
            openSearchGUI(player, kit);
        } else if (slot == 53) { // Next Page
            openCategorySearchGUI(player, kit, category, currentPage + 1);
        } else if (slot < 45) { // Item selection
            ItemStack clickedItem = player.getOpenInventory().getItem(slot);
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                selectItemForKit(player, kit, clickedItem.getType());
            }
        }
    }
    
    private void handleLoreEditGUIClick(Player player, int slot, boolean isRightClick, boolean isShiftClick) {
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null || session.getMode() != EditMode.LORE_EDIT) return;
        
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }
        
        if (slot == 11) { // Add Lore Book (corrected position)
            openLoreInputChat(player, kit);
        } else if (slot == 17) { // Reset Lore (corrected position)
            resetKitLore(player, kit);
        } else if (slot == 45) { // Back
            openKitEditGUI(player, kit);
        } else if (slot == 49) { // Close
            player.closeInventory();
        } else if (slot >= 27 && slot < 45) { // Lore book slots (adjusted range)
            // Check if this is a lore paper
            ItemStack clickedItem = player.getOpenInventory().getItem(slot);
            if (clickedItem != null && clickedItem.getType() == Material.PAPER) {
                handleLorePaperInGUI(player, kit, clickedItem, isRightClick, isShiftClick, slot);
            } else {
                handleLoreBookClick(player, kit, slot - 27, isRightClick, isShiftClick);
            }
        }
    }
    
    private void handleLorePaperInGUI(Player player, Kit kit, ItemStack lorePaper, boolean isRightClick, boolean isShiftClick, int slot) {
        ItemMeta meta = lorePaper.getItemMeta();
        if (meta == null || !meta.hasLore()) return;
        
        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) return;
        
        // Check if this is a lore paper (has our specific lore)
        boolean isLorePaper = false;
        for (String line : lore) {
            if (line.contains("Left Click:") && line.contains("Delete")) {
                isLorePaper = true;
                break;
            }
        }
        
        if (!isLorePaper) return;
        
        String loreText = meta.getDisplayName();
        if (loreText.equals(ChatColor.GRAY + "Empty Line")) {
            loreText = "";
        }
        
        if (!isRightClick) { // Left Click - Delete
            // Remove the lore paper from session data
            EditSession session = editSessions.get(player.getUniqueId());
            if (session != null && session.getMode() == EditMode.LORE_EDIT) {
                Map<String, Object> data = session.getData();
                data.remove("lorePaper");
                data.remove("loreText");
                editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.LORE_EDIT, data));
            }
            
            // Delete the lore paper from GUI
            player.getOpenInventory().setItem(slot, null);
            player.sendMessage(ChatColor.RED + "Lore paper deleted.");
        } else if (isRightClick) { // Right Click
            if (isShiftClick) {
                // Set index
                openLoreIndexInput(player, kit, loreText);
            } else {
                // Set lore
                setKitLore(player, kit, loreText);
            }
        }
    }
    
    private void openLoreInputChat(Player player, Kit kit) {
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "Please type the lore text in chat:");
        player.sendMessage(ChatColor.GRAY + "Supports color codes (&) and hex codes (&x&f&f&0&0&0&0)");
        player.sendMessage(ChatColor.GRAY + "Type '#' to create an empty line");
        player.sendMessage(ChatColor.RED + "Type 'cancel' to cancel");
        
        // Store the session for lore editing
        Map<String, Object> data = new HashMap<>();
        data.put("editingLore", true);
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.LORE_EDIT, data));
    }
    
    private void resetKitLore(Player player, Kit kit) {
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection == null) return;
            
            // Remove the icon section to reset to default
            kitSection.set("icon", null);
            cfg.save(file);
            plugin.getKitStorage().reload();
            
            player.sendMessage(ChatColor.GREEN + "Reset lore for " + kit.getName());
            openLoreEditGUI(player, kit);
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error resetting lore: " + e.getMessage());
        }
    }
    
    private void handleLoreBookClick(Player player, Kit kit, int bookIndex, boolean isRightClick, boolean isShiftClick) {
        // This will be implemented to handle individual lore book clicks
        // For now, just show a message
        player.sendMessage(ChatColor.YELLOW + "Lore book " + (bookIndex + 1) + " clicked");
    }
    
    private void openAnvilForDisplayName(Player player, Kit kit) {
        // Use sign interface for display name editing
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "Please type the new display name in chat:");
        player.sendMessage(ChatColor.GRAY + "Current: " + kit.getDisplayName());
        player.sendMessage(ChatColor.GRAY + "Supports color codes and hex codes");
        player.sendMessage(ChatColor.GRAY + "Example: &x&f&f&0&0&0&0 for gold");
        player.sendMessage(ChatColor.RED + "Type 'cancel' to cancel");
        
        // Store the session for chat editing
        Map<String, Object> data = new HashMap<>();
        data.put("editingDisplayName", true);
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.ANVIL_EDIT, data));
    }
    
    private void selectItemForKit(Player player, Kit kit, Material material) {
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection == null) return;
            
            // Create the icon configuration section in the correct format
            ConfigurationSection iconSection = kitSection.createSection("icon");
            iconSection.set("material", material.name());
            iconSection.set("amount", 1);
            iconSection.set("name", kit.getDisplayName());
            iconSection.set("lore", new ArrayList<String>());
            
            cfg.save(file);
            plugin.getKitStorage().reload();
            
            player.sendMessage(ChatColor.GREEN + "Updated kit icon to: " + material.name());
            
            // Return to the display name editor
            openDisplayNameEditGUI(player, kit);
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error updating kit icon: " + e.getMessage());
        }
    }
    
    private void handleAnvilClick(Player player, int slot, InventoryClickEvent event) {
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null || session.getMode() != EditMode.ANVIL_EDIT) return;
        
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }
        
        // Handle anvil result slot (slot 2)
        if (slot == 2) {
            ItemStack result = event.getCurrentItem();
            if (result != null && result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
                String newDisplayName = result.getItemMeta().getDisplayName();
                updateKitDisplayName(player, kit, newDisplayName);
                player.closeInventory();
            }
        }
    }
    
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!(event.getView().getPlayer() instanceof Player)) return;
        Player player = (Player) event.getView().getPlayer();
        
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null || session.getMode() != EditMode.ANVIL_EDIT) return;
        
        // Set the result to show the new display name
        ItemStack firstItem = event.getInventory().getItem(0);
        String renameText = event.getInventory().getRenameText();
        
        if (firstItem != null && renameText != null && !renameText.isEmpty()) {
            ItemStack result = firstItem.clone();
            ItemMeta meta = result.getItemMeta();
            meta.setDisplayName(renameText);
            result.setItemMeta(meta);
            event.setResult(result);
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        EditSession session = editSessions.get(player.getUniqueId());
        
        if (session == null) return;
        
        if (session.getMode() == EditMode.ANVIL_EDIT && session.getData().containsKey("editingDisplayName")) {
            event.setCancelled(true);
            
            String message = event.getMessage();
            
            if (message.equalsIgnoreCase("cancel")) {
                editSessions.remove(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Display name editing cancelled.");
                return;
            }
            
            // Update the display name
            Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
            if (kit != null) {
                // Run the update on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        updateKitDisplayName(player, kit, message);
                        // Create a new session for the display name edit GUI
                        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.DISPLAY_NAME_EDIT));
                        // Reopen the display name edit GUI after updating
                        openDisplayNameEditGUI(player, kit);
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "Error updating display name: " + e.getMessage());
                    }
                });
            }
        } else if (session.getMode() == EditMode.LORE_EDIT && session.getData().containsKey("editingLore")) {
            event.setCancelled(true);
            
            String message = event.getMessage();
            
            if (message.equalsIgnoreCase("cancel")) {
                editSessions.remove(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Lore editing cancelled.");
                return;
            }
            
            // Handle lore input
            Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
            if (kit != null) {
                // Run the update on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        handleLoreInput(player, kit, message);
                        // Create a new session for the lore edit GUI
                        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.LORE_EDIT));
                        // Reopen the lore edit GUI after handling input
                        openLoreEditGUI(player, kit);
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "Error handling lore input: " + e.getMessage());
                    }
                });
            }
        } else if (session.getMode() == EditMode.LORE_EDIT && session.getData().containsKey("editingLoreIndex")) {
            event.setCancelled(true);
            
            String message = event.getMessage();
            
            if (message.equalsIgnoreCase("cancel")) {
                editSessions.remove(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Lore index editing cancelled.");
                return;
            }
            
            try {
                int index = Integer.parseInt(message);
                if (index >= 0 && index < 10) { // Assuming max 10 lore lines
                    Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
                    if (kit != null) {
                        // Run the update on the main thread
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            try {
                                setKitLoreIndex(player, kit, index);
                                // Create a new session for the lore edit GUI
                                editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.LORE_EDIT));
                                // Reopen the lore edit GUI after setting index
                                openLoreEditGUI(player, kit);
                            } catch (Exception e) {
                                player.sendMessage(ChatColor.RED + "Error setting lore index: " + e.getMessage());
                            }
                        });
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Please enter a number between 0 and 9.");
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Please enter a valid number.");
            }
        }
        // Particle text editing - TEMPORARILY DISABLED
        /*
        else if (session.getMode() == EditMode.PARTICLE_EDIT && session.getData().containsKey("editingParticleText")) {
            event.setCancelled(true);
            
            String message = event.getMessage();
            
            if (message.equalsIgnoreCase("cancel")) {
                editSessions.remove(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Text editing cancelled.");
                return;
            }
            
            Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
            if (kit != null) {
                String textType = (String) session.getData().get("textType");
                // Run the update on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        saveParticleText(player, kit, textType, message);
                        // Create a new session for the particle edit GUI with a slight delay
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.PARTICLE_EDIT));
                            openParticleEditGUI(player, kit);
                        }, 2L);
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "Error saving text: " + e.getMessage());
                    }
                });
            }
        }
        */
    }
    
    private void handleLoreInput(Player player, Kit kit, String loreText) {
        try {
            // Process the lore text
            String processedLore = loreText;
            if (loreText.equals("#")) {
                processedLore = ""; // Empty line
            } else {
                // Apply color codes
                processedLore = ChatColor.translateAlternateColorCodes('&', loreText);
            }
            
            // Create a paper item to represent this lore entry
            ItemStack lorePaper = new ItemStack(Material.PAPER);
            ItemMeta meta = lorePaper.getItemMeta();
            
            if (processedLore.isEmpty()) {
                meta.setDisplayName(ChatColor.GRAY + "Empty Line");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "Left Click: " + ChatColor.WHITE + "Delete");
                lore.add(ChatColor.YELLOW + "Right Click: " + ChatColor.WHITE + "Set");
                lore.add(ChatColor.YELLOW + "Shift + Right Click: " + ChatColor.WHITE + "Set Index");
                meta.setLore(lore);
            } else {
                meta.setDisplayName(ChatColor.WHITE + processedLore);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "Left Click: " + ChatColor.WHITE + "Delete");
                lore.add(ChatColor.YELLOW + "Right Click: " + ChatColor.WHITE + "Set");
                lore.add(ChatColor.YELLOW + "Shift + Right Click: " + ChatColor.WHITE + "Set Index");
                meta.setLore(lore);
            }
            
            lorePaper.setItemMeta(meta);
            
            // Get or create session data
            EditSession session = editSessions.get(player.getUniqueId());
            Map<String, Object> data;
            if (session != null && session.getMode() == EditMode.LORE_EDIT) {
                data = session.getData();
            } else {
                data = new HashMap<>();
            }
            
            // Store the lore paper in session data
            data.put("lorePaper", lorePaper);
            data.put("loreText", processedLore);
            
            // Update or create the session
            editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.LORE_EDIT, data));
            
            // Show the paper to the player and return to GUI
            player.sendMessage(ChatColor.GREEN + "Lore paper created! Use it to manage your lore.");
            player.sendMessage(ChatColor.GRAY + "Left Click: Delete | Right Click: Set | Shift+Right Click: Set Index");
            
            // Note: GUI will be reopened by the async handler
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error processing lore: " + e.getMessage());
            throw new RuntimeException("Failed to process lore input", e);
        }
    }
    
    private void updateKitDisplayName(Player player, Kit kit, String newDisplayName) {
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection == null) return;
            
            kitSection.set("displayName", newDisplayName);
            cfg.save(file);
            plugin.getKitStorage().reload();
            
            player.sendMessage(ChatColor.GREEN + "Updated display name to: " + newDisplayName);
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error updating display name: " + e.getMessage());
            throw new RuntimeException("Failed to update display name", e);
        }
    }
    
    public void openLoreEditGUI(Player player, Kit kit) {
        Inventory inv = Bukkit.createInventory(null, 54, LORE_EDIT_TITLE + " - " + kit.getName());
        
        // Add/Reset Lore Books (corrected positions)
        inv.setItem(11, createButton(Material.BOOK, ChatColor.GREEN + "Add Lore Book", 
            List.of(ChatColor.GRAY + "Click to add a new lore book")));
        inv.setItem(17, createButton(Material.BOOK, ChatColor.RED + "Reset Lore", 
            List.of(ChatColor.GRAY + "Click to reset all lore to default")));
        
        // Display current lore books (2 rows below, starting from slot 27)
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection != null) {
                ConfigurationSection iconSection = kitSection.getConfigurationSection("icon");
                if (iconSection != null) {
                    List<String> currentLore = iconSection.getStringList("lore");
                    
                    int slot = 27; // Start from third row
                    for (int i = 0; i < Math.min(currentLore.size(), 18); i++) { // 2 rows = 18 slots
                        String loreLine = currentLore.get(i);
                        ItemStack loreBook = new ItemStack(Material.BOOK);
                        ItemMeta meta = loreBook.getItemMeta();
                        
                        if (loreLine.isEmpty()) {
                            meta.setDisplayName(ChatColor.GRAY + "Empty Line " + i);
                            List<String> bookLore = new ArrayList<>();
                            bookLore.add(ChatColor.YELLOW + "Index: " + i);
                            bookLore.add(ChatColor.GRAY + "Empty lore line");
                            meta.setLore(bookLore);
                        } else {
                            meta.setDisplayName(ChatColor.WHITE + loreLine);
                            List<String> bookLore = new ArrayList<>();
                            bookLore.add(ChatColor.YELLOW + "Index: " + i);
                            bookLore.add(ChatColor.GRAY + "Click to edit");
                            meta.setLore(bookLore);
                        }
                        
                        loreBook.setItemMeta(meta);
                        inv.setItem(slot++, loreBook);
                    }
                }
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error loading current lore: " + e.getMessage());
        }
        
        // Display any pending lore papers in the GUI
        EditSession session = editSessions.get(player.getUniqueId());
        if (session != null && session.getData().containsKey("lorePaper")) {
            ItemStack lorePaper = (ItemStack) session.getData().get("lorePaper");
            if (lorePaper != null) {
                // Find an empty slot in the paper area (slots 27-44)
                for (int i = 27; i < 45; i++) {
                    if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                        inv.setItem(i, lorePaper);
                        break;
                    }
                }
            }
        }
        
        // Navigation
        inv.setItem(45, createButton(Material.ARROW, ChatColor.YELLOW + "Back", 
            List.of(ChatColor.GRAY + "Return to kit editor")));
        inv.setItem(49, createButton(Material.BARRIER, ChatColor.RED + "Close", 
            List.of(ChatColor.GRAY + "Close the editor")));
        
        // Store the edit session with existing data
        if (session != null && session.getMode() == EditMode.LORE_EDIT) {
            // Keep existing session data
            editSessions.put(player.getUniqueId(), session);
        } else {
            // Create new session
            editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.LORE_EDIT));
        }
        
        player.openInventory(inv);
    }
    
    private void setKitLoreIndex(Player player, Kit kit, int index) {
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection == null) return;
            
            // Get or create the icon section
            ConfigurationSection iconSection = kitSection.getConfigurationSection("icon");
            if (iconSection == null) {
                iconSection = kitSection.createSection("icon");
                iconSection.set("material", kit.getIcon().getType().name());
                iconSection.set("amount", 1);
                iconSection.set("name", kit.getDisplayName());
            }
            
            // Get current lore or create new list
            List<String> currentLore = iconSection.getStringList("lore");
            
            // Get the lore text from session data
            EditSession session = editSessions.get(player.getUniqueId());
            String loreText = (String) session.getData().get("loreText");
            
            // Ensure the list is big enough
            while (currentLore.size() <= index) {
                currentLore.add("");
            }
            
            // Set the lore at the specified index
            currentLore.set(index, loreText);
            iconSection.set("lore", currentLore);
            
            cfg.save(file);
            plugin.getKitStorage().reload();
            
            player.sendMessage(ChatColor.GREEN + "Set lore at index " + index + ": " + loreText);
            
            // Remove the lore paper from session data
            if (session != null && session.getMode() == EditMode.LORE_EDIT) {
                Map<String, Object> data = session.getData();
                data.remove("lorePaper");
                data.remove("loreText");
                editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.LORE_EDIT, data));
            }
            
            // Note: GUI will be reopened by the async handler
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error setting lore index: " + e.getMessage());
            throw new RuntimeException("Failed to set lore index", e);
        }
    }
    
    public void openSlotEditorGUI(Player player, Kit kit) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Slot Editor - " + kit.getName());
        
        // Fill with dark stained glass panes to represent occupied slots
        ItemStack occupiedSlot = createButton(Material.BLACK_STAINED_GLASS_PANE, 
            ChatColor.RED + "Occupied Slot", 
            List.of(ChatColor.GRAY + "This slot is taken by another kit"));
        
        // Get all kits to show which slots are occupied
        Map<Integer, String> occupiedSlots = new HashMap<>();
        for (Kit otherKit : plugin.getKitStorage().getAll().values()) {
            if (!otherKit.getName().equals(kit.getName())) {
                occupiedSlots.put(otherKit.getSlot(), otherKit.getName());
            }
        }
        
        // Fill the GUI (54 slots, 6 rows)
        for (int i = 0; i < 54; i++) {
            if (occupiedSlots.containsKey(i)) {
                // This slot is occupied by another kit
                ItemStack occupiedItem = occupiedSlot.clone();
                ItemMeta meta = occupiedItem.getItemMeta();
                List<String> lore = meta.getLore();
                lore.add(ChatColor.YELLOW + "Kit: " + ChatColor.WHITE + occupiedSlots.get(i));
                meta.setLore(lore);
                occupiedItem.setItemMeta(meta);
                inv.setItem(i, occupiedItem);
            } else if (i == kit.getSlot()) {
                // This is the current kit's slot - make it movable
                ItemStack currentSlot = createButton(Material.LIME_STAINED_GLASS_PANE, 
                    ChatColor.GREEN + "Current Position", 
                    List.of(ChatColor.GRAY + "This is your kit's current slot",
                            ChatColor.YELLOW + "Right click to move to a new slot"));
                inv.setItem(i, currentSlot);
            } else {
                // Empty slot - available for moving
                ItemStack emptySlot = createButton(Material.WHITE_STAINED_GLASS_PANE, 
                    ChatColor.GREEN + "Empty Slot", 
                    List.of(ChatColor.GRAY + "Click to move kit here",
                            ChatColor.YELLOW + "Right click to select"));
                inv.setItem(i, emptySlot);
            }
        }
        
        // Navigation
        inv.setItem(45, createButton(Material.ARROW, ChatColor.YELLOW + "Back", 
            List.of(ChatColor.GRAY + "Return to display name editor")));
        
        inv.setItem(49, createButton(Material.BARRIER, ChatColor.RED + "Close", 
            List.of(ChatColor.GRAY + "Close the editor")));
        
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.SLOT_EDIT));
        player.openInventory(inv);
    }
    
    private void handleSlotEditorGUIClick(Player player, int slot) {
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null || session.getMode() != EditMode.SLOT_EDIT) return;
        
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }
        
        if (slot == 45) { // Back
            openDisplayNameEditGUI(player, kit);
        } else if (slot == 49) { // Close
            player.closeInventory();
        } else if (slot < 45) { // Slot selection
            // Check if this slot is occupied by another kit
            boolean isOccupied = false;
            for (Kit otherKit : plugin.getKitStorage().getAll().values()) {
                if (!otherKit.getName().equals(kit.getName()) && otherKit.getSlot() == slot) {
                    isOccupied = true;
                    break;
                }
            }
            
            if (isOccupied) {
                player.sendMessage(ChatColor.RED + "This slot is already occupied by another kit!");
                return;
            }
            
            if (slot == kit.getSlot()) {
                player.sendMessage(ChatColor.YELLOW + "This is already your kit's current slot!");
                return;
            }
            
            // Move the kit to the new slot
            updateKitSlot(player, kit, slot);
        }
    }
    
    private void updateKitSlot(Player player, Kit kit, int newSlot) {
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection == null) return;
            
            kitSection.set("slot", newSlot);
            cfg.save(file);
            plugin.getKitStorage().reload();
            
            player.sendMessage(ChatColor.GREEN + "Moved " + kit.getName() + " to slot " + newSlot);
            
            // Return to the slot editor to show the updated position
            openSlotEditorGUI(player, kit);
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error updating kit slot: " + e.getMessage());
        }
    }
    
    // Helper classes
    private static class EditSession {
        private final Kit kit;
        private final EditMode mode;
        private final Map<String, Object> data;
        
        public EditSession(Kit kit, EditMode mode) {
            this.kit = kit;
            this.mode = mode;
            this.data = new HashMap<>();
        }
        
        public EditSession(Kit kit, EditMode mode, Map<String, Object> data) {
            this.kit = kit;
            this.mode = mode;
            this.data = data;
        }
        
        public Kit getKit() { return kit; }
        public EditMode getMode() { return mode; }
        public Map<String, Object> getData() { return data; }
    }
    
    private enum EditMode {
        KIT_EDIT,
        COOLDOWN_EDIT,
        DELETE_CONFIRM,
        DISPLAY_NAME_EDIT,
        ITEM_SELECTOR,
        SEARCH_ITEMS,
        ANVIL_EDIT,
        CATEGORY_SEARCH,
        LORE_EDIT,
        SLOT_EDIT,
        PARTICLE_EDIT,
        PARTICLE_LIST,
        PARTICLE_CONFIG,
        CURRENT_PARTICLES,
        PARTICLE_TEXT
    }
    
    public void openParticleEditGUI(Player player, Kit kit) {
        Inventory inv = Bukkit.createInventory(null, 27, PARTICLE_EDIT_TITLE + " - " + kit.getName());
        
        // Get current particle configuration - TEMPORARILY DISABLED
        // Map<String, Object> particleConfig = getParticleConfig(kit);
        
        // Particles option - TEMPORARILY DISABLED
        /*
        ItemStack particlesItem = createButton(Material.FIREWORK_ROCKET, ChatColor.LIGHT_PURPLE + "Particles", 
            List.of(ChatColor.GRAY + "Configure particle effects",
                    ChatColor.YELLOW + "Left Click: Edit particles",
                    ChatColor.YELLOW + "Right Click: Preview effects"));
        inv.setItem(10, particlesItem);
        */
        
        // Title option - TEMPORARILY DISABLED
        /*
        Object titleObj = particleConfig.get("title");
        String currentTitle;
        if (titleObj == null || titleObj.equals("None")) {
            currentTitle = "No title set";
        } else {
            currentTitle = titleObj.toString();
        }
        ItemStack titleItem = createButton(Material.NAME_TAG, ChatColor.GREEN + "Title", 
            List.of(ChatColor.GRAY + "Current: " + currentTitle,
                    ChatColor.YELLOW + "Left Click: Edit text",
                    ChatColor.YELLOW + "Right Click: Edit fade/size",
                    ChatColor.RED + "Shift + Right Click: Delete"));
        inv.setItem(12, titleItem);
        
        // Action Bar option - show current text
        Object actionBarObj = particleConfig.get("actionbar");
        String currentActionBar;
        if (actionBarObj == null || actionBarObj.equals("None")) {
            currentActionBar = "No actionbar set";
        } else {
            currentActionBar = actionBarObj.toString();
        }
        ItemStack actionBarItem = createButton(Material.BOOK, ChatColor.BLUE + "Action Bar", 
            List.of(ChatColor.GRAY + "Current: " + currentActionBar,
                    ChatColor.YELLOW + "Left Click: Edit text",
                    ChatColor.YELLOW + "Right Click: Edit fade/size",
                    ChatColor.RED + "Shift + Right Click: Delete"));
        inv.setItem(14, actionBarItem);
        */
        
        // Navigation
        inv.setItem(22, createButton(Material.ARROW, ChatColor.YELLOW + "Back", 
            List.of(ChatColor.GRAY + "Return to kit editor")));
        
        inv.setItem(26, createButton(Material.BARRIER, ChatColor.RED + "Close", 
            List.of(ChatColor.GRAY + "Close the editor")));
        
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.PARTICLE_EDIT));
        player.openInventory(inv);
        
        // Debug information - TEMPORARILY DISABLED
        // player.sendMessage(ChatColor.GRAY + "[Debug] Title: " + currentTitle);
        // player.sendMessage(ChatColor.GRAY + "[Debug] ActionBar: " + currentActionBar);
    }
    
    private Map<String, Object> getParticleConfig(Kit kit) {
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection != null) {
                // Try both "particle" and "particles" sections for compatibility
                ConfigurationSection particleSection = kitSection.getConfigurationSection("particle");
                if (particleSection == null) {
                    particleSection = kitSection.getConfigurationSection("particles");
                }
                
                if (particleSection != null) {
                    Map<String, Object> config = new HashMap<>();
                    
                    // Handle title from the command structure
                    ConfigurationSection titleSection = particleSection.getConfigurationSection("title");
                    if (titleSection != null && titleSection.contains("title")) {
                        String titleText = titleSection.getString("title");
                        config.put("title", titleText != null ? titleText : "None");
                    } else {
                        config.put("title", "None");
                    }
                    
                    // Handle actionbar from the command structure
                    ConfigurationSection actionBarSection = particleSection.getConfigurationSection("actionbar");
                    if (actionBarSection != null && actionBarSection.contains("message")) {
                        String actionBarText = actionBarSection.getString("message");
                        config.put("actionbar", actionBarText != null ? actionBarText : "None");
                    } else {
                        config.put("actionbar", "None");
                    }
                    
                    // Handle particles list
                    List<Map<?, ?>> particlesList = particleSection.getMapList("effects");
                    
                    List<Map<String, Object>> convertedParticles = new ArrayList<>();
                    for (Map<?, ?> particle : particlesList) {
                        Map<String, Object> converted = new HashMap<>();
                        for (Map.Entry<?, ?> entry : particle.entrySet()) {
                            Object value = entry.getValue();
                            if (value instanceof Number) {
                                if (value instanceof Integer) {
                                    converted.put(entry.getKey().toString(), (Integer) value);
                                } else {
                                    converted.put(entry.getKey().toString(), ((Number) value).doubleValue());
                                }
                            } else {
                                converted.put(entry.getKey().toString(), value);
                            }
                        }
                        convertedParticles.add(converted);
                    }
                    config.put("particles", convertedParticles);
                    
                    // Debug information
                    System.out.println("[KitEditGUI] Particle config for " + kit.getName() + ": " + config);
                    System.out.println("[KitEditGUI] Title section: " + titleSection);
                    System.out.println("[KitEditGUI] ActionBar section: " + actionBarSection);
                    System.out.println("[KitEditGUI] Particles list size: " + convertedParticles.size());
                    
                    return config;
                }
            }
        } catch (Exception e) {
            // Return default config if error
            System.out.println("[KitEditGUI] Error reading particle config: " + e.getMessage());
        }
        
        // Default configuration
        Map<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("title", "None");
        defaultConfig.put("actionbar", "None");
        defaultConfig.put("particles", new ArrayList<>());
        return defaultConfig;
    }
    
    public void openParticleListGUI(Player player, Kit kit) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.LIGHT_PURPLE + "Particle List - " + kit.getName());
        
        // Current particles display (top middle)
        Map<String, Object> particleConfig = getParticleConfig(kit);
        List<Map<String, Object>> currentParticles = (List<Map<String, Object>>) particleConfig.getOrDefault("particles", new ArrayList<>());
        
        ItemStack currentDisplay = createButton(Material.FIREWORK_ROCKET, ChatColor.GOLD + "Current Particles", 
            List.of(ChatColor.GRAY + "Click to edit current particles",
                    ChatColor.YELLOW + "Count: " + currentParticles.size()));
        inv.setItem(4, currentDisplay);
        
        // List all available particles as papers
        int slot = 9;
        for (Particle particle : Particle.values()) {
            if (slot >= 45) break; // Leave space for navigation
            
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + particle.name());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to configure this particle");
            lore.add(ChatColor.YELLOW + "Type: " + particle.getDataType().getSimpleName());
            meta.setLore(lore);
            paper.setItemMeta(meta);
            
            inv.setItem(slot++, paper);
        }
        
        // Navigation
        inv.setItem(45, createButton(Material.ARROW, ChatColor.YELLOW + "Back", 
            List.of(ChatColor.GRAY + "Return to particle editor")));
        
        inv.setItem(49, createButton(Material.BARRIER, ChatColor.RED + "Close", 
            List.of(ChatColor.GRAY + "Close the editor")));
        
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.PARTICLE_LIST));
        player.openInventory(inv);
    }
    
    public void openParticleConfigGUI(Player player, Kit kit, Particle particle) {
        Inventory inv = Bukkit.createInventory(null, 36, ChatColor.LIGHT_PURPLE + "Configure " + particle.name());
        
        // Current values (default to 0)
        Map<String, Object> particleConfig = getParticleConfig(kit);
        List<Map<String, Object>> particles = (List<Map<String, Object>>) particleConfig.getOrDefault("particles", new ArrayList<>());
        
        // Find existing config for this particle
        Map<String, Object> existingConfig = null;
        for (Map<String, Object> p : particles) {
            if (particle.name().equals(p.get("type"))) {
                existingConfig = p;
                break;
            }
        }
        
        if (existingConfig == null) {
            existingConfig = new HashMap<>();
            existingConfig.put("type", particle.name());
            existingConfig.put("x", 0.0);
            existingConfig.put("y", 0.0);
            existingConfig.put("z", 0.0);
            existingConfig.put("speed", 0.0);
            existingConfig.put("count", 1);
        }
        
        // X, Y, Z, Speed, Count controls
        Object xControlObj = existingConfig.getOrDefault("x", 0.0);
        double xControl = xControlObj instanceof Number ? ((Number) xControlObj).doubleValue() : 0.0;
        
        Object yControlObj = existingConfig.getOrDefault("y", 0.0);
        double yControl = yControlObj instanceof Number ? ((Number) yControlObj).doubleValue() : 0.0;
        
        Object zControlObj = existingConfig.getOrDefault("z", 0.0);
        double zControl = zControlObj instanceof Number ? ((Number) zControlObj).doubleValue() : 0.0;
        
        Object speedControlObj = existingConfig.getOrDefault("speed", 0.0);
        double speedControl = speedControlObj instanceof Number ? ((Number) speedControlObj).doubleValue() : 0.0;
        
        Object countControlObj = existingConfig.getOrDefault("count", 1);
        int countControl = countControlObj instanceof Number ? ((Number) countControlObj).intValue() : 1;
        
        inv.setItem(10, createValueButton(Material.REDSTONE, "X", xControl));
        inv.setItem(12, createValueButton(Material.LIME_DYE, "Y", yControl));
        inv.setItem(14, createValueButton(Material.LAPIS_LAZULI, "Z", zControl));
        inv.setItem(16, createValueButton(Material.GLOWSTONE_DUST, "Speed", speedControl));
        inv.setItem(28, createValueButton(Material.EMERALD, "Count", countControl));
        
        // Show current particles in the top row
        int slot = 0;
        for (Map<String, Object> p : particles) {
            if (slot >= 9) break;
            
            String type = (String) p.get("type");
            
            Object xObj = p.getOrDefault("x", 0.0);
            double x = xObj instanceof Number ? ((Number) xObj).doubleValue() : 0.0;
            
            Object yObj = p.getOrDefault("y", 0.0);
            double y = yObj instanceof Number ? ((Number) yObj).doubleValue() : 0.0;
            
            Object zObj = p.getOrDefault("z", 0.0);
            double z = zObj instanceof Number ? ((Number) zObj).doubleValue() : 0.0;
            
            Object speedObj = p.getOrDefault("speed", 0.0);
            double speed = speedObj instanceof Number ? ((Number) speedObj).doubleValue() : 0.0;
            
            Object countObj = p.getOrDefault("count", 1);
            int count = countObj instanceof Number ? ((Number) countObj).intValue() : 1;
            
            ItemStack item = new ItemStack(Material.FIREWORK_ROCKET);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + type);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "X: " + x);
            lore.add(ChatColor.GRAY + "Y: " + y);
            lore.add(ChatColor.GRAY + "Z: " + z);
            lore.add(ChatColor.GRAY + "Speed: " + speed);
            lore.add(ChatColor.GRAY + "Count: " + count);
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            inv.setItem(slot++, item);
        }
        
        // Confirm/Cancel buttons
        inv.setItem(30, createButton(Material.EMERALD_BLOCK, ChatColor.GREEN + "Confirm", 
            List.of(ChatColor.GRAY + "Save particle configuration")));
        
        inv.setItem(32, createButton(Material.REDSTONE_BLOCK, ChatColor.RED + "Cancel", 
            List.of(ChatColor.GRAY + "Cancel without saving")));
        
        // Store particle info in session
        Map<String, Object> data = new HashMap<>();
        data.put("editingParticle", particle.name());
        data.put("particleConfig", existingConfig);
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.PARTICLE_CONFIG, data));
        
        player.openInventory(inv);
    }
    
    public void openCurrentParticlesGUI(Player player, Kit kit) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Current Particles - " + kit.getName());
        
        Map<String, Object> particleConfig = getParticleConfig(kit);
        List<Map<String, Object>> particles = (List<Map<String, Object>>) particleConfig.getOrDefault("particles", new ArrayList<>());
        
        int slot = 0;
        for (Map<String, Object> particle : particles) {
            if (slot >= 45) break;
            
            String type = (String) particle.get("type");
            
            Object xObj = particle.getOrDefault("x", 0.0);
            double x = xObj instanceof Number ? ((Number) xObj).doubleValue() : 0.0;
            
            Object yObj = particle.getOrDefault("y", 0.0);
            double y = yObj instanceof Number ? ((Number) yObj).doubleValue() : 0.0;
            
            Object zObj = particle.getOrDefault("z", 0.0);
            double z = zObj instanceof Number ? ((Number) zObj).doubleValue() : 0.0;
            
            Object speedObj = particle.getOrDefault("speed", 0.0);
            double speed = speedObj instanceof Number ? ((Number) speedObj).doubleValue() : 0.0;
            
            Object countObj = particle.getOrDefault("count", 1);
            int count = countObj instanceof Number ? ((Number) countObj).intValue() : 1;
            
            ItemStack item = new ItemStack(Material.FIREWORK_ROCKET);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + type);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "X: " + x);
            lore.add(ChatColor.GRAY + "Y: " + y);
            lore.add(ChatColor.GRAY + "Z: " + z);
            lore.add(ChatColor.GRAY + "Speed: " + speed);
            lore.add(ChatColor.GRAY + "Count: " + count);
            lore.add("");
            lore.add(ChatColor.YELLOW + "Left Click: Edit");
            lore.add(ChatColor.RED + "Right Click: Remove");
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            inv.setItem(slot++, item);
        }
        
        // Navigation
        inv.setItem(45, createButton(Material.ARROW, ChatColor.YELLOW + "Back", 
            List.of(ChatColor.GRAY + "Return to particle list")));
        
        inv.setItem(49, createButton(Material.BARRIER, ChatColor.RED + "Close", 
            List.of(ChatColor.GRAY + "Close the editor")));
        
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.CURRENT_PARTICLES));
        player.openInventory(inv);
    }
    
    private static ItemStack createValueButton(Material material, String name, double value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + name + ": " + ChatColor.WHITE + value);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Current: " + ChatColor.WHITE + value);
        lore.add("");
        lore.add(ChatColor.GREEN + "Right Click: " + ChatColor.WHITE + "Add 0.1");
        lore.add(ChatColor.BLUE + "Shift + Right Click: " + ChatColor.WHITE + "Add 1.0");
        lore.add(ChatColor.RED + "Left Click: " + ChatColor.WHITE + "Remove 0.1");
        lore.add(ChatColor.DARK_RED + "Shift + Left Click: " + ChatColor.WHITE + "Remove 1.0");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private static ItemStack createValueButton(Material material, String name, int value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + name + ": " + ChatColor.WHITE + value);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Current: " + ChatColor.WHITE + value);
        lore.add("");
        lore.add(ChatColor.GREEN + "Right Click: " + ChatColor.WHITE + "Add 1");
        lore.add(ChatColor.BLUE + "Shift + Right Click: " + ChatColor.WHITE + "Add 10");
        lore.add(ChatColor.RED + "Left Click: " + ChatColor.WHITE + "Remove 1");
        lore.add(ChatColor.DARK_RED + "Shift + Left Click: " + ChatColor.WHITE + "Remove 10");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private void handleParticleEditGUIClick(Player player, int slot, boolean isRightClick, boolean isShiftClick) {
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null || session.getMode() != EditMode.PARTICLE_EDIT) return;
        
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }
        
        switch (slot) {
            // Particle functionality - TEMPORARILY DISABLED
            /*
            case 10: // Particles
                if (isRightClick) {
                    // Preview effects
                    previewParticleEffects(player, kit);
                } else {
                    // Edit particles
                    openParticleListGUI(player, kit);
                }
                break;
            case 12: // Title
                if (isShiftClick && isRightClick) {
                    // Delete title
                    deleteParticleText(player, kit, "title");
                } else if (isRightClick) {
                    // Edit fade/size
                    openTextFadeSizeGUI(player, kit, "title");
                } else {
                    // Edit text
                    openTextInputChat(player, kit, "title");
                }
                break;
            case 14: // Action Bar
                if (isShiftClick && isRightClick) {
                    // Delete actionbar
                    deleteParticleText(player, kit, "actionbar");
                } else if (isRightClick) {
                    // Edit fade/size
                    openTextFadeSizeGUI(player, kit, "actionbar");
                } else {
                    // Edit text
                    openTextInputChat(player, kit, "actionbar");
                }
                break;
            */
            case 22: // Back
                openKitEditGUI(player, kit);
                break;
            case 26: // Close
                player.closeInventory();
                break;
        }
    }
    
    private void handleParticleListGUIClick(Player player, int slot) {
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null || session.getMode() != EditMode.PARTICLE_LIST) return;
        
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }
        
        if (slot == 4) { // Current Particles
            openCurrentParticlesGUI(player, kit);
        } else if (slot == 45) { // Back
            openParticleEditGUI(player, kit);
        } else if (slot == 49) { // Close
            player.closeInventory();
        } else if (slot >= 9 && slot < 45) { // Particle selection
            ItemStack clickedItem = player.getOpenInventory().getItem(slot);
            if (clickedItem != null && clickedItem.getType() == Material.PAPER) {
                String particleName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                try {
                    Particle particle = Particle.valueOf(particleName);
                    openParticleConfigGUI(player, kit, particle);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + "Invalid particle: " + particleName);
                }
            }
        }
    }
    
    private void handleParticleConfigGUIClick(Player player, int slot, boolean isRightClick, boolean isShiftClick) {
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null || session.getMode() != EditMode.PARTICLE_CONFIG) return;
        
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }
        
        Map<String, Object> particleConfig = (Map<String, Object>) session.getData().get("particleConfig");
        if (particleConfig == null) return;
        
        if (slot == 30) { // Confirm
            saveParticleConfig(player, kit, particleConfig);
        } else if (slot == 32) { // Cancel
            openParticleListGUI(player, kit);
        } else if (slot == 10) { // X
            updateParticleValue(player, kit, particleConfig, "x", isRightClick, isShiftClick);
        } else if (slot == 12) { // Y
            updateParticleValue(player, kit, particleConfig, "y", isRightClick, isShiftClick);
        } else if (slot == 14) { // Z
            updateParticleValue(player, kit, particleConfig, "z", isRightClick, isShiftClick);
        } else if (slot == 16) { // Speed
            updateParticleValue(player, kit, particleConfig, "speed", isRightClick, isShiftClick);
        } else if (slot == 28) { // Count
            updateParticleCount(player, kit, particleConfig, isRightClick, isShiftClick);
        }
    }
    
    private void handleCurrentParticlesGUIClick(Player player, int slot, boolean isRightClick) {
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null || session.getMode() != EditMode.CURRENT_PARTICLES) return;
        
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }
        
        if (slot == 45) { // Back
            openParticleListGUI(player, kit);
        } else if (slot == 49) { // Close
            player.closeInventory();
        } else if (slot < 45) { // Particle management
            ItemStack clickedItem = player.getOpenInventory().getItem(slot);
            if (clickedItem != null && clickedItem.getType() == Material.FIREWORK_ROCKET) {
                String particleName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                if (isRightClick) {
                    // Remove particle
                    removeParticle(player, kit, particleName);
                } else {
                    // Edit particle
                    try {
                        Particle particle = Particle.valueOf(particleName);
                        openParticleConfigGUI(player, kit, particle);
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(ChatColor.RED + "Invalid particle: " + particleName);
                    }
                }
            }
        }
    }
    
    private void updateParticleValue(Player player, Kit kit, Map<String, Object> particleConfig, String value, boolean isRightClick, boolean isShiftClick) {
        Object currentValueObj = particleConfig.getOrDefault(value, 0.0);
        double currentValue;
        
        if (currentValueObj instanceof Number) {
            currentValue = ((Number) currentValueObj).doubleValue();
        } else {
            currentValue = 0.0;
        }
        
        double change = isShiftClick ? 1.0 : 0.1;
        
        if (isRightClick) {
            currentValue += change;
        } else {
            currentValue -= change;
        }
        
        particleConfig.put(value, currentValue);
        
        // Save the updated particle config to file
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection != null) {
                ConfigurationSection particleSection = kitSection.getConfigurationSection("particles");
                if (particleSection == null) {
                    particleSection = kitSection.createSection("particles");
                }
                
                // Get existing particles
                List<Map<?, ?>> existingParticles = particleSection.getMapList("effects");
                List<Map<String, Object>> particles = new ArrayList<>();
                
                for (Map<?, ?> p : existingParticles) {
                    Map<String, Object> particle = new HashMap<>();
                    for (Map.Entry<?, ?> entry : p.entrySet()) {
                        particle.put(entry.getKey().toString(), entry.getValue());
                    }
                    particles.add(particle);
                }
                
                // Update the specific particle
                String particleType = (String) particleConfig.get("type");
                for (int i = 0; i < particles.size(); i++) {
                    if (particleType.equals(particles.get(i).get("type"))) {
                        particles.set(i, particleConfig);
                        break;
                    }
                }
                
                // Save back to file
                particleSection.set("effects", particles);
                cfg.save(file);
                plugin.getKitStorage().reload();
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error saving particle value: " + e.getMessage());
        }
        
        // Create a new session with the updated particle config
        Map<String, Object> data = new HashMap<>();
        data.put("particleConfig", particleConfig);
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.PARTICLE_CONFIG, data));
        
        // Refresh the GUI with a slight delay to ensure the session is properly set
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String particleName = (String) particleConfig.get("type");
            try {
                Particle particle = Particle.valueOf(particleName);
                openParticleConfigGUI(player, kit, particle);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "Error refreshing particle config");
            }
        }, 1L);
    }
    
    private void saveParticleConfig(Player player, Kit kit, Map<String, Object> particleConfig) {
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection == null) return;
            
            // Use the "particles" section to match the command structure
            ConfigurationSection particleSection = kitSection.getConfigurationSection("particles");
            if (particleSection == null) {
                particleSection = kitSection.createSection("particles");
            }
            
            // Get existing particles from the effects list
            List<Map<?, ?>> existingParticles = particleSection.getMapList("effects");
            
            List<Map<String, Object>> particles = new ArrayList<>();
            for (Map<?, ?> p : existingParticles) {
                Map<String, Object> particle = new HashMap<>();
                for (Map.Entry<?, ?> entry : p.entrySet()) {
                    particle.put(entry.getKey().toString(), entry.getValue());
                }
                particles.add(particle);
            }
            
            // Remove existing config for this particle type
            particles.removeIf(p -> particleConfig.get("type").equals(p.get("type")));
            
            // Add new config
            particles.add(particleConfig);
            
            // Save in the effects list directly
            particleSection.set("effects", particles);
            cfg.save(file);
            plugin.getKitStorage().reload();
            
            player.sendMessage(ChatColor.GREEN + "Saved particle configuration for " + particleConfig.get("type"));
            openParticleListGUI(player, kit);
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error saving particle config: " + e.getMessage());
        }
    }
    
    private void removeParticle(Player player, Kit kit, String particleName) {
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection == null) return;
            
            // Use the "particles" section to match the command structure
            ConfigurationSection particleSection = kitSection.getConfigurationSection("particles");
            if (particleSection == null) return;
            
            // Get existing particles from the effects list
            List<Map<?, ?>> existingParticles = particleSection.getMapList("effects");
            
            List<Map<String, Object>> particles = new ArrayList<>();
            for (Map<?, ?> p : existingParticles) {
                Map<String, Object> particle = new HashMap<>();
                for (Map.Entry<?, ?> entry : p.entrySet()) {
                    particle.put(entry.getKey().toString(), entry.getValue());
                }
                particles.add(particle);
            }
            
            // Remove the particle
            particles.removeIf(p -> particleName.equals(p.get("type")));
            
            // Save in the effects list directly
            particleSection.set("effects", particles);
            cfg.save(file);
            plugin.getKitStorage().reload();
            
            player.sendMessage(ChatColor.GREEN + "Removed particle: " + particleName);
            openCurrentParticlesGUI(player, kit);
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error removing particle: " + e.getMessage());
        }
    }
    
    private void openTextInputChat(Player player, Kit kit, String type) {
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "Please type the " + type + " text in chat:");
        player.sendMessage(ChatColor.GRAY + "Supports color codes (&) and hex codes");
        player.sendMessage(ChatColor.RED + "Type 'cancel' to cancel");
        
        Map<String, Object> data = new HashMap<>();
        data.put("editingParticleText", true);
        data.put("textType", type);
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.PARTICLE_EDIT, data));
    }
    
    private void openTextFadeSizeGUI(Player player, Kit kit, String type) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Edit " + type + " Settings - " + kit.getName());
        
        Map<String, Object> particleConfig = getParticleConfig(kit);
        
        // Get values from the command structure format
        int fadeIn = 10;
        int fadeOut = 10;
        int stay = 40;
        int size = 1;
        
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            if (kitSection != null) {
                ConfigurationSection particleSection = kitSection.getConfigurationSection("particles");
                if (particleSection != null) {
                    ConfigurationSection typeSection = particleSection.getConfigurationSection(type);
                    if (typeSection != null) {
                        fadeIn = typeSection.getInt("fadeIn", 10);
                        fadeOut = typeSection.getInt("fadeOut", 10);
                        stay = typeSection.getInt("stay", 40);
                        size = typeSection.getInt("size", 1);
                    }
                }
            }
        } catch (Exception e) {
            // Use defaults if error
        }
        
        inv.setItem(10, createValueButton(Material.GREEN_DYE, "Fade In", fadeIn));
        inv.setItem(12, createValueButton(Material.RED_DYE, "Fade Out", fadeOut));
        inv.setItem(14, createValueButton(Material.YELLOW_DYE, "Stay", stay));
        inv.setItem(16, createValueButton(Material.BLUE_DYE, "Size", size));
        
        // Current text display
        Object textObj = particleConfig.get(type);
        String displayText;
        if (textObj == null || textObj.equals("None")) {
            displayText = "No " + type + " set";
        } else {
            displayText = textObj.toString();
        }
        inv.setItem(22, createButton(Material.PAPER, ChatColor.GOLD + "Current Text", 
            List.of(ChatColor.GRAY + displayText)));
        
        // Navigation
        inv.setItem(26, createButton(Material.ARROW, ChatColor.YELLOW + "Back", 
            List.of(ChatColor.GRAY + "Return to particle editor")));
        
        Map<String, Object> data = new HashMap<>();
        data.put("editingTextSettings", true);
        data.put("textType", type);
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.PARTICLE_EDIT, data));
        
        player.openInventory(inv);
    }
    
    private void deleteParticleText(Player player, Kit kit, String type) {
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection == null) return;
            
            ConfigurationSection particleSection = kitSection.getConfigurationSection("particles");
            if (particleSection == null) return;
            
            // Delete the specific section based on type
            if (type.equals("title")) {
                particleSection.set("title", null);
            } else if (type.equals("actionbar")) {
                particleSection.set("actionbar", null);
            }
            
            cfg.save(file);
            plugin.getKitStorage().reload();
            
            player.sendMessage(ChatColor.GREEN + "Deleted " + type + " configuration");
            openParticleEditGUI(player, kit);
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error deleting " + type + ": " + e.getMessage());
        }
    }
    
    private void previewParticleEffects(Player player, Kit kit) {
        Map<String, Object> particleConfig = getParticleConfig(kit);
        // This would call the ParticleEffects utility to preview the effects
        player.sendMessage(ChatColor.GREEN + "Particle effects previewed!");
    }
    
    private void saveParticleText(Player player, Kit kit, String type, String text) {
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection == null) return;
            
            // Use the "particles" section to match the command structure
            ConfigurationSection particleSection = kitSection.getConfigurationSection("particles");
            if (particleSection == null) {
                particleSection = kitSection.createSection("particles");
            }
            
            // Apply color codes
            String processedText = ChatColor.translateAlternateColorCodes('&', text);
            
            // Save in the command structure format
            if (type.equals("title")) {
                ConfigurationSection titleSection = particleSection.getConfigurationSection("title");
                if (titleSection == null) {
                    titleSection = particleSection.createSection("title");
                }
                titleSection.set("enabled", true);
                titleSection.set("title", processedText);
                titleSection.set("subtitle", "");
                titleSection.set("fadeIn", 10);
                titleSection.set("stay", 60);
                titleSection.set("fadeOut", 10);
            } else if (type.equals("actionbar")) {
                ConfigurationSection actionBarSection = particleSection.getConfigurationSection("actionbar");
                if (actionBarSection == null) {
                    actionBarSection = particleSection.createSection("actionbar");
                }
                actionBarSection.set("enabled", true);
                actionBarSection.set("message", processedText);
                actionBarSection.set("duration", 60);
            }
            
            cfg.save(file);
            plugin.getKitStorage().reload();
            
            player.sendMessage(ChatColor.GREEN + "Saved " + type + " text: " + processedText);
            
            // Debug information
            System.out.println("[KitEditGUI] Saved " + type + " text for " + kit.getName() + ": " + processedText);
            System.out.println("[KitEditGUI] Full particle section after save: " + particleSection);
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error saving " + type + " text: " + e.getMessage());
            System.out.println("[KitEditGUI] Error saving " + type + " text: " + e.getMessage());
        }
    }
    
    private void updateTextSetting(Player player, Kit kit, String type, String setting, boolean isRightClick, boolean isShiftClick) {
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection == null) return;
            
            ConfigurationSection particleSection = kitSection.getConfigurationSection("particles");
            if (particleSection == null) return;
            
            ConfigurationSection typeSection = particleSection.getConfigurationSection(type);
            if (typeSection == null) return;
            
            int currentValue = typeSection.getInt(setting, 10);
            int change = isShiftClick ? 10 : 1;
            
            if (isRightClick) {
                currentValue += change;
            } else {
                currentValue = Math.max(0, currentValue - change);
            }
            
            typeSection.set(setting, currentValue);
            cfg.save(file);
            plugin.getKitStorage().reload();
            
            player.sendMessage(ChatColor.GREEN + "Updated " + setting + " to " + currentValue);
            
            // Create a new session for the text settings GUI with a slight delay
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Map<String, Object> data = new HashMap<>();
                data.put("editingTextSettings", true);
                data.put("textType", type);
                editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.PARTICLE_EDIT, data));
                openTextFadeSizeGUI(player, kit, type);
            }, 1L);
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error updating " + setting + ": " + e.getMessage());
        }
    }
    
    private void handleTextSettingsGUIClick(Player player, int slot, boolean isRightClick, boolean isShiftClick) {
        EditSession session = editSessions.get(player.getUniqueId());
        if (session == null || session.getMode() != EditMode.PARTICLE_EDIT) return;
        
        Kit kit = plugin.getKitStorage().getKit(session.getKit().getName());
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }
        
        String textType = (String) session.getData().get("textType");
        if (textType == null) return;
        
        switch (slot) {
            case 10: // Fade In
                updateTextSetting(player, kit, textType, "fadeIn", isRightClick, isShiftClick);
                break;
            case 12: // Fade Out
                updateTextSetting(player, kit, textType, "fadeOut", isRightClick, isShiftClick);
                break;
            case 14: // Stay
                updateTextSetting(player, kit, textType, "stay", isRightClick, isShiftClick);
                break;
            case 16: // Size
                updateTextSetting(player, kit, textType, "size", isRightClick, isShiftClick);
                break;
            case 26: // Back
                openParticleEditGUI(player, kit);
                break;
        }
    }
    
    private void updateParticleCount(Player player, Kit kit, Map<String, Object> particleConfig, boolean isRightClick, boolean isShiftClick) {
        Object currentValueObj = particleConfig.getOrDefault("count", 1);
        int currentValue;
        
        if (currentValueObj instanceof Number) {
            currentValue = ((Number) currentValueObj).intValue();
        } else {
            currentValue = 1;
        }
        
        int change = isShiftClick ? 10 : 1;
        
        if (isRightClick) {
            currentValue += change;
        } else {
            currentValue = Math.max(1, currentValue - change);
        }
        
        particleConfig.put("count", currentValue);
        
        // Save the updated particle config to file
        try {
            File file = plugin.getConfigManager().getKitsFile();
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection kitSection = cfg.getConfigurationSection("kits." + kit.getName());
            
            if (kitSection != null) {
                ConfigurationSection particleSection = kitSection.getConfigurationSection("particles");
                if (particleSection == null) {
                    particleSection = kitSection.createSection("particles");
                }
                
                // Get existing particles
                List<Map<?, ?>> existingParticles = particleSection.getMapList("effects");
                List<Map<String, Object>> particles = new ArrayList<>();
                
                for (Map<?, ?> p : existingParticles) {
                    Map<String, Object> particle = new HashMap<>();
                    for (Map.Entry<?, ?> entry : p.entrySet()) {
                        particle.put(entry.getKey().toString(), entry.getValue());
                    }
                    particles.add(particle);
                }
                
                // Update the specific particle
                String particleType = (String) particleConfig.get("type");
                for (int i = 0; i < particles.size(); i++) {
                    if (particleType.equals(particles.get(i).get("type"))) {
                        particles.set(i, particleConfig);
                        break;
                    }
                }
                
                // Save back to file
                particleSection.set("effects", particles);
                cfg.save(file);
                plugin.getKitStorage().reload();
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error saving particle count: " + e.getMessage());
        }
        
        // Create a new session with the updated particle config
        Map<String, Object> data = new HashMap<>();
        data.put("particleConfig", particleConfig);
        editSessions.put(player.getUniqueId(), new EditSession(kit, EditMode.PARTICLE_CONFIG, data));
        
        // Refresh the GUI with a slight delay to ensure the session is properly set
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String particleName = (String) particleConfig.get("type");
            try {
                Particle particle = Particle.valueOf(particleName);
                openParticleConfigGUI(player, kit, particle);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "Error refreshing particle config");
            }
        }, 1L);
    }
}
