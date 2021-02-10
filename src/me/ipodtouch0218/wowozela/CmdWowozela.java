package me.ipodtouch0218.wowozela;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CmdWowozela implements CommandExecutor, TabCompleter {

	private final Wowozela main;
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!(sender instanceof Player)) {
			sender.sendMessage("§c§lWowozela » §fOnly players can do this command!");
			return true;
		}
		
		Player player = (Player) sender;
		UUID uuid = player.getUniqueId();
		boolean anyPerms = Arrays.stream(Sound.values()).anyMatch(snd -> hasPermission(player, snd));
		if (!anyPerms) {
			sender.sendMessage("§c§lWowozela » §fYou don't have perimssion to use the Wowozela!");
			return true;
		}
		
		if (args.length <= 0) {
			sender.sendMessage("§b§lWowozela » §fUsage: §b/wowozela <off/SOUND>");
			return true;
		}
		
		if (args[0].equalsIgnoreCase("off")) {
			main.sounds.put(uuid, null);
			sender.sendMessage("§b§lWowozela » §fTurned off the Wowozela!");
		} else {
			try {
				
				Sound sound = Sound.valueOf(args[0].toUpperCase());
				if (!hasPermission(player, sound)) {
					sender.sendMessage("§c§lWowozela » §fYou don't have perimssion for this sound!");
					return true;
				}
				
				boolean first = main.sounds.put(uuid, sound) == null;
				sender.sendMessage("§b§lWowozela » §fSound set to §b" + sound.name() + "§f!" + (first ? " §7§o(( Hold SHIFT to play! ))" : ""));
			} catch (Exception e) {
				sender.sendMessage("§c§lWowozela » §fUnknown sound §c" + args[0] + "§f!");
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!(sender instanceof Player)) {
			return null;
		}
		return Arrays.stream(Sound.values())
				.filter(snd -> hasPermission((Player) sender, snd))
				.map(Sound::name)
				.filter(str -> str.toLowerCase().startsWith(args[0].toLowerCase()))
				.collect(Collectors.toList());
	}
	
	public boolean hasPermission(Player player, Sound sound) {
		if (player.hasPermission("wowozela.sound.*"))
			return true;
		String[] split = sound.name().split("_");
		String currentCheck = "";
		for (int i = 0; i < split.length; i++) {
			currentCheck += ".";
			currentCheck += split[i];
			if (player.hasPermission("wowozela.sound" + currentCheck.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
