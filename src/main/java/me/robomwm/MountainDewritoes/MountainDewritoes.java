package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.Sounds.HitSound;
import me.robomwm.MountainDewritoes.Sounds.LowHealth;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Robo on 2/13/2016.
 */
public class MountainDewritoes extends JavaPlugin implements Listener
{
    Set<Player> usedEC = new HashSet<>();
    Map<Player, Integer> usingTitlePlayers = new HashMap<>();
    public void onEnable()
    {
        //Modifies PlayerListName and prefixes
        getServer().getPluginManager().registerEvents(new SimpleClansListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new LongFallBoots(), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new BetterZeldaHearts(), this);
        getServer().getPluginManager().registerEvents(new RandomStructurePaster(this), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new RandomTipOfTheDay(this), this);
        //getServer().getPluginManager().registerEvents(new SecondWind(this), this);
        getServer().getPluginManager().registerEvents(new ShoppingMall(), this);
        getServer().getPluginManager().registerEvents(new LowHealth(this), this);
        getServer().getPluginManager().registerEvents(new TeleportingEffects(this), this);
        getServer().getPluginManager().registerEvents(new HitSound(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
    }

    //Warn new players that /ec costs money to use
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    void onPlayerPreprocess(PlayerCommandPreprocessEvent event)
    {
        //Check if player is attempting to access enderchest via command
        String message = event.getMessage().toLowerCase();
        if (!message.equalsIgnoreCase("/ec") && !message.equalsIgnoreCase("/pv") && !message.equalsIgnoreCase("/echest"))
            return;

        Player player = event.getPlayer();
        //If player isn't new or if we've already warned this player before...
        if (player.hasPlayedBefore() || usedEC.contains(player))
        {
            player.sendMessage(ChatColor.GREEN + "Charged 1337 dogecoins to provide access to your enderchest via command.");
            return;
        }
        player.sendMessage(ChatColor.GOLD + "Accessing the enderchest via a slash command costs 1337 dogecoins. To confirm, type /ec again.");
        event.setCancelled(true);
        usedEC.add(player);
    }

    public boolean isUsingTitle(Player player)
    {
        return usingTitlePlayers.containsKey(player);
    }
    public void addUsingTitle(Player player, Long ticks)
    {
        int index = 0;
        if (isUsingTitle(player))
            index += usingTitlePlayers.get(player);
        final int finalIndex = index;
        usingTitlePlayers.put(player, index);
        new BukkitRunnable()
        {
            public void run()
            {
                if (!isUsingTitle(player))
                    return;
                if (usingTitlePlayers.get(player) == finalIndex)
                    usingTitlePlayers.remove(player);
                //Otherwise, another addUsingTitle had overrided our previous addUsingTitle invokation
            }
        }.runTaskLater(this, ticks);
    }
}
