package me.robomwm.MountainDewritoes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

/**
 * Created by RoboMWM on 11/5/2016.
 * Provides jukeboxes to play "additional tracks"
 * Does NOT use AtmosphericManager's metadata system
 * (i.e. retaining vanilla behavior)
 */
public class JukeboxManager implements Listener
{
    MountainDewritoes instance;

    public JukeboxManager(MountainDewritoes mountainDewritoes)
    {
        instance = mountainDewritoes;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerInteractJukebox(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        Material disc = event.getMaterial();
        Block block = event.getClickedBlock();

        if (event.getClickedBlock().getType() != Material.JUKEBOX)
            return;

        Jukebox jukebox = (Jukebox)event.getClickedBlock().getState();
        List<MetadataValue> blockMetadata = block.getMetadata("SONG");

        Location loc = block.getLocation();

        //If there's already a disc in here, eject it and stop playing
        if (jukebox.eject())
        {
            //Don't stop sounds if... we didn't start the sound...
            if (!block.hasMetadata("SONG"))
                return;
            instance.getLogger().info("stopsound @a[x=" + loc.getBlockX() + ",y=" + loc.getBlockY() + ",z=" + loc.getBlockZ() + ",r=100] record " + blockMetadata.get(0).asString());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stopsound @a[x=" + loc.getBlockX() + ",y=" + loc.getBlockY() + ",z=" + loc.getBlockZ() + ",r=100] record " + blockMetadata.get(0).asString());
            block.removeMetadata("SONG", instance);
            return;
        }

        if (!disc.isRecord())
            return;

        //Otherwise, let's play a song, yay
        String songToPlay = null;


        switch(disc)
        {
            case GOLD_RECORD:
                songToPlay = "custom-stuff";
                break;
            case GREEN_RECORD:
            case RECORD_3:
            case RECORD_4:
            case RECORD_5:
            case RECORD_6:
            case RECORD_7:
            case RECORD_8:
            case RECORD_9:
            case RECORD_10:
            case RECORD_11:
            case RECORD_12:
                break;
        }

        if (songToPlay == null)
            return;

        block.setMetadata("SONG", new FixedMetadataValue(instance, songToPlay));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "playsound " + songToPlay + " record @a " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " 4");
    }
}