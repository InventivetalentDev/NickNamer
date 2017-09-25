package org.inventivetalent.nicknamer.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.inventivetalent.nicknamer.NickNamerPlugin;
import org.inventivetalent.nicknamer.api.NickNamerAPI;
import org.inventivetalent.pluginannotations.command.Command;
import org.inventivetalent.pluginannotations.command.OptionalArg;
import org.inventivetalent.pluginannotations.command.Permission;
import org.inventivetalent.pluginannotations.command.exception.PermissionException;
import org.inventivetalent.pluginannotations.message.MessageFormatter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SkinCommands {

	private NickNamerPlugin plugin;

	public SkinCommands(NickNamerPlugin plugin) {
		this.plugin = plugin;
	}

	@Command(name = "skin",
			aliases = {
					"setskin",
					"changeskin",
					"skinset"},
			usage = "<Skin> [Player]",
			description = "Set your own, or another player's skin",
			min = 1,
			max = 2,
			errorHandler = NickNamerErrorHandler.class)
	@Permission("nick.command.skin")
	public void skin(CommandSender sender, final String skin, @OptionalArg String targetName) {
		boolean otherTarget = targetName != null && !targetName.isEmpty();
		final Player target = CommandUtil.findTarget(sender, targetName, otherTarget);
		if (target == null) {
			return;
		}

		if (otherTarget && !sender.hasPermission("skin.other")) {
			throw new PermissionException("skin.other");
		}

		if (skin.length() > 16) {
			sender.sendMessage(CommandUtil.MESSAGE_LOADER.getMessage("skin.error.length", "skin.error.length"));
			return;
		}
		if ((!sender.hasPermission("nick.skin." + skin) && !sender.hasPermission("nick.skin.*")) /*|| sender.hasPermission("-nick.skin." + skin)*/) {
			throw new PermissionException("nick.skin." + skin);
		}

		sender.sendMessage(CommandUtil.MESSAGE_LOADER.getMessage("skin.changed", "skin.changed", new MessageFormatter() {
			@Override
			public String format(String key, String message) {
				return message.replace("%player%", target.getName()).replace("%skin%", skin);
			}
		}));
		//if (!skin.equals(NickNamerAPI.getNickManager().getSkin(target.getUniqueId()))) {
		NickNamerAPI.getNickManager().setSkin(target.getUniqueId(), skin);
		//}
	}

	@Command(name = "clearSkin",
			aliases = {
					"skinclear",
					"resetskin"},
			usage = "[Player]",
			description = "Reset your own, or another player's skin",
			max = 1)
	@Permission("nick.command.skin.clear")
	public void clearSkin(final CommandSender sender, @OptionalArg String targetName) {
		boolean otherTarget = targetName != null && !targetName.isEmpty();
		final Player target = CommandUtil.findTarget(sender, targetName, otherTarget);
		if (target == null) {
			return;
		}

		if (otherTarget && !sender.hasPermission("skin.other")) {
			throw new PermissionException("skin.other");
		}

		NickNamerAPI.getNickManager().removeSkin(target.getUniqueId());
		Bukkit.getScheduler().runTaskLater(plugin, () -> sender.sendMessage(CommandUtil.MESSAGE_LOADER.getMessage("skin.cleared", "skin.cleared", new MessageFormatter() {
			@Override
			public String format(String key, String message) {
				return message.replace("%player%", target.getName());
			}
		})), 10);
	}

	@Command(name = "randomSkin",
			aliases = {"skinrandom"},
			usage = "[Player] [Category]",
			description = "Get a random skin",
			max = 2)
	@Permission("nick.command.skin.random")
	public void randomSkin(final CommandSender sender, @OptionalArg String targetName, @OptionalArg(def = "__default__") final String category) {
		if (!plugin.randomSkins.containsKey(category)) {
			sender.sendMessage(CommandUtil.MESSAGE_LOADER.getMessage("skin.error.category.unknown", "skin.error.category.unknown", new MessageFormatter() {
				@Override
				public String format(String key, String message) {
					return String.format(message, category);
				}
			}));
		} else {
			if (sender instanceof Player) {
				((Player) sender).chat("/skin " + NickNamerAPI.getRandomSkin(plugin.randomSkins.get(category)) + (targetName != null ? " " + targetName : ""));
			} else {
				Bukkit.getServer().dispatchCommand(sender, "/skin " + NickNamerAPI.getRandomSkin(plugin.randomSkins.get(category)) + (targetName != null ? " " + targetName : ""));
			}
		}
	}

	@Command(name = "listSkins",
			aliases = {
					"skinlist",
					"listskin"},
			description = "Get a list of used skins",
			max = 0)
	@Permission("nick.command.skin.list")
	public void listNick(final CommandSender sender) {
		Collection<String> usedSkins = NickNamerAPI.getNickManager().getUsedSkins();
		if (usedSkins.isEmpty()) {
			sender.sendMessage(CommandUtil.MESSAGE_LOADER.getMessage("skin.error.list.empty", "skin.error.list.empty"));
			return;
		}

		sender.sendMessage(CommandUtil.MESSAGE_LOADER.getMessage("skin.list.used", "skin.list.used"));
		for (final String used : usedSkins) {
			Collection<UUID> usedByIds = NickNamerAPI.getNickManager().getPlayersWithSkin(used);
			final Set<String> usedByNames = new HashSet<>();
			for (UUID uuid : usedByIds) {
				OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				if (player != null) {
					usedByNames.add(player.getName());
				} else {
					usedByNames.add(uuid.toString());
				}
			}
			sender.sendMessage(CommandUtil.MESSAGE_LOADER.getMessage("skin.list.format", "skin.list.format", new MessageFormatter() {
				@Override
				public String format(String key, String message) {
					return String.format(message, used, usedByNames.toString());
				}
			}));
		}
	}

}
