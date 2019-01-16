package facemywrath.rpglite.professions.abstraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import facemywrath.rpglite.main.Main;
import facemywrath.rpglite.professions.Mining;
import facemywrath.rpglite.storage.User;

public interface Profession {
	
	public static final Profession MINING = new Mining();

	public static List<Profession> getValues() {
		return Arrays.asList(MINING);
	}
	
	public static List<CraftingProfession> getCrafting() {
		return new ArrayList<>();
	}

	public static List<GatheringProfession> getGathering() {
		return Arrays.asList((GatheringProfession)MINING);
	}
	
	public static Profession getByName(String str) {
		switch(str.toLowerCase()) {
		case "mining":
			return MINING;
		}
		return null;
	}
	
	public void init(Main main);
	
	public default void increment(User user, int amount){
		user.increment(this, amount);
	}
	
	public String getTier(User user);
	
	public String getName();
	
	public int getSkillGap();
	
	public default ChatColor getDifferenceColor(User user, int blockreq) {
		int gap = getSkillGap()+blockreq;
		int level = user.getLevel(this);
		if(level < blockreq) return ChatColor.RED;
		if(level >= gap) return ChatColor.GRAY;
		if(level <= gap/3.0) return ChatColor.GOLD;
		if(level <= gap/3.0*2.0) return ChatColor.YELLOW;
		if(level < gap) return ChatColor.GREEN;
		return ChatColor.RED;
	}
	
	public default MaterialData parseMaterialData(String key) {
		Material material = null;
		byte data = -1;
		if(key.contains(":"))
		{
			int index = key.indexOf(":");
			String matname = key.substring(0, index-1).toUpperCase();
			String datastr = key.substring(index+1);
			if(StringUtils.isNumeric(datastr))
				data = Byte.parseByte(datastr);
			if(StringUtils.isNumeric(matname) && Material.getMaterial(Integer.parseInt(matname)) != null)
				material = Material.getMaterial(Integer.parseInt(matname));
			else if(Material.getMaterial(matname)!= null)
				material = Material.getMaterial(matname);
		}
		else
		{
			if(StringUtils.isNumeric(key) && Material.getMaterial(Integer.parseInt(key)) != null)
				material = Material.getMaterial(Integer.parseInt(key));
			else if(Material.getMaterial(key)!= null)
				material = Material.getMaterial(key);
		}
		if(material == null) return null;
		return new MaterialData(material, data);
	}

}
