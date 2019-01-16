package facemywrath.rpglite.professions.abstraction;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public interface GatheringProfession extends Profession {

	
	public boolean onCooldown(Block block);
	
	public int getToolRequirement(ItemStack item);

	public int getBlockRequirement(MaterialData data);
	
	public List<MaterialData> getTotalBlocks();
	
}
