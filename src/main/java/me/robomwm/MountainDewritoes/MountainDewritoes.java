package me.robomwm.MountainDewritoes;

import com.reilaos.bukkit.TheThuum.shouts.ShoutAreaOfEffectEvent;
import com.robomwm.customitemrecipes.CustomItemRecipes;
import com.robomwm.grandioseapi.GrandioseAPI;
import info.gomeow.chester.Chester;
import me.robomwm.MountainDewritoes.Commands.ClearChatCommand;
import me.robomwm.MountainDewritoes.Commands.DebugCommand;
import me.robomwm.MountainDewritoes.Commands.EmoticonCommands;
import me.robomwm.MountainDewritoes.Commands.Emoticons;
import me.robomwm.MountainDewritoes.Commands.NickCommand;
import me.robomwm.MountainDewritoes.Commands.PseudoCommands;
import me.robomwm.MountainDewritoes.Commands.ResetCommands;
import me.robomwm.MountainDewritoes.Commands.StaffRestartCommand;
import me.robomwm.MountainDewritoes.Commands.TipCommand;
import me.robomwm.MountainDewritoes.Commands.ViewDistanceCommand;
import me.robomwm.MountainDewritoes.Commands.VoiceCommand;
import me.robomwm.MountainDewritoes.Commands.WarpCommand;
import me.robomwm.MountainDewritoes.Events.ReverseOsmosis;
import me.robomwm.MountainDewritoes.Music.AtmosphericManager;
import me.robomwm.MountainDewritoes.NotOverwatch.Ogrewatch;
import me.robomwm.MountainDewritoes.Rewards.LodsOfEmone;
import me.robomwm.MountainDewritoes.Sounds.HitSound;
import me.robomwm.MountainDewritoes.Sounds.LowHealth;
import me.robomwm.MountainDewritoes.Sounds.ReplacementSoundEffects;
import me.robomwm.MountainDewritoes.armor.ArmorAugmentation;
import net.milkbowl.vault.economy.Economy;
import net.minecrell.serverlistplus.core.ServerListPlusCore;
import net.minecrell.serverlistplus.core.player.PlayerIdentity;
import net.minecrell.serverlistplus.core.replacement.LiteralPlaceholder;
import net.minecrell.serverlistplus.core.replacement.ReplacementManager;
import net.minecrell.serverlistplus.core.status.StatusResponse;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jibble.jmegahal.JMegaHal;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolType;
import protocolsupport.api.ProtocolVersion;
import pw.valaria.bookutil.BookUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * Created by RoboMWM on 2/13/2016.
 */
public class MountainDewritoes extends JavaPlugin implements Listener
{
    //metadata "API" that allows us to clear metadata onDisable
    private Map<String, Set<Metadatable>> usedMetadata = new HashMap<>();
    public void setMetadata(Metadatable target, String key, Object value)
    {
        if (!usedMetadata.containsKey(key))
            usedMetadata.put(key, new HashSet<>());
        usedMetadata.get(key).add(target);
        target.setMetadata(key, new FixedMetadataValue(this, value));
    }

    //Set<Player> usedEC = new HashSet<>();
    //Pattern ec = Pattern.compile("\\bec\\b|\\bechest\\b|\\bpv\\b");
    private long currentTick = 0L; //"Server time in ticks"
    private Set<World> safeWorlds = new HashSet<>();
    private Set<World> survivalWorlds = new HashSet<>();
    private Set<World> minigameWorlds = new HashSet<>();
    private Set<World> noModifyWorld = new HashSet<>();
    private FileConfiguration newConfig;
    private Economy economy;
    private boolean serverDoneLoading = false;

    public long getCurrentTick()
    {
        return currentTick;
    }

    public boolean isSurvivalWorld(World world)
    {
        return survivalWorlds.contains(world);
    }

    //Currently only used to bypass teleport warmup
    public boolean isSafeWorld(World world)
    {
        return safeWorlds.contains(world);
    }

    public boolean isMinigameWorld(World world)
    {
        return minigameWorlds.contains(world);
    }

    public boolean isNoModifyWorld(World world)
    {
        return noModifyWorld.contains(world);
    }

