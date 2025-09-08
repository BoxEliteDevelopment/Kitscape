package tiie.kitscape.kit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class Kit {

    private final String name;
    private final String displayName;
    private final ItemStack[] items;
    private final Map<String, Long> cooldowns;
    private final int slot;
    private final String hexColor;
    private final ItemStack guiIcon;

    public Kit(String name,
               String displayName,
               ItemStack[] items,
               Map<String,Long> cooldowns,
               int slot,
               String hexColor,
               ItemStack icon
    ) {
        this.name        = name;
        this.displayName = displayName;
        this.items       = items;
        this.cooldowns   = cooldowns;
        this.slot        = slot;
        this.hexColor    = hexColor;
        // use the passed-in icon, or fallback to BARRIER if null
        this.guiIcon     = (icon != null ? icon.clone()
                : new ItemStack(Material.BARRIER));
    }

    public String getName()          { return name; }
    public String getDisplayName()   { return displayName; }
    public ItemStack[] getItems()    { return items; }
    public Map<String,Long> getCooldowns() { return cooldowns; }
    public int getSlot()             { return slot; }
    public String getHexColor()      { return hexColor; }
    public ItemStack getIcon() {
        return guiIcon.clone();
    }
}
