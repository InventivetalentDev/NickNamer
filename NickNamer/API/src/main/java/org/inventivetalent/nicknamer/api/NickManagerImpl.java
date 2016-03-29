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
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.nicknamer.api.event.NickNamerSelfUpdateEvent;
import org.inventivetalent.nicknamer.api.event.refresh.PlayerRefreshEvent;
import org.inventivetalent.nicknamer.api.wrapper.GameProfileWrapper;
import org.json.simple.JSONObject;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class NickManagerImpl implements NickManager {

	Class EnumDifficulty = SkinLoader.nmsClassResolver.resolveSilent("EnumDifficulty");
	Class WorldType      = SkinLoader.nmsClassResolver.resolveSilent("WorldType");
	Class EnumGamemode   = SkinLoader.nmsClassResolver.resolveSilent("WorldSettings$EnumGamemode", "EnumGamemode");

	private Plugin plugin;

	final Map<UUID, String> nickedPlayers = new ConcurrentHashMap<>();
	//	final Map<UUID, String> storedNames   = new ConcurrentHashMap<>();
	final Map<UUID, String> skins         = new ConcurrentHashMap<>();

	NickManagerImpl(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean isNicked(@Nonnull UUID id) {
		return nickedPlayers.containsKey(id);
	}

	@Override
	public boolean isNickUsed(@Nonnull String nick) {
		return nickedPlayers.containsValue(nick);
	}

	@Override
	public String getNick(@Nonnull UUID id) {
		return nickedPlayers.get(id);
	}

	@Override
	public void setNick(@Nonnull UUID uuid, @Nonnull String nick) {
		if (nick.length() > 16) { throw new IllegalArgumentException("Name is too long (" + nick.length() + " > 16)"); }
		if (isNicked(uuid)) {
			removeNick(uuid);
		}
		nickedPlayers.put(uuid, nick);

		//		Player player = Bukkit.getPlayer(uuid);
		//		if (player != null) {
		//			storedNames.put(uuid, player.getDisplayName());
		//			if (getConfigOption("nick.chat")) {
		//				p.setDisplayName(nick);
		//			}
		//			if (getConfigOption("nick.tab")) {
		//				p.setPlayerListName(nick);
		//			}
		//			if (getConfigOption("nick.scoreboard")) {
		//				Scoreboard sb = p.getScoreboard();
		//				if (sb == null) {
		//					sb = Bukkit.getScoreboardManager().getMainScoreboard();
		//				}
		//				if (sb != null) {
		//					Team t = sb.getPlayerTeam(p);
		//					if (t != null) {
		//						t.removePlayer(p);
		//						t.addEntry(nick);
		//					}
		//				}
		//			}
		//		}

		//		nickNamer.sendPluginMessage(p, "name", nick);

		//		updatePlayer(id, true, false, (boolean) getConfigOption("selfUpdate"));
		refreshPlayer(uuid);
	}

	@Override
	public void removeNick(@Nonnull UUID uuid) {
		nickedPlayers.remove(uuid);

		//		Player player = Bukkit.getPlayer(uuid);
		//		if (player != null) {
		//			if (getConfigOption("nick.chat")) {
		//				p.setDisplayName(storedNames.get(id));
		//			}
		//			if (getConfigOption("nick.tab")) {
		//				p.setPlayerListName(storedNames.get(id));
		//			}
		//			if (getConfigOption("nick.scoreboard")) {
		//				Scoreboard sb = p.getScoreboard();
		//				if (sb == null) {
		//					sb = Bukkit.getScoreboardManager().getMainScoreboard();
		//				}
		//				if (sb != null) {
		//					Team t = null;
		//					for (Team tm : sb.getTeams()) {
		//						for (String s : tm.getEntries()) {
		//							if (s.equals(nick)) {
		//								t = tm;
		//								break;
		//							}
		//						}
		//					}
		//					if (t != null) {
		//						t.removeEntry(nick);
		//						t.addPlayer(p);
		//					}
		//				}
		//			}
		//		}
		//		storedNames.remove(id);

		//		nickNamer.sendPluginMessage(player, "name", "reset");

		//		updatePlayer(id, true, false, (boolean) getConfigOption("selfUpdate"));
		refreshPlayer(uuid);
	}

	@Nonnull
	@Override
	public List<UUID> getPlayersWithNick(@Nonnull String nick) {
		List<UUID> list = new ArrayList<>();
		for (Map.Entry<UUID, String> entry : nickedPlayers.entrySet()) {
			if (entry.getValue().equals(nick)) {
				list.add(entry.getKey());
			}
		}
		return Collections.unmodifiableList(list);
	}

	@Nonnull
	@Override
	public List<String> getUsedNicks() {
		return Collections.unmodifiableList(new ArrayList<>(nickedPlayers.values()));
	}

	@Override
	public void setSkin(@Nonnull final UUID uuid, @Nonnull final String skinOwner) {
		if (hasSkin(uuid)) {
			removeSkin(uuid);
		}

		//		@SuppressWarnings("deprecation")
		//		UUID skinID = Bukkit.getOfflinePlayer(skinOwner).getUniqueId();

		skins.put(uuid, skinOwner);

		//		nickNamer.sendPluginMessage(Bukkit.getPlayer(id), "skin", skinOwner);

		//		if (!Bukkit.getOnlineMode() && !NickNamer.BUNGEECORD) {
		//			UUIDResolver.resolve(skinOwner);
		//		} else {
		//			long l = SkinLoader.load(skinOwner);
		//			if (l == -1) {
		//				updatePlayer(id, false, true, NickNamer.SELF_UPDATE);
		//			}
		//			return l;
		Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				SkinLoader.loadSkin(skinOwner);
				//				Object profile = SkinLoader.loadSkin(skinOwner);
				//				updatePlayer(id, false, true, (boolean) getConfigOption("selfUpdate"));
				refreshPlayer(uuid);
			}
		}, 2);
		//		}
	}

	@Override
	public void loadCustomSkin(@Nonnull String key, @Nonnull Object gameProfile) {
		SkinLoader.skinMap.put(key, gameProfile);
	}

	@Override
	public void loadCustomSkin(@Nonnull String key, @Nonnull GameProfileWrapper profileWrapper) {
		loadCustomSkin(key, profileWrapper.getHandle());
	}

	@Override
	@Deprecated
	public void loadCustomSkin(String key, JSONObject data) {
		if (key == null || data == null) { throw new IllegalArgumentException("key and data cannot be null"); }
		if (!data.containsKey("properties")) { throw new IllegalArgumentException("JSONObject must contain 'properties' entry"); }
		//		SkinLoader.skinStorage.put(key, data);
		loadCustomSkin(key, new GameProfileWrapper(data));
		//		loadCustomSkin(key, SkinLoader.toProfile(key.length() > 16 ? key.substring(0, 16) : key, data));
	}

	@Override
	public void setCustomSkin(@Nonnull UUID uuid, @Nonnull String skin) {
		if (!SkinLoader.skinMap.containsKey(skin)) { throw new IllegalStateException("Specified skin has not been loaded yet"); }
		skins.put(uuid, skin);

		//		updatePlayer(id, false, true, (boolean) getConfigOption("selfUpdate"));
		refreshPlayer(uuid);
	}

	@Override
	public void removeSkin(@Nonnull UUID uuid) {
		skins.remove(uuid);

		//		nickNamer.sendPluginMessage(Bukkit.getPlayer(id), "skin", "reset");

		//		updatePlayer(id, false, true, (boolean) getConfigOption("selfUpdate"));
		refreshPlayer(uuid);
	}

	@Override
	public String getSkin(@Nonnull UUID uuid) {
		if (hasSkin(uuid)) { return skins.get(uuid); }
		Player player = Bukkit.getPlayer(uuid);
		return player != null ? player.getName() : null;
	}

	@Override
	public boolean hasSkin(@Nonnull UUID uuid) {
		return skins.containsKey(uuid);
	}

	@Nonnull
	@Override
	public List<UUID> getPlayersWithSkin(@Nonnull String skin) {
		List<UUID> list = new ArrayList<>();
		for (Map.Entry<UUID, String> entry : skins.entrySet()) {
			if (entry.getValue().equals(skin)) {
				list.add(entry.getKey());
			}
		}
		return Collections.unmodifiableList(list);
	}

	@Override
	public void refreshPlayer(@Nonnull UUID uuid) {
		final Player player = Bukkit.getPlayer(uuid);
		if (player == null || !player.isOnline()) { return; }

		PlayerRefreshEvent refreshEvent = new PlayerRefreshEvent(player, true);
		Bukkit.getPluginManager().callEvent(refreshEvent);
		if (refreshEvent.isCancelled()) { return; }

		if (refreshEvent.isSelf()) {
			updateSelf(player);
		}

		Bukkit.getScheduler().runTask(plugin, new Runnable() {

			@Override
			public void run() {
				List<Player> canSee = new ArrayList<>();
				for (Player player1 : Bukkit.getOnlinePlayers()) {
					if (player1.canSee(player)) {
						canSee.add(player1);
						player1.hidePlayer(player);
					}
				}
				for (Player player1 : canSee) {
					player1.showPlayer(player);
				}
			}
		});
	}

	protected void updateSelf(final Player player) {
		if (player == null || !player.isOnline()) { return; }
		Object profile = ClassBuilder.getGameProfile(player);

		NickNamerSelfUpdateEvent event = new NickNamerSelfUpdateEvent(player, isNicked(player.getUniqueId()) ? getNick(player.getUniqueId()) : player.getPlayerListName(), profile, player.getWorld().getDifficulty(), player.getGameMode());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) { return; }

		try {
			final Object removePlayer = ClassBuilder.buildPlayerInfoPacket(4, event.getGameProfile(), 0, event.getGameMode().ordinal(), event.getName());
			final Object addPlayer = ClassBuilder.buildPlayerInfoPacket(0, event.getGameProfile(), 0, event.getGameMode().ordinal(), event.getName());
			Object difficulty = EnumDifficulty.getDeclaredMethod("getById", int.class).invoke(null, event.getDifficulty().getValue());
			Object type = ((Object[]) WorldType.getDeclaredField("types").get(null))[0];
			Object gamemode = EnumGamemode.getDeclaredMethod("getById", int.class).invoke(null, event.getGameMode().getValue());
			final Object respawnPlayer = SkinLoader.nmsClassResolver.resolve("PacketPlayOutRespawn").getConstructor(int.class, EnumDifficulty, WorldType, EnumGamemode).newInstance(0, difficulty, type, gamemode);

			NickNamerAPI.packetListener.sendPacket(player, removePlayer);

			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					boolean flying = player.isFlying();
					Location location = player.getLocation();
					int level = player.getLevel();
					float xp = player.getExp();

					NickNamerAPI.packetListener.sendPacket(player, respawnPlayer);

					player.setFlying(flying);
					player.teleport(location);
					player.updateInventory();
					player.setLevel(level);
					player.setExp(xp);

					NickNamerAPI.packetListener.sendPacket(player, addPlayer);
				}
			}, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	@Deprecated
	public void updatePlayer(Player player, boolean updateName, boolean updateSkin, boolean updateSelf) {
		refreshPlayer(player.getUniqueId());
	}

	@Override
	@Deprecated
	public void updatePlayer(UUID uuid, boolean updateName, boolean updateSkin, boolean updateSelf) {
		refreshPlayer(uuid);
	}

}
