package ro.tridentmc.rewardoldplayers.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ro.tridentmc.rewardoldplayers.RewardOldPlayers;
import ro.tridentmc.rewardoldplayers.files.Config;

import java.util.List;
import java.util.Optional;

public class PlayerJoinListener implements Listener {

    private final RewardOldPlayers plugin;

    private final Config config;

    private String joinMessage;
    private List<String> consoleCommands;
    private int daysLastJoined;
    private List<String> otherMessages;
    private int configuredXpBoost;
    private String xpmessage;
    private Boolean enablexp;
    private Boolean enableplugin;
    private Boolean enableplaytime;
    private String playtimeMessageR;





    public PlayerJoinListener(RewardOldPlayers plugin) {
        config = plugin.configYml();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        enableplugin = config.options().getBoolean("enable");
        joinMessage = config.options().getString("join-message");
        consoleCommands = config.options().getStringList("commands");
        daysLastJoined = config.options().getInt("days");
        otherMessages = config.options().getStringList("other-messages");
        enablexp = config.options().getBoolean("experience-boost.enable");
        configuredXpBoost = config.options().getInt("experience-boost.amount");
        xpmessage = config.options().getString("experience-boost.message");
        enableplaytime = config.options().getBoolean("playtime-rewards.enable");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        if (enableplugin){
            String joinMessage = this.joinMessage;
            Player p = e.getPlayer();

            if (p.hasPlayedBefore()) {
                long lastPlayed = p.getLastPlayed();
                long currentTime = System.currentTimeMillis();
                long differenceInMillis = currentTime - lastPlayed;
                long differenceInDays = differenceInMillis / (24 * 60 * 60 * 1000);

                if (!(differenceInMillis <= 5000)) {
                    joinMessage = joinMessage.replace("%player%", p.getName());
                    joinMessage = joinMessage.replace("%days%", String.valueOf(differenceInDays));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', joinMessage));
                    for (String msg : otherMessages) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                    }
                    for (String cmd : consoleCommands) {
                        Bukkit.dispatchCommand(this.plugin.getServer().getConsoleSender(), cmd.replace("%player%", p.getName()));
                    }
                    applyExperienceBoost(p);
                    applyPlayTimeRewards(p);
                }
            }
        }
    }
    private void applyExperienceBoost(Player player) {
        if (enablexp) {
            if (player != null) {
                int currentExperience = player.getTotalExperience();
                int experienceBoostAmount = configuredXpBoost;

                if (xpmessage != null) {
                    int newExperience = currentExperience + experienceBoostAmount;
                    player.giveExp(newExperience);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', xpmessage));
                } else {
                    plugin.getLogger().warning("Experience boost message not set in the configuration!");
                }
            }
        } else {
            plugin.getLogger().warning("Not giving experience boost, disabled in config. ");
        }
    }

    private void applyPlayTimeRewards(Player player) {
        long playTime = player.getStatistic(Statistic.TOTAL_WORLD_TIME);

        if (enableplaytime) {
            int playtimeInHours = (int) (playTime / (60 * 60 * 20)); // Convert to hours

            for (int i = 1; ; i++) {

                if (config.options().isSet("playtime-rewards." + i)) {
                    int requiredPlaytime = config.options().getInt("playtime-rewards." + i + ".required-hours");


                    if (playtimeInHours >= requiredPlaytime) {

                        List<String> rewardCommands = config.options().getStringList("playtime-rewards." + i + ".commands");

                        for (String cmd : rewardCommands) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                        }

                        playtimeMessageR = config.options().getString("playtime-rewards." + i + ".message");

                        if (!(playtimeMessageR == null)){
                            playtimeMessageR = playtimeMessageR.replace("%hours%", Integer.toString(playtimeInHours));
                            playtimeMessageR = playtimeMessageR.replace("%player%", player.getName());
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', playtimeMessageR));
                        }
                    }
                } else {
                    // No more playtime rewards configurations, exit the loop
                    break;
                }
            }
        }
    }

}


