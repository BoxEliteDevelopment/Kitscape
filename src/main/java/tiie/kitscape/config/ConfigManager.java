package tiie.kitscape.config;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class ConfigManager {

    private final JavaPlugin plugin;
    private final File kitsFile, cooldownFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        kitsFile     = new File(plugin.getDataFolder(), "kits.yml");
        cooldownFile = new File(plugin.getDataFolder(), "cooldowns.yml");
        plugin.saveResource("kits.yml", false);
    }

    public CompletableFuture<Void> loadKitsAsync() {
        return CompletableFuture.runAsync(() -> {
            // parse kitsFile into memory
        });
    }

    public CompletableFuture<Void> saveCooldownsAsync() {
        return CompletableFuture.runAsync(() -> {
            // serialize cooldowns to cooldownFile
        });
    }


    public File getKitsFile() {
        return kitsFile;
    }

    public File getCooldownFile() {
        return cooldownFile;
    }

}
