package org.inventivetalent.nicknamer.api;

import lombok.NonNull;
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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"unused", "WeakerAccess"})
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
	 *
	 * @param original       original message
	 * @param namesToReplace names to replace
	 * @param replacer       {@link NameReplacer}
	 * @param ignoreCase     whether to ignore case
	 * @return the replaced message
	 */
	public static String replaceNames(@NonNull final String original, @NonNull final Iterable<String> namesToReplace, @NonNull final NameReplacer replacer, boolean ignoreCase) {
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
		if (getNickManager().isSimple()) {
			return nickedPlayerNames;
		}
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

	public static String getRandomNick(Collection<String> nicks) {
		RandomNickRequestEvent event;
		Bukkit.getPluginManager().callEvent(event = new RandomNickRequestEvent(new ArrayList<>(nicks)));
		if (event.getPossibilities().isEmpty()) {
			return "";
		}
		return ((List<String>) event.getPossibilities()).get(random.nextInt(event.getPossibilities().size()));
	}

	public static String getRandomSkin(Collection<String> skins) {
		RandomSkinRequestEvent event;
		Bukkit.getPluginManager().callEvent(event = new RandomSkinRequestEvent(new ArrayList<>(skins)));
		if (event.getPossibilities().isEmpty()) {
			return "";
		}
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


