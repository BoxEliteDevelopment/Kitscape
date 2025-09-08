package tiie.kitscape.utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;
import java.util.Map;

public class ParticleEffects {

    public static void playKitClaimEffects(Player player, ConfigurationSection particleConfig) {
        if (particleConfig == null) {
            // Default effects if no config
            playDefaultEffects(player);
            return;
        }

        Location loc = player.getLocation().add(0, 1, 0);
        World world = player.getWorld();

        // Play particles
        if (particleConfig.getBoolean("particles.enabled", true)) {
            playParticles(world, loc, particleConfig.getConfigurationSection("particles"));
        }

        // Play title
        if (particleConfig.getBoolean("title.enabled", true)) {
            playTitle(player, particleConfig.getConfigurationSection("title"));
        }

        // Play action bar
        if (particleConfig.getBoolean("actionbar.enabled", true)) {
            playActionBar(player, particleConfig.getConfigurationSection("actionbar"));
        }

        // Play sounds
        if (particleConfig.getBoolean("sounds.enabled", true)) {
            playSounds(player, particleConfig.getConfigurationSection("sounds"));
        }
    }

    private static void playDefaultEffects(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        World world = player.getWorld();

        // Default particles
        world.spawnParticle(Particle.EXPLOSION, loc, 8, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticle(Particle.FIREWORK, loc, 16, 0.5, 0.5, 0.5, 0.05);
        world.spawnParticle(Particle.FLAME, loc, 20, 0.3, 0.3, 0.3, 0.02);

        // Default title
        player.sendTitle(
            ChatColor.translateAlternateColorCodes('&', "&6Kit Claimed!"),
            "",
            10, 60, 10
        );

        // Default action bar
        player.spigot().sendMessage(
            ChatMessageType.ACTION_BAR,
            new TextComponent(ChatColor.translateAlternateColorCodes('&', "&a&lKit successfully claimed!"))
        );
    }

    private static void playParticles(World world, Location loc, ConfigurationSection config) {
        if (config == null) return;

        List<Map<?, ?>> particles = config.getMapList("effects");
        for (Map<?, ?> particleData : particles) {
            try {
                String particleName = (String) particleData.get("type");
                Object countObj = particleData.get("count");
                Object offsetXObj = particleData.get("offsetX");
                Object offsetYObj = particleData.get("offsetY");
                Object offsetZObj = particleData.get("offsetZ");
                Object speedObj = particleData.get("speed");
                
                int count = countObj instanceof Number ? ((Number) countObj).intValue() : 10;
                double offsetX = offsetXObj instanceof Number ? ((Number) offsetXObj).doubleValue() : 0.5;
                double offsetY = offsetYObj instanceof Number ? ((Number) offsetYObj).doubleValue() : 0.5;
                double offsetZ = offsetZObj instanceof Number ? ((Number) offsetZObj).doubleValue() : 0.5;
                double speed = speedObj instanceof Number ? ((Number) speedObj).doubleValue() : 0.1;

                Particle particle = Particle.valueOf(particleName.toUpperCase());
                world.spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, speed);
            } catch (Exception e) {
                // Skip invalid particle types
            }
        }
    }

    private static void playTitle(Player player, ConfigurationSection config) {
        if (config == null) return;

        String title = ChatColor.translateAlternateColorCodes('&', 
            config.getString("title", "&6Kit Claimed!"));
        String subtitle = ChatColor.translateAlternateColorCodes('&', 
            config.getString("subtitle", ""));
        int fadeIn = config.getInt("fadeIn", 10);
        int stay = config.getInt("stay", 60);
        int fadeOut = config.getInt("fadeOut", 10);

        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    private static void playActionBar(Player player, ConfigurationSection config) {
        if (config == null) return;

        String message = ChatColor.translateAlternateColorCodes('&', 
            config.getString("message", "&a&lKit successfully claimed!"));
        int duration = config.getInt("duration", 60);

        player.spigot().sendMessage(
            ChatMessageType.ACTION_BAR,
            new TextComponent(message)
        );

        // Schedule removal if duration is specified
        if (duration > 0) {
            // Note: In a real implementation, you'd want to schedule this with BukkitRunnable
            // For now, we'll just send the message once
        }
    }

    private static void playSounds(Player player, ConfigurationSection config) {
        if (config == null) return;

        List<Map<?, ?>> sounds = config.getMapList("effects");
        for (Map<?, ?> soundData : sounds) {
            try {
                String soundName = (String) soundData.get("type");
                Object volumeObj = soundData.get("volume");
                Object pitchObj = soundData.get("pitch");
                
                float volume = volumeObj instanceof Number ? ((Number) volumeObj).floatValue() : 1.0f;
                float pitch = pitchObj instanceof Number ? ((Number) pitchObj).floatValue() : 1.0f;

                player.playSound(player.getLocation(), soundName, volume, pitch);
            } catch (Exception e) {
                // Skip invalid sound types
            }
        }
    }

    public static void createDefaultParticleConfig(ConfigurationSection kitSection) {
        ConfigurationSection particleSection = kitSection.createSection("particles");
        
        // Enable particles by default
        particleSection.set("enabled", true);
        
        // Default particle effects
        List<Map<String, Object>> particleEffects = List.of(
            Map.of(
                "type", "EXPLOSION",
                "count", 8,
                "offsetX", 0.5,
                "offsetY", 0.5,
                "offsetZ", 0.5,
                "speed", 0.1
            ),
            Map.of(
                "type", "FIREWORK",
                "count", 16,
                "offsetX", 0.5,
                "offsetY", 0.5,
                "offsetZ", 0.5,
                "speed", 0.05
            ),
            Map.of(
                "type", "FLAME",
                "count", 20,
                "offsetX", 0.3,
                "offsetY", 0.3,
                "offsetZ", 0.3,
                "speed", 0.02
            )
        );
        particleSection.set("effects", particleEffects);

        // Title configuration
        ConfigurationSection titleSection = particleSection.createSection("title");
        titleSection.set("enabled", true);
        titleSection.set("title", "&6Kit Claimed!");
        titleSection.set("subtitle", "");
        titleSection.set("fadeIn", 10);
        titleSection.set("stay", 60);
        titleSection.set("fadeOut", 10);

        // Action bar configuration
        ConfigurationSection actionBarSection = particleSection.createSection("actionbar");
        actionBarSection.set("enabled", true);
        actionBarSection.set("message", "&a&lKit successfully claimed!");
        actionBarSection.set("duration", 60);

        // Sound configuration
        ConfigurationSection soundSection = particleSection.createSection("sounds");
        soundSection.set("enabled", true);
        List<Map<String, Object>> soundEffects = List.of(
            Map.of(
                "type", "ENTITY_PLAYER_LEVELUP",
                "volume", 1.0f,
                "pitch", 1.0f
            )
        );
        soundSection.set("effects", soundEffects);
    }
}
