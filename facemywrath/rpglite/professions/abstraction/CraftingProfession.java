package facemywrath.rpglite.professions.abstraction;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import facemywrath.rpglite.main.Main;
import facemywrath.rpglite.professions.Mining;
import facemywrath.rpglite.storage.User;

public interface CraftingProfession extends Profession {

	
	public boolean onCooldown(Block block);
	
	public int getToolRequirement(ItemStack item);
	
	public int getBlockRequirement(MaterialData data);
	
}
