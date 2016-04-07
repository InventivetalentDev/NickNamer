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
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.apihelper.API;
import org.inventivetalent.apihelper.APIManager;
import org.inventivetalent.nicknamer.api.event.random.RandomNickRequestEvent;
import org.inventivetalent.nicknamer.api.event.random.RandomSkinRequestEvent;
import org.inventivetalent.nicknamer.api.event.replace.NameReplacer;
import org.inventivetalent.packetlistener.PacketListenerAPI;
import org.inventivetalent.packetlistener.handler.PacketHandler;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NickNamerAPI implements API, Listener {

	static final Random random = new Random();

	protected static NickManager nickManager;
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
				String replace = replacer.replace(name);
				matcher.appendReplacement(replacementBuffer, replace);
			}
			matcher.appendTail(replacementBuffer);

			replaced = replacementBuffer.toString();
		}
		return replaced;
	}

	/**
	 * @return The names of all nicked players (only works if the plugin is installed)
	 */
	public static Set<String> getNickedPlayerNames() {
		Set<String> nickedPlayerNames = new HashSet<>();
		if (getNickManager().isSimple()) { return nickedPlayerNames; }
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

	public static String getRandomNick(Set<String> nicks) {
		RandomNickRequestEvent event;
		Bukkit.getPluginManager().callEvent(event = new RandomNickRequestEvent(new ArrayList<>(nicks)));
		if (event.getPossibilities().isEmpty()) { return ""; }
		return ((List<String>) event.getPossibilities()).get(random.nextInt(event.getPossibilities().size()));
	}

	public static String getRandomSkin(Set<String> skins) {
		RandomSkinRequestEvent event;
		Bukkit.getPluginManager().callEvent(event = new RandomSkinRequestEvent(new ArrayList<>(skins)));
		if (event.getPossibilities().isEmpty()) { return ""; }
		return ((List<String>) event.getPossibilities()).get(random.nextInt(event.getPossibilities().size()));
	}

	@Override
	public void load() {
		APIManager.require(PacketListenerAPI.class, null);
	}

	@Override
	public void init(Plugin plugin) {
		APIManager.initAPI(PacketListenerAPI.class);

		APIManager.registerEvents(this, this);

		nickManager = new SimpleNickManager(plugin);
		//		uuidResolver = new UUIDResolver(plugin, 3600000/* 1 hour */);

		PacketHandler.addHandler(packetListener = new PacketListener(plugin));
	}

	@Override
	public void disable(Plugin plugin) {
		PacketHandler.removeHandler(packetListener);
		APIManager.disableAPI(PacketListenerAPI.class);
	}

}


