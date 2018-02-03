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

import com.google.gson.JsonObject;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.inventivetalent.data.DataProvider;
import org.inventivetalent.data.async.DataCallable;
import org.inventivetalent.data.async.DataCallback;
import org.inventivetalent.data.mapper.AsyncCacheMapper;
import org.inventivetalent.data.mapper.MapMapper;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;
import org.inventivetalent.nicknamer.NickNamerPlugin;
import org.json.simple.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PluginNickManager extends SimpleNickManager {

	DataProvider<String> nickDataProvider = MapMapper.sync(new HashMap<String, String>());
	DataProvider<String> skinDataProvider = MapMapper.sync(new HashMap<String, String>());

	//	final WrappedKeyDataProvider<UUID, String> nickDataProvider = new WrappedKeyDataProvider<UUID, String>(String.class, new ConcurrentTemporaryDataProvider<>(String.class)) {
	//		@Override
	//		public UUID stringToKey(@NonNull String s) {
	//			return UUID.fromString(s);
	//		}
	//	};
	//	final WrappedKeyDataProvider<UUID, String> skinDataProvider = new WrappedKeyDataProvider<UUID, String>(String.class, new ConcurrentTemporaryDataProvider<>(String.class)) {
	//		@Override
	//		public UUID stringToKey(@NonNull String s) {
	//			return UUID.fromString(s);
	//		}
	//	};

	public void setNickDataProvider(DataProvider<String> provider) {
		this.nickDataProvider = provider;
	}

	public void setSkinDataProvider(DataProvider<String> provider) {
		this.skinDataProvider = provider;
	}

	public PluginNickManager(NickNamerPlugin plugin) {
		super(plugin);
		NickNamerAPI.nickManager = this;
	}

	@Override
	public boolean isNicked(@Nonnull UUID uuid) {
		return nickDataProvider.contains(uuid.toString());
	}

	public void isNicked(@Nonnull UUID uuid, @Nonnull DataCallback<Boolean> callback) {
		if (nickDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			((AsyncCacheMapper.CachedDataProvider) nickDataProvider).contains(uuid.toString(), callback);
		} else {
			callback.provide(false);
		}
	}

	@Override
	public boolean isNickUsed(@Nonnull String nick) {
		for (String uuid : nickDataProvider.keys()) {
			if (nick.equals(nickDataProvider.get(uuid))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getNick(@Nonnull UUID id) {
		return nickDataProvider.get(id.toString());
	}

	public void getNick(@Nonnull UUID uuid, DataCallback<String> callback) {
		if (nickDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			((AsyncCacheMapper.CachedDataProvider<String>) nickDataProvider).get(uuid.toString(), callback);
		} else {
			callback.provide(null);
		}
	}

	@Override
	public void setNick(@Nonnull final UUID uuid, @Nonnull final String nick) {
		if (nick.length() > 16) { throw new IllegalArgumentException("Name is too long (" + nick.length() + " > 16)"); }
		if (nickDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			((AsyncCacheMapper.CachedDataProvider<String>) nickDataProvider).put(uuid.toString(), new DataCallable<String>() {
				@Nonnull
				@Override
				public String provide() {
					Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
						@Override
						public void run() {
							refreshPlayer(uuid);
						}
					}, 20);
					return nick;
				}
			});
		} else {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				@Override
				public void run() {
					nickDataProvider.put(uuid.toString(), nick);
					Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
						@Override
						public void run() {
							refreshPlayer(uuid);
						}
					}, 20);
				}
			});

		}
	}

	@Override
	public void removeNick(@Nonnull final UUID uuid) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				nickDataProvider.remove(uuid.toString());

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
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					@Override
					public void run() {
						refreshPlayer(uuid);
					}
				}, 10);
			}
		});

		if (((NickNamerPlugin) plugin).bungeecord) { ((NickNamerPlugin) plugin).sendPluginMessage(Bukkit.getPlayer(uuid), "name", "reset"); }
	}

	@Nonnull
	@Override
	public List<UUID> getPlayersWithNick(@Nonnull String nick) {
		List<UUID> list = new ArrayList<>();
		for (String uuid : nickDataProvider.keys()) {
			if (nick.equals(nickDataProvider.get(uuid))) { list.add(UUID.fromString(uuid)); }
		}
		//		for (Map.Entry<UUID, String> entry : nicks.entrySet()) {
		//			if (entry.getValue().equals(nick)) {
		//				list.add(entry.getKey());
		//			}
		//		}
		return list;
	}

	@Nonnull
	@Override
	public List<String> getUsedNicks() {
		List<String> nicks = new ArrayList<>();
		for (String uuid : nickDataProvider.keys()) {
			String nick = nickDataProvider.get(uuid);
			if (nick != null) { nicks.add(nick); }
		}
		return nicks;
	}

	@Override
	public void setSkin(@Nonnull final UUID uuid, @Nonnull final String skinOwner, @Nullable final Callback callback) {
		if (skinDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			((AsyncCacheMapper.CachedDataProvider<String>) skinDataProvider).put(uuid.toString(), new DataCallable<String>() {
				@Nonnull
				@Override
				public String provide() {
					SkinLoader.loadSkin(skinOwner);
					Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
						@Override
						public void run() {
							refreshPlayer(uuid);
						}
					}, 20);
					if (callback != null) {
						callback.call();
					}
					return skinOwner;
				}
			});
		} else {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				@Override
				public void run() {
					skinDataProvider.put(uuid.toString(), skinOwner);
					SkinLoader.loadSkin(skinOwner);
					Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
						@Override
						public void run() {
							refreshPlayer(uuid);
						}
					}, 20);
					if (callback != null) {
						callback.call();
					}
				}
			});
		}

		if (((NickNamerPlugin) plugin).bungeecord) { ((NickNamerPlugin) plugin).sendPluginMessage(Bukkit.getPlayer(uuid), "skin", skinOwner); }
	}

	@Override
	public void setSkin(@Nonnull UUID uuid, @Nonnull String skinOwner) {
		setSkin(uuid, skinOwner, null);
	}

	@Override
	public void loadCustomSkin(@Nonnull String key, @Nonnull Object gameProfile) {
		loadCustomSkin(key, new GameProfileWrapper(gameProfile));
	}

	@Override
	public void loadCustomSkin(@Nonnull String key, @Nonnull GameProfileWrapper profileWrapper) {
		SkinLoader.skinDataProvider.put(key, profileWrapper.toJson());
	}

	@Override
	public void loadCustomSkin(@Nonnull String key, @Nonnull JsonObject data) {
		if (!data.has("properties")) { throw new IllegalArgumentException("JsonObject must contain 'properties' entry"); }
		loadCustomSkin(key, new GameProfileWrapper(data));
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
		skinDataProvider.put(uuid.toString(), skin);

		//		updatePlayer(id, false, true, (boolean) getConfigOption("selfUpdate"));
		refreshPlayer(uuid);
	}

	@Override
	public void removeSkin(@Nonnull final UUID uuid) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				skinDataProvider.remove(uuid.toString());

				//		nickNamer.sendPluginMessage(Bukkit.getPlayer(id), "skin", "reset");

				//		updatePlayer(id, false, true, (boolean) getConfigOption("selfUpdate"));
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					@Override
					public void run() {
						refreshPlayer(uuid);
					}
				}, 10);
			}
		});

		if (((NickNamerPlugin) plugin).bungeecord) { ((NickNamerPlugin) plugin).sendPluginMessage(Bukkit.getPlayer(uuid), "skin", "reset"); }
	}

	@Override
	public String getSkin(@Nonnull UUID uuid) {
		return skinDataProvider.get(uuid.toString());
	}

	public void getSkin(@Nonnull UUID uuid, DataCallback<String> callback) {
		if (skinDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			((AsyncCacheMapper.CachedDataProvider<String>) skinDataProvider).get(uuid.toString(), callback);
		} else {
			callback.provide(null);
		}
	}

	@Override
	public boolean hasSkin(@Nonnull UUID uuid) {
		return skinDataProvider.contains(uuid.toString());
	}

	public void hasSkin(@Nonnull UUID uuid, DataCallback<Boolean> callback) {
		if (skinDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			((AsyncCacheMapper.CachedDataProvider) skinDataProvider).contains(uuid.toString(), callback);
		} else {
			callback.provide(false);
		}
	}

	@Nonnull
	@Override
	public List<UUID> getPlayersWithSkin(@Nonnull String skin) {
		List<UUID> list = new ArrayList<>();
		for (String uuid : skinDataProvider.keys()) {
			if (skin.equals(skinDataProvider.get(uuid))) { list.add(UUID.fromString(uuid)); }
		}
		//		for (Map.Entry<UUID, String> entry : skinDataProvider.entrySet()) {
		//			if (entry.getValue().equals(skin)) {
		//				list.add(entry.getKey());
		//			}
		//		}
		return list;
	}

	@NonNull
	@Override
	public List<String> getUsedSkins() {
		List<String> skins = new ArrayList<>();
		for (String uuid : skinDataProvider.keys()) {
			String nick = skinDataProvider.get(uuid);
			if (nick != null) { skins.add(nick); }
		}
		return skins;
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

	@Override
	public boolean isSimple() {
		return false;
	}
}
