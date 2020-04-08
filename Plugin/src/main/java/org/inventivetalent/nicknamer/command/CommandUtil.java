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

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.inventivetalent.nicknamer.NickNamerPlugin;
import org.inventivetalent.pluginannotations.PluginAnnotations;
import org.inventivetalent.pluginannotations.command.exception.InvalidLengthException;
import org.inventivetalent.pluginannotations.message.MessageLoader;

import java.util.UUID;

public class CommandUtil {
	static MessageLoader MESSAGE_LOADER = PluginAnnotations.MESSAGE.newMessageLoader(NickNamerPlugin.instance, "config.yml", "messages.command", null);

	static TargetInfo findTargetInfo(CommandSender sender, String targetName, boolean otherTarget, boolean allowOffline) {
		if (allowOffline) {
			return findOfflineTargetInfo(sender, targetName, otherTarget);
		} else {
			Player player = findTarget(sender, targetName, otherTarget);
			if (player != null) {
				return new TargetInfo(player);
			}
		}
		return null;
	}

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

	static UUID findOfflineTargetUUID(CommandSender sender, String targetName, boolean otherTarget) {
		UUID uuid = null;
		if (otherTarget) {
			Player target = Bukkit.getPlayer(targetName);
			if (target != null) {
				uuid = target.getUniqueId();
			} else {
				try {
					// Paper
					uuid = Bukkit.getPlayerUniqueId(targetName);
				} catch (Exception ignored) {
					// Spigot
					OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
					UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + targetName).getBytes(Charsets.UTF_8));
					if (!offlineUuid.equals(offlinePlayer.getUniqueId())) { // We want the real UUID, not the one generated from OfflinePlayer:name
						uuid = offlinePlayer.getUniqueId();
					}
				}
			}
		} else {
			if (sender instanceof Player) {
				uuid = ((Player) sender).getUniqueId();
			} else {
				throw new InvalidLengthException(2, 1);
			}
		}
		if (uuid == null) {
			sender.sendMessage(MESSAGE_LOADER.getMessage("error.target.notFound", "error.target.notFound"));
			return null;
		}
		return uuid;
	}

	static TargetInfo findOfflineTargetInfo(CommandSender sender, String targetName, boolean otherTarget) {
		TargetInfo targetInfo = null;
		if (otherTarget) {
			Player target = Bukkit.getPlayer(targetName);
			if (target != null) {
				targetInfo = new TargetInfo(target);
			} else {
				sender.sendMessage(MESSAGE_LOADER.getMessage("offlineInfo", "&eThe requested player is currently offline. Changes will be applied when they come online."));
				try {
					// Paper
					targetInfo = new TargetInfo(Bukkit.getPlayerUniqueId(targetName), targetName);
				} catch (Exception ignored) {
					// Spigot
					OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
					UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + targetName).getBytes(Charsets.UTF_8));
					if (!offlineUuid.equals(offlinePlayer.getUniqueId())) { // We want the real UUID, not the one generated from OfflinePlayer:name
						targetInfo = new TargetInfo(offlinePlayer);
					}
				}
			}
		} else {
			if (sender instanceof Player) {
				targetInfo = new TargetInfo((Player) sender);
			} else {
				throw new InvalidLengthException(2, 1);
			}
		}
		if (targetInfo == null) {
			sender.sendMessage(MESSAGE_LOADER.getMessage("error.target.notFound", "error.target.notFound"));
			return null;
		}
		return targetInfo;
	}


	public static class TargetInfo {
		UUID uuid;
		String name;

		public TargetInfo(UUID uuid, String name) {
			this.uuid = uuid;
			this.name = name;
		}

		public TargetInfo(OfflinePlayer player) {
			if (player != null) {
				this.uuid = player.getUniqueId();
				this.name = player.getName();
			}
		}

	}

}
