package facemywrath.rpglite.main;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import facemywrath.rpglite.professions.Profession;
import facemywrath.rpglite.storage.UserManager;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

	private UserManager userManager;
	
	private Economy economy;
	
	public void onEnable() {
		this.saveResource("config.yml", false);
		userManager = new UserManager(this);
        if (!setupEconomy()) {
            this.getLogger().severe("Disabled due to no Vault dependency found!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        for(Profession prof : Profession.getValues()) {
        	prof.event(this);
        }
	}

	public UserManager getUserManager() {
		return userManager;
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
