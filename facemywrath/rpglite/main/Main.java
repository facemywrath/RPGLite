package facemywrath.rpglite.main;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import facemywrath.rpglite.commands.BaseCommand;
import facemywrath.rpglite.events.PlayerJoin;
import facemywrath.rpglite.professions.abstraction.Profession;
import facemywrath.rpglite.storage.ItemManager;
import facemywrath.rpglite.storage.UserManager;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

	private UserManager userManager;
	private ItemManager itemManager;
	
	private Economy economy;
	
	public void onEnable() {
		this.saveResource("config.yml", false);
		this.saveResource("items.yml", false);
		this.saveResource("professions/mining.yml", false);
		this.getCommand("rpg").setExecutor(new BaseCommand());
		userManager = new UserManager(this);
		new PlayerJoin(this);
		itemManager = new ItemManager(this);
        if (!setupEconomy()) {
            this.getLogger().severe("Disabled due to no Vault dependency found!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        for(Profession prof : Profession.getValues()) {
        	prof.init(this);
        }
	}

	public UserManager getUserManager() {
		return userManager;
	}
	
	public ItemManager getItemManager() {
		return itemManager;
	}

	public Economy getEconomy() {
		return economy;
	}

	private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
	
}
