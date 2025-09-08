package tiie.kitscape.session;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionManager {

    private final JavaPlugin plugin;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<UUID, String> active = new ConcurrentHashMap<>();

    public SessionManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start(UUID player, String kitName, long timeoutSeconds) {
        active.put(player, kitName);
        scheduler.schedule(() -> active.remove(player), timeoutSeconds, TimeUnit.SECONDS);
    }

    public String confirm(UUID player) {
        return active.remove(player);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
