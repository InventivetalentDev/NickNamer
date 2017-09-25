package org.inventivetalent.nicknamer.api;

import com.google.gson.JsonObject;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.inventivetalent.data.DataProvider;
import org.inventivetalent.data.async.DataCallback;
import org.inventivetalent.data.mapper.AsyncCacheMapper;
import org.inventivetalent.data.mapper.MapMapper;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;
import org.inventivetalent.nicknamer.NickNamerPlugin;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PluginNickManager extends SimpleNickManager {

	DataProvider<String> nickDataProvider = MapMapper.sync(new HashMap<String, String>());
	DataProvider<String> skinDataProvider = MapMapper.sync(new HashMap<String, String>());

	/*
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
	*/

	public PluginNickManager(NickNamerPlugin plugin) {
		super(plugin);
		NickNamerAPI.nickManager = this;
	}

	public void setNickDataProvider(DataProvider<String> provider) {
		this.nickDataProvider = provider;
	}

	public void setSkinDataProvider(DataProvider<String> provider) {
		this.skinDataProvider = provider;
	}

	@Override
	public boolean isNicked(@NonNull UUID uuid) {
		return nickDataProvider.contains(uuid.toString());
	}

	public void isNicked(@NonNull UUID uuid, @NonNull DataCallback<Boolean> callback) {
		if (nickDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			((AsyncCacheMapper.CachedDataProvider) nickDataProvider).contains(uuid.toString(), callback);
		} else {
			callback.provide(false);
		}
	}

	@Override
	public boolean isNickUsed(@NonNull String nick) {
		for (String uuid : nickDataProvider.keys()) {
			if (nick.equals(nickDataProvider.get(uuid))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getNick(@NonNull UUID id) {
		return nickDataProvider.get(id.toString());
	}

	public void getNick(@NonNull UUID uuid, DataCallback<String> callback) {
		if (nickDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			((AsyncCacheMapper.CachedDataProvider<String>) nickDataProvider).get(uuid.toString(), callback);
		} else {
			callback.provide(null);
		}
	}

	@Override
	public void setNick(@NonNull final UUID uuid, @NonNull final String nick) {
		if (nick.length() > 16) {
			throw new IllegalArgumentException("Name is too long (" + nick.length() + " > 16)");
		}
		if (nickDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			((AsyncCacheMapper.CachedDataProvider<String>) nickDataProvider).put(uuid.toString(), () -> {
				Bukkit.getScheduler().runTaskLater(plugin, () -> refreshPlayer(uuid), 20);
				return nick;
			});
		} else {
			nickDataProvider.put(uuid.toString(), nick);
		}
	}

	@Override
	public void removeNick(@NonNull final UUID uuid) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			nickDataProvider.remove(uuid.toString());

            /*
			Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if (getConfigOption("nick.chat")) {
                    p.setDisplayName(storedNames.get(id));
                }
                if (getConfigOption("nick.tab")) {
                    p.setPlayerListName(storedNames.get(id));
                }
                if (getConfigOption("nick.scoreboard")) {
                    Scoreboard sb = p.getScoreboard();
                    if (sb == null) {
                        sb = Bukkit.getScoreboardManager().getMainScoreboard();
                    }
                    if (sb != null) {
                        Team t = null;
                        for (Team tm : sb.getTeams()) {
                            for (String s : tm.getEntries()) {
                                if (s.equals(nick)) {
                                    t = tm;
                                    break;
                                }
                            }
                        }
                        if (t != null) {
                            t.removeEntry(nick);
                            t.addPlayer(p);
                        }
                    }
                }
            }
            storedNames.remove(id);

            nickNamer.sendPluginMessage(player, "name", "reset");

            updatePlayer(id, true, false, (boolean) getConfigOption("selfUpdate"));
            */

			Bukkit.getScheduler().runTaskLater(plugin, () -> refreshPlayer(uuid), 10);
		});

		if (((NickNamerPlugin) plugin).bungeecord) {
			((NickNamerPlugin) plugin).sendPluginMessage(Bukkit.getPlayer(uuid), "name", "reset");
		}
	}

	@NonNull
	@Override
	public List<UUID> getPlayersWithNick(@NonNull String nick) {
		List<UUID> list = new ArrayList<>();
		for (String uuid : nickDataProvider.keys()) {
			if (nick.equals(nickDataProvider.get(uuid))) {
				list.add(UUID.fromString(uuid));
			}
		}
		/*
		for (Map.Entry<UUID, String> entry : nicks.entrySet()) {
			if (entry.getValue().equals(nick)) {
				list.add(entry.getKey());
			}
		}
		*/
		return list;
	}

	@NonNull
	@Override
	public List<String> getUsedNicks() {
		List<String> nicks = new ArrayList<>();
		for (String uuid : nickDataProvider.keys()) {
			String nick = nickDataProvider.get(uuid);
			if (nick != null) {
				nicks.add(nick);
			}
		}
		return nicks;
	}

	@Override
	public void setSkin(@NonNull final UUID uuid, @NonNull final String skinOwner) {
		if (skinDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			((AsyncCacheMapper.CachedDataProvider<String>) skinDataProvider).put(uuid.toString(), () -> {
				SkinLoader.loadSkin(skinOwner);
				Bukkit.getScheduler().runTaskLater(plugin, () -> refreshPlayer(uuid), 20);
				return skinOwner;
			});
		} else {
			skinDataProvider.put(uuid.toString(), skinOwner);
		}

		if (((NickNamerPlugin) plugin).bungeecord) {
			((NickNamerPlugin) plugin).sendPluginMessage(Bukkit.getPlayer(uuid), "skin", skinOwner);
		}
	}

	@Override
	public void loadCustomSkin(@NonNull String key, @NonNull Object gameProfile) {
		loadCustomSkin(key, new GameProfileWrapper(gameProfile));
	}

	@Override
	public void loadCustomSkin(@NonNull String key, @NonNull GameProfileWrapper profileWrapper) {
		SkinLoader.skinDataProvider.put(key, profileWrapper.toJson());
	}

	@Override
	public void loadCustomSkin(@NonNull String key, @NonNull JsonObject data) {
		if (!data.has("properties")) {
			throw new IllegalArgumentException("JsonObject must contain 'properties' entry");
		}
		loadCustomSkin(key, new GameProfileWrapper(data));
	}

	@Override
	@Deprecated
	public void loadCustomSkin(String key, JSONObject data) {
		if (key == null || data == null) {
			throw new IllegalArgumentException("key and data cannot be null");
		}
		if (!data.containsKey("properties")) {
			throw new IllegalArgumentException("JSONObject must contain 'properties' entry");
		}
		//SkinLoader.skinDataProvidertorage.put(key, data);
		loadCustomSkin(key, new GameProfileWrapper(data));
		//loadCustomSkin(key, SkinLoader.toProfile(key.length() > 16 ? key.substring(0, 16) : key, data));
	}

	@Override
	public void setCustomSkin(@NonNull UUID uuid, @NonNull String skin) {
		if (!SkinLoader.skinDataProvider.contains(skin)) {
			throw new IllegalStateException("Specified skin has not been loaded yet");
		}
		skinDataProvider.put(uuid.toString(), skin);

		//updatePlayer(id, false, true, (boolean) getConfigOption("selfUpdate"));
		refreshPlayer(uuid);
	}

	@Override
	public void removeSkin(@NonNull final UUID uuid) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				skinDataProvider.remove(uuid.toString());

				//nickNamer.sendPluginMessage(Bukkit.getPlayer(id), "skin", "reset");

				//updatePlayer(id, false, true, (boolean) getConfigOption("selfUpdate"));
				Bukkit.getScheduler().runTaskLater(plugin, () -> refreshPlayer(uuid), 10);
			}
		});

		if (((NickNamerPlugin) plugin).bungeecord) {
			((NickNamerPlugin) plugin).sendPluginMessage(Bukkit.getPlayer(uuid), "skin", "reset");
		}
	}

	@Override
	public String getSkin(@NonNull UUID uuid) {
		return skinDataProvider.get(uuid.toString());
	}

	public void getSkin(@NonNull UUID uuid, DataCallback<String> callback) {
		if (skinDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			((AsyncCacheMapper.CachedDataProvider<String>) skinDataProvider).get(uuid.toString(), callback);
		} else {
			callback.provide(null);
		}
	}

	@Override
	public boolean hasSkin(@NonNull UUID uuid) {
		return skinDataProvider.contains(uuid.toString());
	}

	public void hasSkin(@NonNull UUID uuid, DataCallback<Boolean> callback) {
		if (skinDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			((AsyncCacheMapper.CachedDataProvider) skinDataProvider).contains(uuid.toString(), callback);
		} else {
			callback.provide(false);
		}
	}

	@NonNull
	@Override
	public List<UUID> getPlayersWithSkin(@NonNull String skin) {
		List<UUID> list = new ArrayList<>();
		for (String uuid : skinDataProvider.keys()) {
			if (skin.equals(skinDataProvider.get(uuid))) {
				list.add(UUID.fromString(uuid));
			}
		}
		/*
		for (Map.Entry<UUID, String> entry : skinDataProvider.entrySet()) {
			if (entry.getValue().equals(skin)) {
				list.add(entry.getKey());
			}
		}
		*/
		return list;
	}

	@NonNull
	@Override
	public List<String> getUsedSkins() {
		List<String> skins = new ArrayList<>();
		for (String uuid : skinDataProvider.keys()) {
			String nick = skinDataProvider.get(uuid);
			if (nick != null) {
				skins.add(nick);
			}
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
