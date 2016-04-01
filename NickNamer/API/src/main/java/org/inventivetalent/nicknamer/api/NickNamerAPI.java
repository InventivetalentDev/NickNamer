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

package org.inventivetalent.nicknamer.api;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.apihelper.API;
import org.inventivetalent.apihelper.APIManager;
import org.inventivetalent.nicknamer.api.event.disguise.NickDisguiseEvent;
import org.inventivetalent.nicknamer.api.event.disguise.SkinDisguiseEvent;
import org.inventivetalent.nicknamer.api.event.replace.ChatReplacementEvent;
import org.inventivetalent.nicknamer.api.event.replace.NameReplacementEvent;
import org.inventivetalent.nicknamer.api.event.replace.NameReplacer;
import org.inventivetalent.packetlistener.PacketListenerAPI;
import org.inventivetalent.packetlistener.handler.PacketHandler;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NickNamerAPI implements API, Listener {

	private static NickManager  nickManager;
//	private static UUIDResolver uuidResolver;

	protected static PacketListener packetListener;

	public static NickManager getNickManager() {
		return nickManager;
	}

//	public static UUIDResolver getUuidResolver() {
//		return uuidResolver;
//	}

	/**
	 * Replaces all specified names in the string and calls the {@link NameReplacer} for every name
	 */
	public static String replaceNames(@Nonnull final String original, @Nonnull final Iterable<String> namesToReplace, @Nonnull final NameReplacer replacer, boolean ignoreCase) {
		String replaced = original;
		for (String name : namesToReplace) {
			Pattern pattern = Pattern.compile((ignoreCase ? "(?i)" : "") + name);
			Matcher matcher = pattern.matcher(replaced);

			StringBuffer replacementBuffer = new StringBuffer();
			while (matcher.find()) {
				String replace=replacer.replace(name);
				matcher.appendReplacement(replacementBuffer, replace);
			}
			matcher.appendTail(replacementBuffer);

			replaced = replacementBuffer.toString();
		}
		return replaced;
	}

	public static Set<String> getNickedPlayerNames() {
		Set<String> nickedPlayerNames = new HashSet<>();
		for (String nick : getNickManager().getUsedNicks()) {
			for (UUID uuid : getNickManager().getPlayersWithNick(nick)) {
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
				if (offlinePlayer != null) {
					nickedPlayerNames.add(offlinePlayer.getName());
				}
			}
		}
		return nickedPlayerNames;
	}

	//	public static void setNickManager(NickManager nickManager_) {
	//		if (nickManager != null) { throw new IllegalStateException("NickManager already set"); }
	//		nickManager = nickManager_;
	//	}

	@Override
	public void load() {
		APIManager.require(PacketListenerAPI.class, null);
	}

	@Override
	public void init(Plugin plugin) {
		APIManager.initAPI(PacketListenerAPI.class);

		APIManager.registerEvents(this, this);


		nickManager = new NickManagerImpl(plugin);
//		uuidResolver = new UUIDResolver(plugin, 3600000/* 1 hour */);

		PacketHandler.addHandler(packetListener = new PacketListener(plugin));
	}

	@Override
	public void disable(Plugin plugin) {
		PacketHandler.removeHandler(packetListener);
		APIManager.disableAPI(PacketListenerAPI.class);
	}

	// Internal event listeners

	@EventHandler(priority = EventPriority.LOWEST)
	public void on(NickDisguiseEvent event) {
		if (getNickManager().isNicked(event.getPlayer().getUniqueId())) {
			event.setNick(getNickManager().getNick(event.getPlayer().getUniqueId()));
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void on(SkinDisguiseEvent event) {
		if (getNickManager().hasSkin(event.getPlayer().getUniqueId())) {
			event.setSkin(getNickManager().getSkin(event.getPlayer().getUniqueId()));
		}
	}

	// Name replacement listeners
	@EventHandler(priority = EventPriority.NORMAL)
	public void on(final AsyncPlayerChatEvent event) {
		final String message = event.getMessage();
		Set<String> nickedPlayerNames = getNickedPlayerNames();
		String replacedMessage = replaceNames(message, nickedPlayerNames, new NameReplacer() {
			@Override
			public String replace(String original) {
				Player player = Bukkit.getPlayer(original);
				if (player != null) {
					NameReplacementEvent replacementEvent = new ChatReplacementEvent(player, event.getRecipients(), message, original, original);
					Bukkit.getPluginManager().callEvent(replacementEvent);
					if (replacementEvent.isCancelled()) { return original; }
					return replacementEvent.getReplacement();
				}
				return original;
			}
		}, true);
		event.setMessage(replacedMessage);
	}

}


