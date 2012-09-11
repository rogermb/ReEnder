package me.firedroide.plugins.reender;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class EndListener implements Listener {
	
	ReEnder plugin;
	
	public EndListener(ReEnder reEnder) {
		plugin = reEnder;
	}
	
	@EventHandler()
	public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
		
		if (!plugin.listenerEnabled) return;
		if (plugin.registeredWorlds == null || plugin.registeredWorlds.size() == 0) return;
		if (plugin.repairOnLeave) {
			if (e.getFrom().getEnvironment() != Environment.THE_END) return;
			if (e.getFrom().getPlayers().size() > 0) return;
			if (containsWorld(e.getFrom().getName())) {
				repairWorld(e.getFrom());
			}
		}
		if (plugin.repairOnEnter) {
			if (e.getPlayer().getWorld().getEnvironment() != Environment.THE_END) return;
			if (e.getPlayer().getWorld().getPlayers().size() > 1) return;
			if (containsWorld(e.getPlayer().getWorld().getName())) {
				repairWorld(e.getPlayer().getWorld());
			}
		}
	}
	
	private boolean containsWorld(String worldName) {
		for (String w : plugin.registeredWorlds) {
			if (worldName.equalsIgnoreCase(w)) return true;
		}
		return false;
	}
	
	private void repairWorld(World w) {
		EndRepairer er = new EndRepairer(plugin, w, plugin.getDefaultArguments());
		if (plugin.runInAsyncMode) {
			Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, er);
		} else {
			er.run();
		}
	}
	
	public void reload(ReEnder reEnder) {
		plugin = reEnder;
	}
}