    public void registerListener(Listener listener)
    {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    //Class instances used in onDisable/other classes
    private BetterNoDamageTicks betterNoDamageTicks;
    private TitleManager titleManager;
    private SimpleClansListener simpleClansListener;
    private TipCommand tipCommand;
    private AtmosphericManager atmosphericManager;
    private CustomItemRecipes customItemRecipes;
    private BookUtil bookUtil;

    public GrandioseAPI getGrandioseAPI()
    {
        return (GrandioseAPI)getServer().getPluginManager().getPlugin("GrandioseAPI");
    }

    public BookUtil getBookUtil()
    {
        return bookUtil;
    }

    public CustomItemRecipes getCustomItemRecipes()
    {
        return customItemRecipes;
    }

    public TipCommand getTipCommand()
    {
        return tipCommand;
    }

    public TitleManager getTitleManager()
    {
        return titleManager;
    }

    public SimpleClansListener getSimpleClansListener()
    {
        return simpleClansListener;
    }

    public Economy getEconomy()
    {
        return economy;
    }

    @Override
    public FileConfiguration getConfig()
    {
        if(this.newConfig == null)
            this.reloadConfig();
        return this.newConfig;
    }

    @Override
    public void reloadConfig()
    {
        newConfig = new YamlConfiguration();
        newConfig.options().pathSeparator('|'); //Literally had to override these config-related members in JavaPlugin just to do this -_-
        try
        {
            newConfig.load(new File(getDataFolder(), "config.yml"));
        }
        catch (FileNotFoundException ignored) {}
        catch (IOException | InvalidConfigurationException var4)
        {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load MountainDewritoes config.yml", var4);
        }
    }

    private boolean setupEconomy(JavaPlugin plugin)
    {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public void onLoad()
    {
        try
        {
            ReplacementManager.getDynamic().add(new LiteralPlaceholder("%bot%")
            {
                private JMegaHal brain;

                @Override
                public String replace(ServerListPlusCore core, String s)
                {
                    if (!serverDoneLoading)
                        return ChatColor.RED + "still brewing memes, pls w8.";
                    if (brain == null)
                        brain = ((Chester)getServer().getPluginManager().getPlugin("Chester")).getHal();
                    ChatColor color = TipCommand.getRandomColor();
                    return "U_W0T_B0T: " + color + brain.getSentence();
                }

                @Override
                public String replace(StatusResponse response, String s)
                {
                    if (!serverDoneLoading)
                        return ChatColor.RED + "still brewing memes, pls w8.";
                    if (brain == null)
                        brain = ((Chester)getServer().getPluginManager().getPlugin("Chester")).getHal();
                    ChatColor color = TipCommand.getRandomColor();
                    if (response.getRequest().getIdentity() == null)
                        return "U_W0T_B0T: " + color + brain.getSentence();

                    PlayerIdentity identity = response.getRequest().getIdentity();

                    switch (ThreadLocalRandom.current().nextInt(10))
                    {
                        case 0:
                            return color + getEconomy().format(getEconomy().getBalance(getServer().getOfflinePlayer(identity.getUuid())));
                        case 1:
                            if (getServer().getOnlinePlayers().size() > 0)
                                return "U shuld join " + getServer().getOnlinePlayers().iterator().next().getDisplayName();
                            break;
                        case 2:
                            return color + brain.getSentence("robo");
                    }
                    return "U_WOT_BOT: " + color + brain.getSentence(response.getRequest().getIdentity().getName());

                }
            });
        }
        catch (Throwable rock)
        {
            this.getLogger().warning("ServerListPlus must not exist or something.");
        }
    }

    public void onEnable()
    {
        setupEconomy(this);
        tipCommand = new TipCommand(this);
        customItemRecipes = (CustomItemRecipes)getServer().getPluginManager().getPlugin("CustomItemRecipes");
        bookUtil = new BookUtil(this);

        //Initialize commonly-used sets
        safeWorlds.add(getServer().getWorld("mall"));
        safeWorlds.add(getServer().getWorld("spawn"));
        safeWorlds.add(getServer().getWorld("prison"));
        safeWorlds.add(getServer().getWorld("firstjoin"));

        survivalWorlds.add(getServer().getWorld("mall"));
        survivalWorlds.add(getServer().getWorld("prison"));
        survivalWorlds.add(getServer().getWorld("firstjoin"));
        survivalWorlds.add(getServer().getWorld("world"));
        survivalWorlds.add(getServer().getWorld("world_nether"));
        survivalWorlds.add(getServer().getWorld("world_the_end"));
        survivalWorlds.add(getServer().getWorld("cityworld"));
        survivalWorlds.add(getServer().getWorld("cityworld_nether"));
        survivalWorlds.add(getServer().getWorld("maxiworld"));
        survivalWorlds.add(getServer().getWorld("wellworld"));

        //Set border on survival worlds
        for (World world : survivalWorlds)
        {
            if (!safeWorlds.contains(world))
                world.getWorldBorder().setSize(20000);
        }

        //Don't keep spawn chunks in memory
        for (World world : getServer().getWorlds())
        {
            world.setKeepSpawnInMemory(false);
        }

        minigameWorlds.add(getServer().getWorld("spawn"));
        minigameWorlds.add(getServer().getWorld("minigames"));
        minigameWorlds.add(getServer().getWorld("bam"));
        minigameWorlds.add(getServer().getWorld("flatroom"));
        minigameWorlds.add(getServer().getWorld("CreativeParkourMaps"));
        minigameWorlds.add(getServer().getWorld("dogepvp"));

        noModifyWorld.add(getServer().getWorld("CreativeParkourMaps"));

        //Classes other classes might want to use
        new NSA(this);

        //Wow, lots-o-listeners
        PluginManager pm = getServer().getPluginManager();
        SimpleClans sc = (SimpleClans) Bukkit.getPluginManager().getPlugin("SimpleClans");
        ClanManager clanManager = sc.getClanManager();
        pm.registerEvents(this, this);
        pm.registerEvents(new ChatListener(this, clanManager), this);
        pm.registerEvents(new DeathListener(this), this);
        new BetterZeldaHearts(this, economy);
        new RandomStructurePaster(this);
        new JoinMessages(this);
        pm.registerEvents(new ShoppingMall(this), this);
        pm.registerEvents(new LowHealth(this), this);
        pm.registerEvents(new HitSound(this), this);
        new GamemodeInventoryManager(this);
        pm.registerEvents(new NoKnockback(this), this);
        new SleepManagement(this);

        new ReverseOsmosis(this);
        simpleClansListener = new SimpleClansListener(this, clanManager);
        new ReplacementSoundEffects(this);
        new Ogrewatch(this);
        betterNoDamageTicks = new BetterNoDamageTicks(this);
        new FineSine(this);
        new LodsOfEmone(this);
        new PseudoCommands(this);
        new TabList(this);
        new TheMidnightPortalToAnywhere(this);
        atmosphericManager = new AtmosphericManager(this);
        new ArmorAugmentation(this);
        new AntiLag(this);
        new FirstJoin(this);

        //Plugin-dependent listeners
        if (getServer().getPluginManager().getPlugin("BetterTPA") != null && getServer().getPluginManager().getPlugin("BetterTPA").isEnabled())
            pm.registerEvents(new TeleportingEffects(this), this);

        //Utilities
        new ScoreboardStuff(this);
        new BukkitRunnable()
        {
            public void run()
            {
                currentTick++;
            }
        }.runTaskTimer(this, 1L, 1L);
        titleManager = new TitleManager(this);

        //Commands
        getCommand("nick").setExecutor(new NickCommand(this));
        getCommand("warp").setExecutor(new WarpCommand(this));
        StaffRestartCommand staffRestartCommand = new StaffRestartCommand(this);
        getCommand("restart").setExecutor(staffRestartCommand);
        getCommand("restartnow").setExecutor(staffRestartCommand);
        getCommand("update").setExecutor(staffRestartCommand);
        getCommand("tip").setExecutor(tipCommand);
        DebugCommand debugCommand = new DebugCommand(this);
        getCommand("mdebug").setExecutor(debugCommand);
        getCommand("lejail").setExecutor(debugCommand);
        getCommand("watchwinreward").setExecutor(debugCommand);
        getCommand("md").setExecutor(debugCommand);
        getCommand("voice").setExecutor(new VoiceCommand(this));
        getCommand("view").setExecutor(new ViewDistanceCommand(this));
        getCommand("reset").setExecutor(new ResetCommands(this));
        getCommand("clearchat").setExecutor(new ClearChatCommand());
        new Emoticons(this);

        EmoticonCommands emoticonCommands = new EmoticonCommands(this);
        getCommand("shrug").setExecutor(emoticonCommands);

        getCommand("start").setExecutor(new LetsStart(this));
        saveConfig();
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                serverDoneLoading = true;
            }
        }.runTask(this);
    }

