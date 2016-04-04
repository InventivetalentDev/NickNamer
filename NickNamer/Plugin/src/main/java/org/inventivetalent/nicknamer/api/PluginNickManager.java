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

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.data.api.DataProvider;
import org.inventivetalent.data.api.temporary.ConcurrentTemporaryDataProvider;
import org.inventivetalent.data.api.wrapper.WrappedKeyDataProvider;
import org.inventivetalent.nicknamer.api.event.NickNamerSelfUpdateEvent;
import org.inventivetalent.nicknamer.api.event.refresh.PlayerRefreshEvent;
import org.inventivetalent.nicknamer.api.wrapper.GameProfileWrapper;
import org.json.simple.JSONObject;
import org.spigotmc.CustomTimingsHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PluginNickManager implements NickManager {

	Class EnumDifficulty = SkinLoader.nmsClassResolver.resolveSilent("EnumDifficulty");
	Class WorldType      = SkinLoader.nmsClassResolver.resolveSilent("WorldType");
	Class EnumGamemode   = SkinLoader.nmsClassResolver.resolveSilent("WorldSettings$EnumGamemode", "EnumGamemode");

	private Plugin plugin;

	final WrappedKeyDataProvider<UUID, String> nickDataProvider = new WrappedKeyDataProvider<UUID, String>(String.class, new ConcurrentTemporaryDataProvider<>(String.class)) {
		@Override
		public UUID stringToKey(@NonNull String s) {
			return UUID.fromString(s);
		}
	};
	final WrappedKeyDataProvider<UUID, String> skinDataProvider = new WrappedKeyDataProvider<UUID, String>(String.class, new ConcurrentTemporaryDataProvider<>(String.class)) {
		@Override
		public UUID stringToKey(@NonNull String s) {
			return UUID.fromString(s);
		}
	};

	public void setNickDataProvider(DataProvider<String> provider) {
		nickDataProvider.setDataProvider(provider);
	}

	public void setSkinDataProvider(DataProvider<String> provider) {
		skinDataProvider.setDataProvider(provider);
	}

	public PluginNickManager(Plugin plugin) {
		this.plugin = plugin;
		NickNamerAPI.nickManager = this;
	}

	CustomTimingsHandler isNicked = new CustomTimingsHandler("isNicked");

	@Override
	public boolean isNicked(@Nonnull UUID uuid) {
		isNicked.startTiming();
		boolean b =/* nickDataProvider.contains(uuid) && */nickDataProvider.get(uuid) != null;
		isNicked.stopTiming();
		return b;
	}

	CustomTimingsHandler isNickUsed = new CustomTimingsHandler("isNickUsed");

	@Override
	public boolean isNickUsed(@Nonnull String nick) {
		isNickUsed.startTiming();
		for (UUID uuid : nickDataProvider.keysK()) {
			if (nick.equals(nickDataProvider.get(uuid))) {
				isNickUsed.stopTiming();
				return true;
			}
		}
		isNickUsed.stopTiming();
		return false;
	}

	CustomTimingsHandler getNick = new CustomTimingsHandler("getNick");

	@Override
	public String getNick(@Nonnull UUID id) {
		getNick.startTiming();
		String s = nickDataProvider.get(id);
		getNick.stopTiming();
		return s;
	}

	CustomTimingsHandler setNick     = new CustomTimingsHandler("setNick");
	CustomTimingsHandler setNickTask = new CustomTimingsHandler("setNickTask");

	@Override
	public void setNick(@Nonnull final UUID uuid, @Nonnull final String nick) {
		if (nick.length() > 16) { throw new IllegalArgumentException("Name is too long (" + nick.length() + " > 16)"); }
		setNick.startTiming();
		if (isNicked(uuid)) {
			removeNick(uuid);
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				setNickTask.startTiming();
				nickDataProvider.put(uuid, nick);

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
				//Wait for database
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					@Override
					public void run() {
						refreshPlayer(uuid);
					}
				}, 10);
				setNickTask.stopTiming();
			}
		});

		setNick.stopTiming();
	}

	CustomTimingsHandler removeNick     = new CustomTimingsHandler("removeNick");
	CustomTimingsHandler removeNickTask = new CustomTimingsHandler("removeNickTask");

	@Override
	public void removeNick(@Nonnull final UUID uuid) {
		removeNick.startTiming();
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				removeNickTask.startTiming();
				nickDataProvider.remove(uuid);

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
				removeNickTask.stopTiming();
			}
		});
		removeNick.stopTiming();
	}

	CustomTimingsHandler getPlayersWithNick = new CustomTimingsHandler("getPlayersWithNick");

	@Nonnull
	@Override
	public List<UUID> getPlayersWithNick(@Nonnull String nick) {
		getPlayersWithNick.startTiming();
		List<UUID> list = new ArrayList<>();
		for (UUID uuid : nickDataProvider.keysK()) {
			if (nick.equals(nickDataProvider.get(uuid))) { list.add(uuid); }
		}
		//		for (Map.Entry<UUID, String> entry : nicks.entrySet()) {
		//			if (entry.getValue().equals(nick)) {
		//				list.add(entry.getKey());
		//			}
		//		}
		getPlayersWithNick.stopTiming();
		return list;
	}

	CustomTimingsHandler getUsedNicks = new CustomTimingsHandler("getUsedNicks");

	@Nonnull
	@Override
	public List<String> getUsedNicks() {
		getUsedNicks.startTiming();
		List<String> nicks = new ArrayList<>();
		for (UUID uuid : nickDataProvider.keysK()) {
			String nick = nickDataProvider.get(uuid);
			if (nick != null) { nicks.add(nick); }
		}
		getUsedNicks.stopTiming();
		return nicks;
	}

	CustomTimingsHandler setSkin     = new CustomTimingsHandler("setSkin");
	CustomTimingsHandler setSkinTask = new CustomTimingsHandler("setSkinTask");

	@Override
	public void setSkin(@Nonnull final UUID uuid, @Nonnull final String skinOwner) {
		setSkin.startTiming();
		if (hasSkin(uuid)) {
			removeSkin(uuid);
		}

		Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				setSkinTask.startTiming();
				//		@SuppressWarnings("deprecation")
				//		UUID skinID = Bukkit.getOfflinePlayer(skinOwner).getUniqueId();

				skinDataProvider.put(uuid, skinOwner);

				//		nickNamer.sendPluginMessage(Bukkit.getPlayer(id), "skin", skinOwner);

				//		if (!Bukkit.getOnlineMode() && !NickNamer.BUNGEECORD) {
				//			UUIDResolver.resolve(skinOwner);
				//		} else {
				//			long l = SkinLoader.load(skinOwner);
				//			if (l == -1) {
				//				updatePlayer(id, false, true, NickNamer.SELF_UPDATE);
				//			}
				//			return l;
				SkinLoader.loadSkin(skinOwner);
				//				Object profile = SkinLoader.loadSkin(skinOwner);
				//				updatePlayer(id, false, true, (boolean) getConfigOption("selfUpdate"));
				refreshPlayer(uuid);
				setSkinTask.stopTiming();
			}
		}, 2);

		setSkin.stopTiming();
		//		}
	}

	@Override
	public void loadCustomSkin(@Nonnull String key, @Nonnull Object gameProfile) {
		SkinLoader.skinDataProvider.put(key, gameProfile);
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
		//		SkinLoader.skinDataProvidertorage.put(key, data);
		loadCustomSkin(key, new GameProfileWrapper(data));
		//		loadCustomSkin(key, SkinLoader.toProfile(key.length() > 16 ? key.substring(0, 16) : key, data));
	}

	@Override
	public void setCustomSkin(@Nonnull UUID uuid, @Nonnull String skin) {
		if (!SkinLoader.skinDataProvider.contains(skin)) { throw new IllegalStateException("Specified skin has not been loaded yet"); }
		skinDataProvider.put(uuid, skin);

		//		updatePlayer(id, false, true, (boolean) getConfigOption("selfUpdate"));
		refreshPlayer(uuid);
	}

	CustomTimingsHandler removeSkin     = new CustomTimingsHandler("removeSkin");
	CustomTimingsHandler removeSkinTask = new CustomTimingsHandler("removeSkinTask");

	@Override
	public void removeSkin(@Nonnull final UUID uuid) {
		removeSkin.startTiming();
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				removeSkinTask.startTiming();
				skinDataProvider.remove(uuid);

				//		nickNamer.sendPluginMessage(Bukkit.getPlayer(id), "skin", "reset");

				//		updatePlayer(id, false, true, (boolean) getConfigOption("selfUpdate"));
				refreshPlayer(uuid);
				removeSkinTask.stopTiming();
			}
		});
		removeSkin.stopTiming();
	}

	CustomTimingsHandler getSkin = new CustomTimingsHandler("getSkin");

	@Override
	public String getSkin(@Nonnull UUID uuid) {
		getSkin.startTiming();
		if (hasSkin(uuid)) { return skinDataProvider.get(uuid); }
		Player player = Bukkit.getPlayer(uuid);
		getSkin.stopTiming();
		return player != null ? player.getName() : null;
	}

	CustomTimingsHandler hasSkin = new CustomTimingsHandler("hasSkin");

	@Override
	public boolean hasSkin(@Nonnull UUID uuid) {
		hasSkin.startTiming();
		boolean b = /*skinDataProvider.contains(uuid) &&*/ skinDataProvider.get(uuid) != null;
		hasSkin.stopTiming();
		return b;
	}

	CustomTimingsHandler getPlayersWithSkin = new CustomTimingsHandler("getPlayersWithSkin");

	@Nonnull
	@Override
	public List<UUID> getPlayersWithSkin(@Nonnull String skin) {
		getPlayersWithSkin.startTiming();
		List<UUID> list = new ArrayList<>();
		for (UUID uuid : skinDataProvider.keysK()) {
			if (skin.equals(skinDataProvider.get(uuid))) { list.add(uuid); }
		}
		//		for (Map.Entry<UUID, String> entry : skinDataProvider.entrySet()) {
		//			if (entry.getValue().equals(skin)) {
		//				list.add(entry.getKey());
		//			}
		//		}
		getPlayersWithSkin.stopTiming();
		return list;
	}

	CustomTimingsHandler refreshPlayer = new CustomTimingsHandler("refreshPlayer");

	@Override
	public void refreshPlayer(@Nonnull UUID uuid) {
		final Player player = Bukkit.getPlayer(uuid);
		if (player == null || !player.isOnline()) { return; }
		refreshPlayer.startTiming();

		PlayerRefreshEvent refreshEvent = new PlayerRefreshEvent(player, true);
		Bukkit.getPluginManager().callEvent(refreshEvent);
		if (refreshEvent.isCancelled()) {
			refreshPlayer.stopTiming();
			return;
		}

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

		refreshPlayer.stopTiming();
	}

	CustomTimingsHandler updateSelf = new CustomTimingsHandler("updateSelf");

	protected void updateSelf(final Player player) {
		if (player == null || !player.isOnline()) { return; }
		updateSelf.startTiming();
		Object profile = ClassBuilder.getGameProfile(player);

		NickNamerSelfUpdateEvent event = new NickNamerSelfUpdateEvent(player, isNicked(player.getUniqueId()) ? getNick(player.getUniqueId()) : player.getPlayerListName(), profile, player.getWorld().getDifficulty(), player.getGameMode());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			updateSelf.stopTiming();
			return;
		}

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
		updateSelf.stopTiming();
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
