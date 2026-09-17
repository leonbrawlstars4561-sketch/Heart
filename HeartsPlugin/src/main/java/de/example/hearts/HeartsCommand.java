
package de.example.hearts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.BanList;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public final class HeartsCommand implements CommandExecutor {

    private final HeartsPlugin plugin;

    public HeartsCommand(HeartsPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean admin(CommandSender s) {
        if (!s.hasPermission("hearts.admin")) {
            s.sendMessage(plugin.msg("admin-only"));
            return false;
        }
        return true;
    }

    private Player player(CommandSender s, String name) {
        Player p = Bukkit.getPlayerExact(name);

        if (p == null) {
            s.sendMessage(plugin.msg("player-not-found"));
        }

        return p;
    }

    private Integer number(CommandSender s, String raw) {
        try {
            int n = Integer.parseInt(raw);

            if (n < 0) {
                s.sendMessage(plugin.msg("negative"));
                return null;
            }

            return n;

        } catch (NumberFormatException e) {
            s.sendMessage(plugin.msg("invalid-number"));
            return null;
        }
    }

    private Component replace(Component message, String placeholder, String value) {
        return message.replaceText(
                TextReplacementConfig.builder()
                        .matchLiteral(placeholder)
                        .replacement(value)
                        .build()
        );
    }

    @Override
    public boolean onCommand(
            CommandSender s,
            Command c,
            String label,
            String[] a
    ) {

        if (!admin(s)) {
            return true;
        }

        if (a.length == 0) {
            s.sendMessage(plugin.msg("usage"));
            return true;
        }

        switch (a[0].toLowerCase()) {

            case "set" -> {

                if (a.length != 3) {
                    s.sendMessage(plugin.msg("usage"));
                    return true;
                }

                Player p = player(s, a[1]);
                Integer n = number(s, a[2]);

                if (p == null || n == null) {
                    return true;
                }

                if (n == 0) {

                    plugin.getHeartManager().ban(p);

                    Bukkit.getBanList(BanList.Type.NAME).addBan(
                            p.getName(),
                            "0 Herzen",
                            null,
                            "HeartsPlugin"
                    );

                    p.kick(plugin.msg("banned"));

                } else {

                    plugin.getHeartManager().set(p, n);
                }

                Component message = plugin.msg("set-success");
                message = replace(message, "%player%", p.getName());
                message = replace(message, "%hearts%", String.valueOf(n));

                s.sendMessage(message);
            }

            case "give", "take" -> {

                if (a.length != 3) {
                    s.sendMessage(plugin.msg("usage"));
                    return true;
                }

                Player p = player(s, a[1]);
                Integer n = number(s, a[2]);

                if (p == null || n == null) {
                    return true;
                }

                if (a[0].equals("give")) {

                    int before = plugin.getHeartManager().get(p);

                    plugin.getHeartManager().give(p, n);

                    int amount =
                            plugin.getHeartManager().get(p) - before;

                    Component message = plugin.msg("give-success");
                    message = replace(message, "%player%", p.getName());
                    message = replace(message, "%amount%", String.valueOf(amount));

                    s.sendMessage(message);

                } else {

                    int before = plugin.getHeartManager().get(p);

                    plugin.getHeartManager().take(p, n);

                    int amount =
                            before - plugin.getHeartManager().get(p);

                    Component message = plugin.msg("take-success");
                    message = replace(message, "%player%", p.getName());
                    message = replace(message, "%amount%", String.valueOf(amount));

                    s.sendMessage(message);
                }
            }

            case "get" -> {

                if (a.length != 2) {
                    s.sendMessage(plugin.msg("usage"));
                    return true;
                }

                Player p = player(s, a[1]);

                if (p != null) {

                    Component message = plugin.msg("get-success");
                    message = replace(message, "%player%", p.getName());
                    message = replace(
                            message,
                            "%hearts%",
                            String.valueOf(plugin.getHeartManager().get(p))
                    );

                    s.sendMessage(message);
                }
            }

            case "reload" -> {

                plugin.reloadConfig();
                plugin.getVault().setup();

                for (Player p : Bukkit.getOnlinePlayers()) {
                    plugin.getHeartManager().apply(p);
                }

                s.sendMessage(plugin.msg("reload-success"));
            }

            case "ban" -> {

                if (a.length != 2) {
                    s.sendMessage(plugin.msg("usage"));
                    return true;
                }

                Player p = player(s, a[1]);

                if (p != null) {

                    plugin.getHeartManager().ban(p);

                    Bukkit.getBanList(BanList.Type.NAME).addBan(
                            p.getName(),
                            "Admin-Ban",
                            null,
                            "HeartsPlugin"
                    );

                    p.kick(plugin.msg("banned"));

                    Component message = plugin.msg("banned-success");
                    message = replace(message, "%player%", p.getName());

                    s.sendMessage(message);
                }
            }

            case "unban" -> {

                if (a.length != 2) {
                    s.sendMessage(plugin.msg("usage"));
                    return true;
                }

                String name = a[1];
                Player p = Bukkit.getPlayerExact(name);

                if (p != null) {

                    plugin.getHeartManager().unban(p);

                } else {

                    s.sendMessage(plugin.msg("player-not-found"));
                    return true;
                }

                Bukkit.getBanList(BanList.Type.NAME).pardon(name);

                Component message = plugin.msg("unbanned-success");
                message = replace(message, "%player%", name);

                s.sendMessage(message);
            }

            default -> s.sendMessage(plugin.msg("usage"));
        }

        return true;
    }
}

