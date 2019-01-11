package facemywrath.rpglite.storage;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import facemywrath.rpglite.main.Main;
import facemywrath.rpglite.util.Animation;

public class UserManager {

	private HashMap<UUID, User> users = new HashMap<>();
	
	private long saveDelay = 600L;
	private File userFile;
	private FileConfiguration userConfig;
	
	public UserManager(Main main) {
		File dataFolder = main.getDataFolder();
		userFile = new File(dataFolder, "users.yml");
		userConfig = YamlConfiguration.loadConfiguration(userFile);
		if(main.getConfig().contains("save-delay")) saveDelay = main.getConfig().getLong("save-delay");
		new SaveAnimation(main, saveDelay);
	}

	public User getUser(UUID uuid) {
		if(!users.containsKey(uuid))
			if(Bukkit.getOfflinePlayer(uuid).hasPlayedBefore())generateUser(uuid);
			else return null;
		return users.get(uuid);
	}

	private void generateUser(UUID uuid) {
		users.put(uuid, new User(this, uuid));
	}
	
	public Collection<User> getUsers() {
		return users.values();
	}

	public FileConfiguration getUserConfig() {
		return userConfig;
	}
}
class SaveAnimation extends Animation<UserManager> {

	public SaveAnimation(Main main, long saveDelay) {
		super(main);
		this.addFrame(userManager -> {
			for(User user : userManager.getUsers()) {
				user.save();
			}
		}, saveDelay).setLooping(true, 0L);
	}
	
}
