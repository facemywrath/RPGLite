package facemywrath.rpglite.storage;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import facemywrath.rpglite.professions.Profession;

public class User {

	private HashMap<Profession,Integer> professions = new HashMap<>();
	private UserManager userManager;
	private UUID uuid;
	
	public User(UserManager userManager, UUID uuid) {
		this.userManager = userManager;
		this.uuid = uuid;
		this.professions.put(Profession.MINING, 1);
	}

	public User load() {
		ConfigurationSection section = userManager.getUserConfig().getConfigurationSection(uuid.toString());
		for(String profession : section.getConfigurationSection("Professions").getKeys(false))
		{
			if(Profession.getByName(profession) == null) {
				section.set("Professions." + profession + ".Level", null);
				section.set("Professions." + profession, null);
				continue;
			}
			professions.put(Profession.getByName(profession), section.getInt("Professions." + profession + ".Level"));
		}
		return this;
	}
	
	public void save() {
		
	}
	
	public int getLevel(Profession profession) {
		if(professions.containsKey(profession)) return professions.get(profession);
		return -1;
	}

	public void increment(Profession profession, int amount) {
		if(!professions.containsKey(profession))
			professions.put(profession, 0);
		professions.put(profession, professions.get(profession)+1);
	}
	
}
