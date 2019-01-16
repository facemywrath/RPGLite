package facemywrath.rpglite.professions;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import facemywrath.rpglite.main.Main;
import facemywrath.rpglite.professions.abstraction.GatheringProfession;
import facemywrath.rpglite.professions.abstraction.Profession;
import facemywrath.rpglite.storage.User;
import facemywrath.rpglite.util.Events;
import facemywrath.rpglite.util.ItemParser;

public class Mining implements GatheringProfession {

	private HashMap<String, Integer> miningLevels = new HashMap<>(); // Storing each tier of mining to specific levels.
	private HashMap<MaterialData, Integer> blockLimits = new HashMap<>(); // Storing the required mining level to break a type of block.
	private HashMap<ItemStack, Integer> miningTools = new HashMap<>(); // Storing the required mining level to use a type of tool.
	private HashMap<String, Integer> costs = new HashMap<>();
	private HashMap<Block, Long> cooldowns = new HashMap<>();
	private boolean promotionpurchase = false;
	private int skillGap = 30; 
	private int maxLevel = 225;

	@Override
	public void init(Main main) {
		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(main.getDataFolder() + File.separator + "professions", "mining.yml"));
		if(!config.contains("Mining")) {
			System.out.print("RPGLite: Mining not in the config, disabling Mining.");
			return;
		}
		if(config.contains("MaxLevel")) this.maxLevel = config.getInt("Mining.MaxLevel") == -1 ? Integer.MAX_VALUE : config.getInt("Mining.MaxLevel");
		if(config.contains("Mining.SkillGap")) this.skillGap = config.getInt("Mining.SkillGap") == -1 ? Integer.MAX_VALUE : config.getInt("Mining.SkillGap");
		if(config.contains("Mining.Purchase-Promotion")) this.promotionpurchase = config.getBoolean("Mining.Purchase-Promotion");
		if(config.contains("Mining.Blocks"))
			for(String key : config.getConfigurationSection("Mining.Blocks").getKeys(false)){
				MaterialData material = this.parseMaterialData(key);
				if(material == null) 
				{
					System.out.print("RPGLite: " + key + " not properly defined, skipping.");
					continue;
				}
				blockLimits.put(material, config.getInt("Mining.Blocks." + key));
			}	
		if(config.contains("Mining.Tools"))
			for(String key : config.getConfigurationSection("Mining.Tools").getKeys(false)){
				MaterialData material = this.parseMaterialData(key);
				if(material == null) 
				{
					ItemStack item = ItemParser.parseItem(key);
					if(item == null)
					{
						System.out.print("RPGLite: " + key + " not properly defined, skipping.");
						continue;
					}
					miningTools.put(item, config.getInt("Mining.Tools." + key));
					continue;
				}
				miningTools.put(new ItemStack(material.getItemType(), 1, material.getData()), config.getInt("Mining.Tools." + key));
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
			if(promotionpurchase)
			{
				if(section.contains(tier + ".Cost"))
					costs.put(tier, section.getInt(tier + ".Cost"));
			}
		}
		Events.listen(main, BlockBreakEvent.class, event -> {
			Player player = event.getPlayer();
			User user = main.getUserManager().getUser(player.getUniqueId());
			ItemStack hand = event.getPlayer().getInventory().getItemInHand();
			String tier = getTier(user);
			int level = user.getLevel(this);
			if(hand != null && hand.getType() != Material.AIR) 
				if(miningTools.keySet().stream().anyMatch(item -> isSimilar(item, hand))){
					if(level < getToolRequirement(hand)){
						event.setCancelled(true);
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou can't use a " + ItemParser.parseName(hand) + " yet."));
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
			
			if(cooldowns.containsKey(event.getBlock()) && cooldowns.get(event.getBlock()) > System.currentTimeMillis()) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Block placed too recently. No experience gained."));
				cooldowns.remove(event.getBlock());
				return;
			}
			
			String nextTier = getNextTier(user);
			if(level - blockLimits.get(materialData) < this.skillGap && (!promotionpurchase || (nextTier == null && level < maxLevel) || (nextTier != null && miningLevels.get(nextTier) != level))){
				user.increment(this, 1);
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9Mining level increased to " + (level+1)));
				if(nextTier != null && miningLevels.get(nextTier) == level+1)
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Must promote to next tier to continue gaining experience."));
			}
		});
		Events.listen(main, BlockPlaceEvent.class, event -> {
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
			cooldowns.put(block, System.currentTimeMillis() + 10000L);
		});
	}

	@Override
	public String getName() {
		return "Mining";
	}

	public boolean isSimilar(ItemStack item1, ItemStack item2) {
		if(item1.getData().getData() == -1)
		{
			item1 = item1.clone();
			item1.setDurability(item2.getDurability());
			if(item1.isSimilar(item2)) return true;
			return false;
		}
		if(item1.isSimilar(item2)) return true;
		return false;
	}

	public boolean isSimilar(MaterialData item1, MaterialData item2) {
		if(item1.getData() == -1)
		{
			if(item1.getItemType() == item2.getItemType()) return true;
			return false;
		}
		if(item1.getData() == item2.getData() && item1.getItemType() == item2.getItemType()) return true;
		return false;
	}

	@Override
	public String getTier(User user) {
		int level = user.getLevel(Profession.MINING);
		Optional<String> highest = miningLevels.keySet().stream().filter(key -> miningLevels.get(key) < level).max((key1, key2) -> miningLevels.get(key1).compareTo(miningLevels.get(key2)));
		return highest.isPresent()?highest.get():null;
	}

	public String getNextTier(User user) {
		int level = user.getLevel(Profession.MINING);
		Optional<String> lowest = miningLevels.keySet().stream().filter(key -> miningLevels.get(key) >= level).min((key1, key2) -> miningLevels.get(key1).compareTo(miningLevels.get(key2)));
		return lowest.isPresent()?lowest.get():null;
	}

	@Override
	public boolean onCooldown(Block block) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getToolRequirement(ItemStack hand) {
		for(ItemStack item : miningTools.keySet()) {
			if(isSimilar(item, hand)) return miningTools.get(item);
		}
		return 0;
	}

	@Override
	public int getBlockRequirement(MaterialData data) {
		for(MaterialData item : blockLimits.keySet()) {
			if(isSimilar(item, data)) return blockLimits.get(item);
		}
		return -1;
	}

	@Override
	public int getSkillGap() {
		return skillGap;
	}

	@Override
	public List<MaterialData> getTotalBlocks() {
		return (List<MaterialData>) blockLimits.keySet();
	}

}
