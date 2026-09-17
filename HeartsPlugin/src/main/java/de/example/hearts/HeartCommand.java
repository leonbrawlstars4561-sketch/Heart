package de.example.hearts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class HeartCommand implements org.bukkit.command.CommandExecutor, Listener {
    private final HeartsPlugin plugin;
    public HeartCommand(HeartsPlugin plugin){this.plugin=plugin;}
    public void open(Player p){Inventory inv=org.bukkit.Bukkit.createInventory(null,27,plugin.msgComponent("gui.title"));inv.setItem(10,item(Material.RED_DYE,"gui.cancel-name","gui.cancel-lore"));inv.setItem(16,item(Material.DIAMOND,"gui.confirm-name","gui.confirm-lore"));p.openInventory(inv);}
    private ItemStack item(Material m,String name,String lore){ItemStack i=new ItemStack(m);ItemMeta meta=i.getItemMeta();meta.displayName(plugin.msgComponent(name));meta.lore(plugin.getConfig().getStringList(lore).stream().map(s->s.replace("%price%",plugin.formatPrice())).map(plugin::color).map(LegacyComponentSerializer.legacySection()::deserialize).toList());i.setItemMeta(meta);return i;}
    @Override public boolean onCommand(org.bukkit.command.CommandSender s,org.bukkit.command.Command c,String label,String[] args){if(!(s instanceof Player p)){s.sendMessage("Nur Spieler können /heart verwenden.");return true;}open(p);return true;}
    @EventHandler public void click(InventoryClickEvent e){if(!(e.getWhoClicked() instanceof Player p)||!e.getView().title().equals(plugin.msgComponent("gui.title")))return;e.setCancelled(true);if(e.getRawSlot()==10){p.closeInventory();return;}if(e.getRawSlot()!=16)return;if(!plugin.getVault().available()){p.sendMessage(plugin.msg("no-economy"));return;}int h=plugin.getHeartManager().get(p),max=plugin.getConfig().getInt("max-hearts",20);double price=plugin.getConfig().getDouble("heart-price",10_000_000_000D);if(h>=max){p.sendMessage(plugin.msg("purchase-max").replaceText(Component.text("%max%"),Component.text(String.valueOf(max))));return;}if(plugin.getVault().balance(p)<price){p.sendMessage(plugin.msg("purchase-no-money").replaceText(Component.text("%price%"),Component.text(plugin.formatPrice())));return;}if(plugin.getVault().withdraw(p,price)){plugin.getHeartManager().give(p,1);p.sendMessage(plugin.msg("purchase-success").replaceText(Component.text("%price%"),Component.text(plugin.formatPrice())).replaceText(Component.text("%hearts%"),Component.text(String.valueOf(h+1))));p.closeInventory();}}
    @EventHandler public void drag(InventoryDragEvent e){if(e.getView().title().equals(plugin.msgComponent("gui.title")))e.setCancelled(true);}
}