    public void onDisable()
    {
        getServer().getScheduler().cancelTasks(this);
        //TODO: delete instantiated worlds (i.e. those not in MV)
        betterNoDamageTicks.onDisable();
        for (Player player : getServer().getOnlinePlayers())
            atmosphericManager.stopMusic(player);
        for (String key : usedMetadata.keySet())
            for (Metadatable target : usedMetadata.get(key))
                target.removeMetadata(key, this);
    }

    /*Convenience methods that rely on soft dependencies*/
    public boolean isLatest(Player player)
    {
        if (!getServer().getPluginManager().isPluginEnabled("ProtocolSupport"))
            return true;
        return ProtocolSupportAPI.getProtocolVersion(player) == ProtocolVersion.getLatest(ProtocolType.PC);
    }

    /*
     * Everything below are solely "miscellaneous" enhancements and fixes
     */

    //Warn new players that /ec costs money to use
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
//    void onPlayerPreprocess(PlayerCommandPreprocessEvent event)
//    {
//        //Check if player is attempting to access enderchest via command
//        String message = event.getMessage().toLowerCase();
//        if (!ec.matcher(message).matches())
//            return;
//
//        Player player = event.getPlayer();
//        //If player isn't new or if we've already warned this player before...
//        if (player.hasPlayedBefore() || usedEC.contains(player))
//            return;
//
//        player.sendMessage(ChatColor.GOLD + "Accessing the enderchest via a slash command costs 1337 dogecoins. To confirm, type /ec again.");
//        event.setCancelled(true);
//        usedEC.add(player);
//    }

//    Initially removed because it occasionally caused client-side chunk errors. Clients can reduce render distance if they're having chunk loading issues.
//    We'll see if this is still the case...
//    idk if it's an issue but I haven't had world loading issues for a while. Though
//    /**
//     * Make chunk loading when teleporting between worlds seem faster
//     * @param event
//     */
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
//    void onPlayerChangesWorldSetViewDistance(PlayerTeleportEvent event)
//    {
//        if (event.getFrom().getWorld() == event.getTo().getWorld())
//            return;
//
//        Player player = event.getPlayer();
//        if (player.hasMetadata("DEAD"))
//            return;
//        new BukkitRunnable()
//        {
//            public void run()
//            {
//                //Don't execute if already set
//                if (player.getViewDistance() > 3 || !getServer().getOnlinePlayers().contains(player))
//                    this.cancel();
//                //Wait for player to land before resetting view distance
//                else if (player.isOnGround())
//                {
//                    player.setViewDistance(8);
//                    this.cancel();
//                }
//            }
//        }.runTaskTimer(this, 200L, 100L);
//    }

