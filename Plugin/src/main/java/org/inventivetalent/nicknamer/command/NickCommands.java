/*
 * Copyright 2015-2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.nicknamer.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import java.util.*;

public class NickCommands {

	private NickNamerPlugin plugin;

	public NickCommands(NickNamerPlugin plugin) {
		this.plugin = plugin;
	}

	@Command(name = "nickname",
			 aliases = { "nick" },
			 usage = "<Name> [Player]",
			 description = "Set your own, or another player's nick name",
			 min = 1,
			 max = 2,
			 errorHandler = NickNamerErrorHandler.class)
	@Permission("nick.command.name")
	public void nick(CommandSender sender, String nick, @OptionalArg String targetName) {
		boolean otherTarget = targetName != null && !targetName.isEmpty();
		final Player target = CommandUtil.findTarget(sender, targetName, otherTarget);
		if (target == null) { return; }

		if (otherTarget && !sender.hasPermission("nick.other")) {
			throw new PermissionException("nick.other");
		}

		if (nick.length() > 16) {
			sender.sendMessage(CommandUtil.MESSAGE_LOADER.getMessage("name.error.length", "name.error.length"));
			return;
		}
		if ((!sender.hasPermission("nick.name." + nick) && !sender.hasPermission("nick.name.*")) /*|| sender.hasPermission("-nick.name." + nick)*/) {
			throw new PermissionException("nick.name." + nick);
		}

		try {
			nick = String.format(plugin.namesFormat, nick);
		} catch (IllegalFormatException e) {
			plugin.getLogger().warning("Cannot format name, invalid name format: " + plugin.namesFormat);
		}

		if (sender.hasPermission("nick.colored")) {
			nick = ChatColor.translateAlternateColorCodes('&', nick);
		}
		if (nick.length() > 16) {
			nick = nick.substring(0, 16);
		}

		final String finalNick = nick;
		sender.sendMessage(CommandUtil.MESSAGE_LOADER.getMessage("name.changed", "name.changed", new MessageFormatter() {
			@Override
			public String format(String key, String message) {
				return message.replace("%player%", target.getName()).replace("%name%", finalNick);
			}
		}));
		NickNamerAPI.getNickManager().setNick(target.getUniqueId(), nick);
	}

	@Command(name = "clearNick",
			 aliases = {
					 "nickclear",
					 "resetnick" },
			 usage = "[Player]",
			 description = "Reset your own, or another player's nick name",
			 min = 0,
			 max = 1)
	@Permission("nick.command.name.clear")
	public void clearNick(final CommandSender sender, @OptionalArg String targetName) {
		boolean otherTarget = targetName != null && !targetName.isEmpty();
		final Player target = CommandUtil.findTarget(sender, targetName, otherTarget);
		if (target == null) { return; }

		if (otherTarget && !sender.hasPermission("nick.other")) {
			throw new PermissionException("nick.other");
		}

		NickNamerAPI.getNickManager().removeNick(target.getUniqueId());
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				sender.sendMessage(CommandUtil.MESSAGE_LOADER.getMessage("name.cleared", "name.cleared", new MessageFormatter() {
					@Override
					public String format(String key, String message) {
						return message.replace("%player%", target.getName());
					}
				}));
			}
		}, 10);
	}

	@Command(name = "randomNick",
			 aliases = { "nickrandom" },
			 usage = "[Player] [Category]",
			 description = "Get a random nick name",
			 min = 0,
			 max = 2)
	@Permission("nick.command.name.random")
	public void randomNick(final CommandSender sender, @OptionalArg String targetName, @OptionalArg(def = "__default__") final String category) {
		if (!plugin.randomNicks.containsKey(category)) {
			sender.sendMessage(CommandUtil.MESSAGE_LOADER.getMessage("name.error.category.unknown", "name.error.category.unknown", new MessageFormatter() {
				@Override
				public String format(String key, String message) {
					return String.format(message, category);
				}
			}));
		} else {
			if (sender instanceof Player) {
				((Player) sender).chat("/nickname " + NickNamerAPI.getRandomNick(plugin.randomNicks.get(category)) + (targetName != null ? " " + targetName : ""));
			} else {
				Bukkit.getServer().dispatchCommand(sender, "/nickname " + NickNamerAPI.getRandomNick(plugin.randomNicks.get(category)) + (targetName != null ? " " + targetName : ""));
			}
		}
	}

	@Command(name = "refreshNick",
			 aliases = {
					 "nickrefresh",
					 "refreshskin",
					 "reloadskin",
					 "reloadnick" },
			 usage = "[Player]",
			 description = "Refresh the displayed skin",
			 min = 0,
			 max = 1)
	@Permission("nick.command.refresh")
	public void refreshNick(final CommandSender sender, @OptionalArg String targetName) {
		boolean otherTarget = targetName != null && !targetName.isEmpty();
		final Player target = CommandUtil.findTarget(sender, targetName, otherTarget);
		if (target == null) { return; }

		if (otherTarget && !sender.hasPermission("nick.other")) {
			throw new PermissionException("nick.other");
		}
		NickNamerAPI.getNickManager().refreshPlayer(target.getUniqueId());
	}

	@Command(name = "listNames",
			 aliases = {
					 "nicklist",
					 "listnick" },
			 description = "Get a list of used names",
			 max = 0)
	@Permission("nick.command.name.list")
	public void listNick(final CommandSender sender) {
		Collection<String> usedNicks = NickNamerAPI.getNickManager().getUsedNicks();
		if (usedNicks.isEmpty()) {
			sender.sendMessage(CommandUtil.MESSAGE_LOADER.getMessage("name.error.list.empty", "name.error.list.empty"));
			return;
		}

		sender.sendMessage(CommandUtil.MESSAGE_LOADER.getMessage("name.list.used", "name.list.used"));
		for (final String used : usedNicks) {
			Collection<UUID> usedByIds = NickNamerAPI.getNickManager().getPlayersWithNick(used);
			final Set<String> usedByNames = new HashSet<>();
			for (UUID uuid : usedByIds) {
				OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				if (player != null) {
					usedByNames.add(player.getName());
				} else {
					usedByNames.add(uuid.toString());
				}
			}
			sender.sendMessage(CommandUtil.MESSAGE_LOADER.getMessage("name.list.format", "name.list.format", new MessageFormatter() {
				@Override
				public String format(String key, String message) {
					return String.format(message, used, usedByNames.toString());
				}
			}));
		}
	}

}
