package tiie.kitscape;

import org.bukkit.plugin.java.JavaPlugin;
import tiie.kitscape.command.Commands;
import tiie.kitscape.config.ConfigManager;
import tiie.kitscape.cooldown.CooldownManager;
import tiie.kitscape.gui.KitScapeGUI;
import tiie.kitscape.gui.KitEditGUI;
import tiie.kitscape.kit.KitStorage;
import tiie.kitscape.session.SessionManager;
import tiie.kitscape.tabcomplete.KitscapeTabCompleter;

public final class KitScape extends JavaPlugin {

    private ConfigManager config;
    private KitStorage kits;
    private SessionManager sessions;
    private CooldownManager cooldowns;
    private KitScapeGUI gui;
    private KitEditGUI editGui;

    @Override
    public void onEnable() {
        // Plugin startup logic

        saveDefaultConfig();
        config      = new ConfigManager(this);
        kits        = new KitStorage(this, config);
        sessions    = new SessionManager(this);
        cooldowns   = new CooldownManager(this);
        gui         = new KitScapeGUI(this, kits, sessions, cooldowns);
        editGui     = new KitEditGUI(this);

        getCommand("kitscape").setExecutor(new Commands(this));
        getCommand("kitscape").setTabCompleter(new KitscapeTabCompleter(this));
        getServer().getPluginManager()
                .registerEvents(gui, this);
        getServer().getPluginManager()
                .registerEvents(editGui, this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        sessions.shutdown();
        cooldowns.saveAll();
    }

    public ConfigManager getConfigManager() { return config; }
    public KitStorage    getKitStorage()    { return kits; }
    public SessionManager getSessionMgr()   { return sessions; }
    public CooldownManager getCooldownMgr() { return cooldowns; }
    public KitScapeGUI    getGui()          { return gui; }
    public KitEditGUI     getKitEditGUI()   { return editGui; }
}
