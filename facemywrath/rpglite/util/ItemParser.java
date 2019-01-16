package facemywrath.rpglite.util;

import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import facemywrath.rpglite.main.Main;
import facemywrath.rpglite.storage.ItemManager;

public class ItemParser {
	
	public static ItemStack parseItem(String itemName) {
		if(itemName.startsWith("mm: "))
			return parseMythicMobsItem(itemName);
		if(itemName.startsWith("rpgitem: "))
			return parseRPGItem(itemName);
		ItemManager manager = Main.getPlugin(Main.class).getItemManager();
		itemName = itemName.substring(3);
		return manager.getItemByName(itemName);
	}

	public static String parseName(ItemStack item) {
		return item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : StringUtils.capitaliseAllWords(item.getType().toString().toLowerCase().replaceAll("_", " "));
	}

	public static String parseName(MaterialData data) {
		return StringUtils.capitaliseAllWords(data.getItemType().toString().toLowerCase().replaceAll("_", " "));
	}
	
	private static ItemStack parseMythicMobsItem(String itemName) {
		return null;
	}
	
	private static ItemStack parseRPGItem(String itemName) {
		return null;
	}

}
