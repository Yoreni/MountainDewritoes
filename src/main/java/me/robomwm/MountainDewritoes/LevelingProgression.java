package me.robomwm.MountainDewritoes;

import me.robomwm.MountainDewritoes.Commands.SetExpFix;
import me.robomwm.usefulutil.UsefulUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 8/14/2017.
 *
 * @author RoboMWM
 */
public class LevelingProgression implements Listener
{
    public LevelingProgression(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Deny use of enchantment table. Make anything else require no levels to "enchant."
     */
    @EventHandler(ignoreCancelled = true)
    private void enchantingIsNo(EnchantItemEvent event)
    {
        if (event.getExpLevelCost() <= 0)
            return;

        if (event.getEnchantBlock().getType() == Material.ENCHANTMENT_TABLE)
        {
            event.setCancelled(true);
            event.getEnchanter().closeInventory();
            return;
        }

        event.setExpLevelCost(0);
    }
    @EventHandler(ignoreCancelled = true)
    private void enchantingIsNo(InventoryOpenEvent event)
    {
        if (event.getInventory().getType() != InventoryType.ENCHANTING)
            return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void levelChangeEvent(PlayerExpChangeEvent event)
    {
        //TODO: track exp (so players don't lose any)
        if (event.getSource() == null)
            return;
        Player player = event.getPlayer();

        int nextLevel = SetExpFix.getExpUntilNextLevel(player);

        if (event.getAmount() >= nextLevel)
        {
            player.sendActionBar("You leveled up(?) to level " + player.getLevel() + 1);
        }
    }
}