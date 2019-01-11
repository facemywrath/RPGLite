package facemywrath.rpglite.professions;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import facemywrath.rpglite.main.Main;
import facemywrath.rpglite.storage.User;

public interface Profession {
	
	public static final Profession MINING = new Mining();

	public static List<Profession> getValues() {
		return Arrays.asList(MINING);
	}
	
	public static Profession getByName(String str) {
		switch(str.toLowerCase()) {
		case "mining":
			return MINING;
		}
		return null;
	}
	
	public void event(Main main);
	
	public default void increment(User user, int amount){
		user.increment(this, amount);
	}
	
	public String getTier(User user);
	
	public String getName();
	
	public default MaterialData parseString(String key) {
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
