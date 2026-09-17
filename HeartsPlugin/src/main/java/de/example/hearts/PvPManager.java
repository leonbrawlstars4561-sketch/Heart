package de.example.hearts;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PvPManager implements Listener {
    private final HeartsPlugin plugin;
    private final Map<UUID, Hit> hits = new ConcurrentHashMap<>();
    private record Hit(UUID attacker, long time) {}
    public PvPManager(HeartsPlugin plugin) { this.plugin=plugin; }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        Player attacker = resolvePlayer(e.getDamager());
        if (attacker == null || attacker.getUniqueId().equals(victim.getUniqueId())) return;
        hits.put(victim.getUniqueId(), new Hit(attacker.getUniqueId(), System.currentTimeMillis()));
    }

    private Player resolvePlayer(Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player p) return p;
        return null;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        Player victim=e.getPlayer();
        Hit hit=hits.get(victim.getUniqueId());
        long timeout=plugin.getConfig().getLong("pvp-timeout",10)*1000L;
        boolean pvp=hit != null && System.currentTimeMillis()-hit.time() <= timeout && isPvPFinalDamage(victim);
        hits.remove(victim.getUniqueId());
        if (!pvp) return;
        plugin.getHeartManager().handlePvPDeath(victim);
    }

    private boolean isPvPFinalDamage(Player p) {
        EntityDamageEvent last=p.getLastDamageCause();
        return last instanceof EntityDamageByEntityEvent && resolvePlayer(((EntityDamageByEntityEvent) last).getDamager()) != null;
    }
}
