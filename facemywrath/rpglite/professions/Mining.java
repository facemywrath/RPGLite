package facemywrath.rpglite.professions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import facemywrath.rpglite.main.Main;
import facemywrath.rpglite.storage.User;
import facemywrath.rpglite.util.Events;

public class Mining implements Profession {

	private HashMap<String, Integer> miningLevels = new HashMap<>(); // Storing each tier of mining to specific levels.
	private HashMap<MaterialData, Integer> blockLimits = new HashMap<>(); // Storing the required mining level to break a type of block.
	private HashMap<String, List<MaterialData>> miningTools = new HashMap<>(); // Storing the required mining level to use a type of tool.

	@Override
	public void event(Main main) {
		FileConfiguration config = main.getConfig();
		if(!config.contains("Mining")) {
			System.out.print("RPGLite: Mining not in the config, disabling Mining.");
			return;
		}
		if(config.contains("Mining.Blocks"))
			for(String key : config.getConfigurationSection("Mining.Blocks").getKeys(false)){
				MaterialData material = this.parseString(key);
				if(material == null) 
				{
					System.out.print("RPGLite: " + key + " not properly defined, skipping.");
					continue;
				}
				blockLimits.put(material, config.getInt("Mining.Blocks." + key));
			}
		if(!config.contains("Mining.Tiers")) {
			System.out.println("RPGLite: Mining Tiers not defined, disabling Mining.");
			return;
		}
		ConfigurationSection section = config.getConfigurationSection("Mining.Tiers");
		for(String tier : section.getKeys(false)) {
			if(!section.contains(tier + ".Level"))
			{
				System.out.println("RPGLite: Mining Tier " + tier + " level not defined, skipping.");
				continue;
			}
			miningLevels.put(tier, section.getInt(tier + ".Level"));
			if(!section.contains(tier + ".ToolsAllowed")) {
				System.out.println("RPGLite: Mining Tier " + tier + " tools not defined, skipping.");
				continue;
			}
			List<String> tools = section.getStringList(tier + ".ToolsAllowed");
			List<MaterialData> tooldatas = new ArrayList<>();
			for(String key : tools)
			{
				MaterialData material = this.parseString(key);

				if(material == null) 
				{
					System.out.print("RPGLite: " + key + " improperly defined for Mining Tier " + tier + ", skipping.");
					continue;
				}
				tooldatas.add(material);
			}
			miningTools.put(tier, tooldatas);
		}
		Events.listen(main, BlockBreakEvent.class, event -> {
			Player player = event.getPlayer();
			User user = main.getUserManager().getUser(player.getUniqueId());
			ItemStack hand = event.getPlayer().getInventory().getItemInHand();
			String tier = getTier(user);
			int level = user.getLevel(this);
			List<MaterialData> totalTools = miningTools.values().stream().flatMap(List::stream).distinct().collect(Collectors.toList());
			if(hand != null && hand.getType() != Material.AIR) 
				if(totalTools.stream().anyMatch(item -> isSimilar(item, hand.getData()))){
					if(tier == null || (miningTools.containsKey(tier) && !miningTools.get(tier).stream().anyMatch(item -> isSimilar(item, hand.getData())))){
						event.setCancelled(true);
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou can't use a " + hand.getType().toString().toLowerCase().replaceAll("_", " ") + " yet."));
						return;
					}
				}
			MaterialData materialData = null;
			Block block = event.getBlock();
			for(MaterialData data : blockLimits.keySet()) {
				if(data.getItemType() == block.getType() && (data.getData() == -1 || data.getData() == block.getData())) {
					materialData = data;
					break;
				}
			}
			if(materialData == null) return;
			if(!blockLimits.containsKey(materialData)) return;
			if(blockLimits.get(materialData) > level) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou can't break " + block.getType().toString().toLowerCase().replaceAll("_", " ") + " yet."));
				return;
			}
			if(level - blockLimits.get(materialData) < 30){
				user.increment(this, 1); //TODO  MAKE THE 30 CONFIGURABLE LATER
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9Mining level increased to " + (level+1)));
			}
		});
	}

	@Override
	public String getName() {
		return "Mining";
	}
	
	public boolean isSimilar(MaterialData item1, MaterialData item2) {
		if(item1.getData() == -1 && item1.getItemType() == item2.getItemType()) return true;
		if(item1.getItemType() == item2.getItemType() && item1.getData() == item2.getData()) return true;
		return false;
	}

	@Override
	public String getTier(User user) {
		int level = user.getLevel(Profession.MINING);
		Optional<String> highest = miningLevels.keySet().stream().filter(key -> miningLevels.get(key) < level).max((key1, key2) -> miningLevels.get(key1).compareTo(miningLevels.get(key2)));
		return highest.isPresent()?highest.get():null;
	}

}
