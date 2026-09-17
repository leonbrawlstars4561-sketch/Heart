package de.example.hearts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class HeartsPlugin extends JavaPlugin implements Listener {
    private DatabaseManager database;
    private HeartManager heartManager;
    private VaultManager vault;
    private HeartDisplay display;

    @Override public void onEnable(){
        saveDefaultConfig();
        database=new DatabaseManager(this); display=new HeartDisplay(this); vault=new VaultManager();
        try{database.open();}catch(Exception e){getLogger().severe("SQLite konnte nicht geöffnet werden: "+e.getMessage());getServer().getPluginManager().disablePlugin(this);return;}
        vault.setup();
        if(!vault.available())getLogger().warning("Vault/Economy nicht verfügbar. Geldfunktionen sind deaktiviert.");
        heartManager=new HeartManager(this,database);
        HeartCommand gui=new HeartCommand(this);
        getCommand("heart").setExecutor(gui); getCommand("hearts").setExecutor(new HeartsCommand(this));
        Bukkit.getPluginManager().registerEvents(gui,this); Bukkit.getPluginManager().registerEvents(new PvPManager(this),this); Bukkit.getPluginManager().registerEvents(this,this);
        for(Player p:Bukkit.getOnlinePlayers())try{heartManager.load(p);}catch(Exception e){getLogger().severe("Spielerdaten konnten nicht geladen werden: "+e.getMessage());}
    }
    @Override public void onDisable(){if(heartManager!=null)for(Player p:Bukkit.getOnlinePlayers())heartManager.save(p);if(database!=null)database.close();}

    @EventHandler public void login(PlayerLoginEvent e){
        try{var d=database.load(e.getPlayer().getUniqueId(),getConfig().getInt("starting-hearts",4));if(d.banned()){e.disallow(PlayerLoginEvent.Result.KICK_BANNED,msg("banned"));Bukkit.getBanList(BanList.Type.NAME).addBan(e.getPlayer().getName(),"0 Herzen",null,"HeartsPlugin");}}
        catch(Exception ex){e.disallow(PlayerLoginEvent.Result.KICK_OTHER,"Datenbankfehler. Bitte später erneut versuchen.");getLogger().severe("SQLite login load failed: "+ex.getMessage());}
    }
    @EventHandler public void join(PlayerJoinEvent e){try{heartManager.load(e.getPlayer());}catch(Exception ex){getLogger().severe("Join load failed: "+ex.getMessage());}}
    @EventHandler public void quit(PlayerQuitEvent e){heartManager.unload(e.getPlayer());display.clear(e.getPlayer());}
    @EventHandler public void respawn(PlayerRespawnEvent e){Bukkit.getScheduler().runTask(this,()->heartManager.apply(e.getPlayer()));}

    public HeartManager getHeartManager(){return heartManager;}
    public VaultManager getVault(){return vault;}
    public HeartDisplay getDisplay(){return display;}
    public Component msg(String key){return msgComponent("messages."+key);}
    public Component msgComponent(String path){return LegacyComponentSerializer.legacyAmpersand().deserialize(getConfig().getString(path,""));}
    public String color(String s){return s==null?"":s.replace('&','§');}
    public String formatPrice(){double d=getConfig().getDouble("heart-price",10_000_000_000D);return String.format("%,.0f",d).replace(',', '.');}
}
