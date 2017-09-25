package org.inventivetalent.nicknamer.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.inventivetalent.nicknamer.NickNamerPlugin;
import org.inventivetalent.pluginannotations.PluginAnnotations;
import org.inventivetalent.pluginannotations.command.exception.InvalidLengthException;
import org.inventivetalent.pluginannotations.message.MessageLoader;

@SuppressWarnings({"unused", "WeakerAccess"})
public class CommandUtil {
	static MessageLoader MESSAGE_LOADER = PluginAnnotations.MESSAGE.newMessageLoader(NickNamerPlugin.instance, "config.yml", "messages.command", null);

	static Player findTarget(CommandSender sender, String targetName, boolean otherTarget) {
		Player target;
		if (otherTarget) {
			target = Bukkit.getPlayer(targetName);
		} else {
			if (sender instanceof Player) {
				target = (Player) sender;
			} else {
				throw new InvalidLengthException(2, 1);
			}
		}
		if (target == null || !target.isOnline()) {
			sender.sendMessage(MESSAGE_LOADER.getMessage("error.target.notFound", "error.target.notFound"));
			return null;
		}
		return target;
	}

}
