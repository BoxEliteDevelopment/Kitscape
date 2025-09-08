package tiie.kitscape.cooldown;

import tiie.kitscape.KitScape;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final KitScape plugin;
    private final Map<UUID, Map<String,Long>> data = new ConcurrentHashMap<>();

    public CooldownManager(KitScape plugin) {
        this.plugin = plugin;
    }

    public boolean isOnCooldown(UUID player, String kit) {
        return data.getOrDefault(player, Map.of())
                .getOrDefault(kit, 0L) > System.currentTimeMillis();
    }

    public void setCooldown(UUID player, String kit, long millis) {
        data.computeIfAbsent(player, k->new ConcurrentHashMap<>())
                .put(kit, System.currentTimeMillis()+millis);
    }

    public long timeLeft(UUID player, String kit) {
        long expiry = data.getOrDefault(player, Map.of())
                .getOrDefault(kit, 0L);
        return Math.max(expiry - System.currentTimeMillis(), 0L);
    }

    public void saveAll() {
        plugin.getConfigManager().saveCooldownsAsync();
    }
}
