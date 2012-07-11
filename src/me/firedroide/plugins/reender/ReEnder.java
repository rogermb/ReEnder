package me.firedroide.plugins.reender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ReEnder extends JavaPlugin implements CommandExecutor {
	
	private static final int xzMin = -144;
	private static final int xzMax = 144;
	HashMap<World, List<Block>> crystals;
	HashMap<World, List<Block>> portals;
	
	public void onEnable() {
		crystals = new HashMap<World, List<Block>>();
		portals = new HashMap<World, List<Block>>();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!sender.hasPermission("reender.use") && sender instanceof Player) {
			sender.sendMessage("§4You don't have the permission to use this command.");
			return true;
		}
		
		World w;
		
		if (!hasWorldArgument(args)) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You need to specify a world if you want to execute this command from the console.");
				return true;
			} else if (!((Player) sender).getWorld().getEnvironment().equals(Environment.THE_END)) {
				sender.sendMessage("§cThe world you are in must be 'The End'.");
				sender.sendMessage("§cOtherwise you should use /reend <World>.");
				return true;
			}
			w = ((Player) sender).getWorld();
		} else {
			if (!getWorld(args).getEnvironment().equals(Environment.THE_END)) {
				sender.sendMessage("§cThe world needs to be in 'The End' dimension.");
				return true;
			}
			w = getWorld(args);
		}
		
		if ((!hasArgument(args, "-c") && (!crystals.containsKey(w) || hasArgument(args, "-r"))) || !hasArgument(args, "-p")) {
			getCrystalLocations(w);
		}
		
		if (!hasArgument(args, "-c")) {
			for (EnderCrystal ec : w.getEntitiesByClass(EnderCrystal.class)) {
				ec.remove();
			}
			
			for (Block b : crystals.get(w)) {
				w.spawn(b.getLocation().add(0.5, 0, 0.5), EnderCrystal.class);
				b.getRelative(BlockFace.UP).setType(Material.FIRE);
			}
		}
		
		if (!hasArgument(args, "-p")) {
			for (Block b : portals.get(w)) {
				checkRelatives(b);
			}
			portals.remove(w);
		}
		
		if (!hasArgument(args, "-d")) {
			for (EnderDragon ed : w.getEntitiesByClass(EnderDragon.class)) {
				ed.remove();
			}
			w.spawn(new Location(w, 0, 128, 0), EnderDragon.class);
		}
		
		return true;
	}
	
	public boolean hasArgument(String[] args, String arg) {
		for (String a : args) {
			if (a.equalsIgnoreCase(arg)) return true;
		}
		return false;
	}
	public boolean hasWorldArgument(String[] args) {
		if (args == null || args.length == 0) return false;
		for (String arg : args) {
			if (arg.startsWith("-")) continue;
			for (World world : Bukkit.getWorlds()) {
				if (world.getName().equalsIgnoreCase(arg)) return true;
			}
		}
		return false;
	}
	public World getWorld(String[] args) {
		for (String arg : args) {
			if (arg.startsWith("-")) continue;
			for (World world : Bukkit.getWorlds()) {
				if (world.getName().equalsIgnoreCase(arg)) return world;
			}
		}
		return null;
	}
	
	public void getCrystalLocations(World w) {
		List<Block> c = new ArrayList<Block>();
		List<Block> p = new ArrayList<Block>();
		int b, y;
		for (int x = xzMin; x < xzMax; x++) {
			for (int z = xzMin; z < xzMax; z++) {
				y = w.getHighestBlockYAt(x, z) - 1;
				b = w.getBlockTypeIdAt(x, y, z);
				if (b == Material.BEDROCK.getId()) {
					if (w.getBlockAt(x, y, z).getRelative(BlockFace.DOWN).getTypeId() == Material.OBSIDIAN.getId()) {
						c.add(w.getBlockAt(x, y, z));
					}
					if (w.getBlockAt(x, y, z).getRelative(BlockFace.UP).getTypeId() == Material.ENDER_PORTAL.getId()) {
						p.add(w.getBlockAt(x, y + 1, z));
					}
				}
			}
		}
		crystals.put(w, c);
		portals.put(w, p);
	}
	
	public void checkRelatives(Block b) {
		Block down = b.getRelative(BlockFace.DOWN);
		Block up = b.getRelative(BlockFace.UP);
		
		if (down.getTypeId() == Material.BEDROCK.getId()) {
			down.setType(Material.AIR);
		}
		checkRelativeNESW(b.getRelative(BlockFace.NORTH));
		checkRelativeNESW(b.getRelative(BlockFace.EAST));
		checkRelativeNESW(b.getRelative(BlockFace.SOUTH));
		checkRelativeNESW(b.getRelative(BlockFace.WEST));
		if (up.getTypeId() == Material.DRAGON_EGG.getId()) {
			up.setType(Material.AIR);
		} else if (up.getTypeId() == Material.BEDROCK.getId()) {
			checkRelatives(up);
		}
		b.setType(Material.AIR);
	}
	public void checkRelativeNESW(Block b) {
		if (b.getTypeId() == Material.BEDROCK.getId()) {
			b.setType(Material.AIR);
			if (b.getRelative(BlockFace.DOWN).getTypeId() == Material.BEDROCK.getId()) {
				b.getRelative(BlockFace.DOWN).setType(Material.AIR);
			}
			if (b.getRelative(BlockFace.UP).getTypeId() == Material.BEDROCK.getId()) {
				checkRelatives(b.getRelative(BlockFace.UP));
			}
		} else if (b.getTypeId() == Material.TORCH.getId()) {
			b.setType(Material.AIR);
		}
	}
}
