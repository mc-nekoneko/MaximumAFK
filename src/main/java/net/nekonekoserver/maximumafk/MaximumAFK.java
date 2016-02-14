package net.nekonekoserver.maximumafk;

import com.earth2me.essentials.IUser;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Nekoneko
 */
public class MaximumAFK extends JavaPlugin implements Listener {

    private final PluginManager plm = getServer().getPluginManager();
    private final Queue<UUID> ids = new LinkedList<>();

    @Override
    public void onEnable() {
        super.onEnable(); //To change body of generated methods, choose Tools | Templates.
        saveDefaultConfig();

        plm.registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        super.onDisable(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("maximumafk.reload")) {
            return true;
        }
        reloadConfig();
        sender.sendMessage(ChatColor.GRAY + getName() + "Reload Comp");
        debug("config reload");
        return true;
    }

    @EventHandler
    public void playerLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.KICK_FULL) {
            return;
        }

        if (ids.size() > 0) {
            Player player = getServer().getPlayer(ids.poll());
            if (player.isOnline()) {
                player.kickPlayer("You have been kicked for idling...");
                debug(player.getName() + " AFK Kicked");
            } else {
                return;
            }

            event.allow();

            debug(player.getName() + " Changes -> " + event.getPlayer().getName());
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (ids.contains(player.getUniqueId())) {
            ids.remove(player.getUniqueId());
            debug(player.getName() + " poll remove");
        }
    }

    @EventHandler
    public void statusChange(AfkStatusChangeEvent event) {
        IUser user = event.getAffected();
        if (event.getValue()) {
            if (!ids.contains(user.getBase().getUniqueId())) {
                ids.offer(user.getBase().getUniqueId());
                debug(user.getName() + " added poll");
            }
        } else {
            if (ids.contains(user.getBase().getUniqueId())) {
                ids.remove(user.getBase().getUniqueId());
                debug(user.getName() + " poll remove");
            }
        }
    }

    private void debug(String msg) {
        getLogger().info(msg);
    }
}
