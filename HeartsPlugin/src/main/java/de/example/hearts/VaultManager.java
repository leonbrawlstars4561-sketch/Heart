package de.example.hearts;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class VaultManager {
    private Economy economy;
    public boolean setup() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }
    public boolean available() { return economy != null; }
    public double balance(org.bukkit.OfflinePlayer player) { return economy.getBalance(player); }
    public boolean withdraw(org.bukkit.OfflinePlayer player, double amount) {
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
    public String format(double amount) { return economy.format(amount); }
}
