package dvie.waterPearl;

import dev.splityosis.sysengine.configlib.ConfigLib;
import dev.splityosis.sysengine.configlib.configuration.Configuration;
import dev.splityosis.sysengine.configlib.manager.ConfigManager;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WaterPearl extends JavaPlugin implements Configuration, Listener {

    @Getter private ConfigManager configManager;

    @Field public boolean enabled = true;

    private static final Map<UUID, BukkitRunnable> pearls = new ConcurrentHashMap<>();

    @Override
    @SneakyThrows
    public void onEnable() {
        configManager = ConfigLib.createConfigManager(this);
        configManager.registerConfig(this, new File(getDataFolder(), "pearls.yml"));
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPearl(ProjectileLaunchEvent e) {

        if (!enabled)
            return;

        if (!(e.getEntity() instanceof EnderPearl enderPearl))
            return;

        if (!(enderPearl.getShooter() instanceof Player))
            return;

        UUID uuid = enderPearl.getUniqueId();

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                Vector velocity = enderPearl.getVelocity();
                velocity.multiply(0.99).setY(velocity.getY() - 0.03);
                enderPearl.setVelocity(velocity);
            }
        };

        task.runTaskTimer(this, 1L, 1L);
        pearls.put(uuid, task);
    }

    @EventHandler
    public void onLand(ProjectileHitEvent e) {

        if (!enabled)
            return;

        if (!(e.getEntity() instanceof EnderPearl pearl))
            return;

        if (!(pearl.getShooter() instanceof Player))
            return;

        UUID uuid = pearl.getUniqueId();
        BukkitRunnable task = pearls.remove(uuid);
        if (task != null)
            task.cancel();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        if (!enabled)
            return;

        UUID player = e.getPlayer().getUniqueId();
        pearls.entrySet().removeIf(entry -> {
            BukkitRunnable task = entry.getValue();
            boolean shouldRemove = entry.getKey().equals(player);
            if (shouldRemove) {
                task.cancel();
            }
            return shouldRemove;
        });
    }
}