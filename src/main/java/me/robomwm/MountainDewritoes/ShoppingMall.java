package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by RoboMWM on 6/1/2016.
 * All things related to shopping in da memetastic mall
 */
public class ShoppingMall implements Listener
{
    MountainDewritoes instance;
    public ShoppingMall(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
    }

    /**
     * Set walking speed when entering or leaving mall
     * @param event
     */
    World mallWorld = Bukkit.getWorld("mall");
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onWorldChange(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        //Reset speed when leaving mall
        if (event.getFrom().equals(mallWorld))
        {
            player.setWalkSpeed(0.2f);
            return;
        }

        //Increase speed when entering mall
        if (player.getWorld().equals(mallWorld))
        {
            player.setWalkSpeed(0.5f);
        }
    }

    /**
     * Set walking speed if player joins inside mall
     */
    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerJoinInMall(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        new BukkitRunnable()
        {
            public void run()
            {
                if (player.getWorld().equals(mallWorld))
                    player.setWalkSpeed(0.5f);
            }
        }.runTaskLater(instance, 2L);
    }
}
