package de.example.hearts;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HeartDisplay {
    private final HeartsPlugin plugin;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public HeartDisplay(HeartsPlugin plugin) { this.plugin = plugin; }

    public void update(Player player, int hearts) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("hearts", Criteria.DUMMY,
                Component.text(plugin.getConfig().getString("display.title", "❤ Herzen").replace('&', '§')));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        int max = Math.max(1, plugin.getConfig().getInt("max-hearts", 20));
        if (max == 4) {
            objective.getScore("§f  " + heart(hearts >= 1)).setScore(2);
            objective.getScore(heart(hearts >= 2) + heart(hearts >= 3) + heart(hearts >= 4)).setScore(1);
        } else {
            int rows = (max + 4) / 5;
            for (int row = rows - 1; row >= 0; row--) {
                StringBuilder line = new StringBuilder();
                int start = row * 5 + 1, end = Math.min(max, start + 4);
                for (int i = start; i <= end; i++) line.append(heart(hearts >= i));
                objective.getScore(line.toString()).setScore(row + 1);
            }
        }
        boards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
    }

    private String heart(boolean active) { return (active ? ChatColor.BLUE : ChatColor.GRAY) + "♥ "; }
    public void clear(Player player) { boards.remove(player.getUniqueId()); player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard()); }
}
