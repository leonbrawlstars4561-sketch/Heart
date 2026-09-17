package de.example.hearts;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HeartManager {
    private final HeartsPlugin plugin;
    private final DatabaseManager database;
    private final Map<UUID,Integer> hearts = new ConcurrentHashMap<>();
    private final Map<UUID,Boolean> banned = new ConcurrentHashMap<>();

    public HeartManager(HeartsPlugin plugin, DatabaseManager database) { this.plugin=plugin; this.database=database; }
    public void load(Player p) throws SQLException { var d=database.load(p.getUniqueId(), plugin.getConfig().getInt("starting-hearts",4)); hearts.put(p.getUniqueId(),d.hearts()); banned.put(p.getUniqueId(),d.banned()); apply(p); }
    public void unload(Player p){ save(p); hearts.remove(p.getUniqueId()); banned.remove(p.getUniqueId()); }
    public void save(Player p){ try{database.save(p.getUniqueId(),get(p),isBanned(p));}catch(SQLException e){plugin.getLogger().severe("SQLite save failed: "+e.getMessage());} }
    public int get(Player p){return hearts.getOrDefault(p.getUniqueId(),plugin.getConfig().getInt("starting-hearts",4));}
    public boolean isBanned(Player p){return banned.getOrDefault(p.getUniqueId(),false);}
    public void set(Player p,int amount){int max=plugin.getConfig().getInt("max-hearts",20); amount=Math.max(0,Math.min(max,amount)); hearts.put(p.getUniqueId(),amount); if(amount>0)banned.put(p.getUniqueId(),false); apply(p); save(p);}
    public void give(Player p,int amount){set(p,get(p)+amount);}
    public void take(Player p,int amount){set(p,get(p)-amount);}
    public void ban(Player p){hearts.put(p.getUniqueId(),0); banned.put(p.getUniqueId(),true); save(p);}
    public void unban(Player p){banned.put(p.getUniqueId(),false); save(p);}
    public void apply(Player p){double max=Math.max(2.0,get(p)*2.0);var attr=p.getAttribute(Attribute.MAX_HEALTH);if(attr!=null){attr.setBaseValue(max);if(p.getHealth()>max)p.setHealth(max);}plugin.getDisplay().update(p,get(p));}

    public void handlePvPDeath(Player p){
        int h=get(p);
        if(h>1){ set(p,h-1); return; }
        double price=plugin.getConfig().getDouble("heart-price",10_000_000_000D);
        if(plugin.getVault().available() && plugin.getVault().balance(p)>=price){
            if(plugin.getVault().withdraw(p,price)){
                set(p,1);
                p.sendMessage(plugin.msg("paid-life"));
                return;
            }
        }
        ban(p);
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(p.getName(), "0 Herzen", null, "HeartsPlugin");
        p.sendMessage(plugin.msg("banned"));
        Bukkit.getScheduler().runTask(plugin, () -> p.kick(plugin.msg("banned")));
    }
}