    /**
     * Worldguard doesn't fully protect paintings and itemframes from being destroyed...
     * TODO: might need to include "Item" entity
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onExplosionDestroyPainting(HangingBreakEvent event)
    {
        Entity entity = event.getEntity();
        if (!safeWorlds.contains(entity.getWorld()))
            return;
        if (event.getCause() != HangingBreakEvent.RemoveCause.EXPLOSION)
            return;
        event.setCancelled(true);
    }

    /**
     * Protect dropped items from moving in the mall (and spawn I guess)
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onExplosionPushesItems(EntityExplodeEvent event)
    {
        Entity entity = event.getEntity();
        if (!safeWorlds.contains(entity.getWorld()))
            return;
        double yield = event.getYield();
        for (Entity nearbyEntity : entity.getNearbyEntities(yield, yield, yield))
        {
            if (nearbyEntity.getType() == EntityType.DROPPED_ITEM)
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Don't allow shouts to push dropped items in the mall (primarily to preserve showcases)
     * But also to prevent usage when "dead"
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    void onExplosionPushesItemsButNotViaATNTEntity(ShoutAreaOfEffectEvent event)
    {
        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), "fortress.fusrodah", SoundCategory.PLAYERS, 1.5f, 1.0f);
        if (!safeWorlds.contains(event.getPlayer().getWorld()) && !event.getPlayer().hasMetadata("DEAD"))
            return;

        List<Entity> newEntities = new ArrayList<>();
        for (Entity nearbyEntity : event.getAffectedEntities())
        {
            if (nearbyEntity.getType() != EntityType.DROPPED_ITEM || !nearbyEntity.hasMetadata("NO_PICKUP"))
            {
                newEntities.add(nearbyEntity);
            }
        }
        event.setAffectedEntities(newEntities);
    }

    /**
     * Reset things some plugins stupidly play around with >_>
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onWorldChange(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        player.setHealthScaled(false);
    }

    /**
     * Convenience method
     * @param min
     * @param max
     * @return
     */
    public int r4nd0m(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
