import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ro.tridentmc.rewardoldplayers.RewardOldPlayers;
import ro.tridentmc.rewardoldplayers.files.Config;

import java.util.List;

public class PlayerJoinListener implements Listener {

    private final RewardOldPlayers plugin;

    private final Config config;

    private String joinMessage;
    private List<String> consoleCommands;
    private int daysLastJoined;
    private List<String> otherMessages;
    private int configuredXpBoost;
    private String xpmessage;


    public PlayerJoinListener(RewardOldPlayers plugin) {
        config = plugin.configYml();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        joinMessage = config.options().getString("join-message");
        consoleCommands = config.options().getStringList("commands");
        daysLastJoined = config.options().getInt("days");
        otherMessages = config.options().getStringList("other-messages");
        configuredXpBoost = config.options().getInt("experience-boost.amount");
        xpmessage = config.options().getString("experience-boost.message");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        String joinMessage = this.joinMessage;

        Player p = e.getPlayer();

        if (p.hasPlayedBefore()) {
            long lastPlayed = p.getLastPlayed();
            long currentTime = System.currentTimeMillis();
            long differenceInMillis = currentTime - lastPlayed;
            long differenceInDays = differenceInMillis / (24 * 60 * 60 * 1000);

            if (!(differenceInMillis <= 5000)) {
                joinMessage = joinMessage.replace("%player%", p.getName());
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', joinMessage));
                for (String msg : otherMessages) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                }
                for (String cmd : consoleCommands) {
                    Bukkit.dispatchCommand(this.plugin.getServer().getConsoleSender(), cmd.replace("%player%", p.getName()));
                }
                applyExperienceBoost(p);
            }
        }
    }
    private void applyExperienceBoost(Player player) {
        int currentExperience = player.getTotalExperience();
        int experienceBoostAmount = configuredXpBoost;

        if (xpmessage != null) {
            int newExperience = currentExperience + experienceBoostAmount;
            player.setTotalExperience(newExperience);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', xpmessage));
        } else {
            plugin.getLogger().warning("Experience boost message not set in the configuration!");
        }
    }
}
