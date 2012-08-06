package me.firedroide.plugins.reender;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;

public class EndRepairer implements Runnable {
	
	private static final int xzMin = -144;
	private static final int xzMax = 144;
	
	World w;
	String[] args;
	ReEnder plugin;
	
	public EndRepairer(ReEnder reender, World world, String[] arguments) {
		if (world == null) throw new IllegalArgumentException("The world can't be null.");
		if (arguments == null) throw new IllegalArgumentException("There must be some arguments.");
		w = world;
		args = arguments;
		plugin = reender;
	}
	
	@Override
	public void run() {
		
		if ((!hasArgument(args, "-c") && (!plugin.crystals.containsKey(w) || hasArgument(args, "-r"))) || !hasArgument(args, "-p")) {
			getCrystalLocations(w);
		}
		
		if (!hasArgument(args, "-c")) {
			for (EnderCrystal ec : w.getEntitiesByClass(EnderCrystal.class)) {
				ec.remove();
			}
			
			for (Block b : plugin.crystals.get(w)) {
				w.spawn(b.getLocation().add(0.5, 0, 0.5), EnderCrystal.class);
				if (b.getTypeId() != Material.BEDROCK.getId()) b.setType(Material.BEDROCK);
				b.getRelative(BlockFace.UP).setType(Material.FIRE);
			}
		}
		
		if (!hasArgument(args, "-p")) {
			for (Block b : plugin.portals.get(w)) {
				checkRelatives(b);
			}
			plugin.portals.remove(w);
		}
		
		if (!hasArgument(args, "-d")) {
			for (EnderDragon ed : w.getEntitiesByClass(EnderDragon.class)) {
				ed.remove();
			}
			w.spawn(new Location(w, 0, 128, 0), EnderDragon.class);
		}
	}
	
	public boolean hasArgument(String[] args, String arg) {
		for (String a : args) {
			if (a.equalsIgnoreCase(arg)) return true;
		}
		return false;
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
		plugin.crystals.put(w, c);
		plugin.portals.put(w, p);
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
