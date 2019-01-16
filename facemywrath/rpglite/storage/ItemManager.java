package facemywrath.rpglite.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import facemywrath.rpglite.main.Main;
import facemywrath.rpglite.util.ItemCreator;

public class ItemManager {
	
	private HashMap<String, ItemStack> items = new HashMap<>();

	public ItemManager(Main main) {
		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(main.getDataFolder(), "items.yml"));
		for(String str : config.getKeys(false)){
			String name = config.getString(str + ".Name", " ");
			if(name.equals(" ")) continue;
			String displayName = config.getString(str + ".DisplayName", " ");
			if(displayName.equals(" ")) continue;
			Material material = Material.getMaterial(config.getInt(str + ".Material", 0));
			if(material == Material.AIR) continue;
			byte data = (byte) config.getInt(str + ".Data");
			List<String> lore = config.contains(str + ".Lore")?config.getStringList(str + ".Lore"):new ArrayList<>();
			List<String> enchantments = config.contains(str + ".Enchantments")?config.getStringList(str + ".Enchantments"):new ArrayList<>();
			ItemStack item = new ItemCreator(material).durability(data).name(displayName).lore(lore).build();
			if(enchantments.isEmpty()) {
				items.put(name, item);
				return;
			}
			for(String enchantparse : enchantments) {
				if(!enchantparse.contains(":"))
				{
					System.out.println("RPGLite: " + name + " had issue parsing enchantment " + enchantparse + ", must specify an enchantment level, skipping.");
					continue;
				}
				String enchantname = enchantparse.substring(0, enchantparse.indexOf(":"));
				String enchantlevel = enchantparse.substring(enchantparse.indexOf(":")+1);
				if(Enchantment.getByName(enchantname) == null || !StringUtils.isNumeric(enchantlevel))
				{
					System.out.println("RPGLite: " + name + " had issue parsing enchantment " + enchantparse + ", skipping.");
					return;
				}
				item.addUnsafeEnchantment(Enchantment.getByName(enchantname), Integer.parseInt(enchantlevel));
			}
			items.put(name, item);
		}
	}
	
	public ItemStack getItemByName(String name) {
		if(items.containsKey(name)) return items.get(name);
		return null;
	}

}
