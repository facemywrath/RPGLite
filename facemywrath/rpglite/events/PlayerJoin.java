package facemywrath.rpglite.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.BlockIterator;

import facemywrath.rpglite.main.Main;
import facemywrath.rpglite.professions.abstraction.GatheringProfession;
import facemywrath.rpglite.professions.abstraction.Profession;
import facemywrath.rpglite.storage.User;
import facemywrath.rpglite.util.Animation;
import facemywrath.rpglite.util.Events;
import facemywrath.rpglite.util.ItemParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class PlayerJoin {
	
	Animation<Player> lookUpdate;
	
	@SuppressWarnings("unchecked")
	public PlayerJoin(Main main) {
		lookUpdate = new Animation<Player>(main).addFrame(player -> {
			Block block = player.getTargetBlock(null, 5);
			if(block == null) return;
			User user = main.getUserManager().getUser(player.getUniqueId());
			MaterialData data = block.getState().getData();
			for(GatheringProfession prof : Profession.getGathering()) {
				if(prof.getBlockRequirement(data) != -1) {
					int level = prof.getBlockRequirement(data);
					boolean canBreak = user.getLevel(prof) >= level;
					boolean unlocked = user.getLevel(prof) > 0;
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', (unlocked?"&a" : "&c") + prof.getName() + " (" + user.getLevel(prof) + ") &7- " + (prof.getDifferenceColor(user, level)) + ItemParser.parseName(data)) + " (" + level + ")").create());
				}
			}
		}, 10L).setLooping(true, 0L);
		Events.listen(main, PlayerJoinEvent.class, event -> {
			lookUpdate.animate(event.getPlayer());
		});
	}

}
