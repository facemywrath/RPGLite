package facemywrath.rpglite.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import facemywrath.rpglite.main.Main;
import facemywrath.rpglite.professions.abstraction.Profession;

public class BaseCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		Player player = (Player) sender;
		if(args.length == 0)
			Main.getPlugin(Main.class).getUserManager().getUser(player.getUniqueId()).promote(Profession.MINING);
		else
			player.getInventory().addItem(Main.getPlugin(Main.class).getItemManager().getItemByName("ascendpick"));
		return true;
	}

}
