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

package org.inventivetalent.nicknamer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.inventivetalent.apihelper.APIManager;
import org.inventivetalent.data.api.SerializationDataProvider;
import org.inventivetalent.data.api.bukkit.ebean.EbeanDataProvider;
import org.inventivetalent.data.api.bukkit.ebean.KeyValueBean;
import org.inventivetalent.data.api.redis.RedisDataProvider;
import org.inventivetalent.data.api.sql.SQLDataProvider;
import org.inventivetalent.data.api.wrapper.AsyncDataProviderWrapper;
import org.inventivetalent.data.api.wrapper.CachedAsyncDataProviderWrapper;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;
import org.inventivetalent.nicknamer.api.*;
import org.inventivetalent.nicknamer.api.event.disguise.NickDisguiseEvent;
import org.inventivetalent.nicknamer.api.event.disguise.SkinDisguiseEvent;
import org.inventivetalent.nicknamer.api.event.replace.*;
import org.inventivetalent.nicknamer.api.event.skin.SkinLoadedEvent;
import org.inventivetalent.nicknamer.command.NickCommands;
import org.inventivetalent.nicknamer.command.SkinCommands;
import org.inventivetalent.nicknamer.database.NickEntry;
import org.inventivetalent.nicknamer.database.SkinDataEntry;
import org.inventivetalent.nicknamer.database.SkinEntry;
import org.inventivetalent.packetlistener.PacketListenerAPI;
import org.inventivetalent.pluginannotations.PluginAnnotations;
import org.inventivetalent.pluginannotations.config.ConfigValue;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NickNamerPlugin extends JavaPlugin implements Listener, PluginMessageListener, INickNamer {

	public static NickNamerPlugin instance;

	public NickCommands nickCommands;
	public SkinCommands skinCommands;

	final Executor storageExecutor = Executors.newSingleThreadExecutor();

	//	@ConfigValue(path = "replace.tab") boolean replaceTab;
	@ConfigValue(path = "replace.chat.player")     boolean replaceChatPlayer;
	@ConfigValue(path = "replace.chat.out")        boolean replaceChatOut;
	@ConfigValue(path = "replace.chat.in.general") boolean replaceChatInGeneral;
	@ConfigValue(path = "replace.chat.in.command") boolean replaceChatInCommand;
	@ConfigValue(path = "replace.scoreboard")      boolean replaceScoreboard;

	@ConfigValue(path = "bungeecord") public boolean bungeecord;

	@ConfigValue(path = "storage.type") String storageType = "local";

	@ConfigValue(path = "storage.sql.address") String sqlAddress;
	//	@ConfigValue(path = "storage.sql.host") String sqlHost;
	//	@ConfigValue(path = "storage.sql.port") int    sqlPort;
	@ConfigValue(path = "storage.sql.user")    String sqlUser;
	@ConfigValue(path = "storage.sql.pass")    String sqlPass;

	@ConfigValue(path = "storage.redis.host")            String redisHost;
	@ConfigValue(path = "storage.redis.port")            int    redisPort;
	@ConfigValue(path = "storage.redis.pass")            String redisPass;
	@ConfigValue(path = "storage.redis.max-connections") int    redisMaxConnections;

	@Override
	public void onLoad() {
		APIManager.require(PacketListenerAPI.class, this);
		APIManager.registerAPI(new NickNamerAPI(), this);
	}

	@Override
	public void onEnable() {
		instance = this;
		APIManager.initAPI(PacketListenerAPI.class);
		APIManager.initAPI(NickNamerAPI.class);

		Bukkit.getPluginManager().registerEvents(this, this);

		saveDefaultConfig();
		PluginAnnotations.CONFIG.loadValues(this, this);

		PluginAnnotations.COMMAND.registerCommands(this, nickCommands = new NickCommands(this));
		PluginAnnotations.COMMAND.registerCommands(this, skinCommands = new SkinCommands(this));

		if (bungeecord) {
			if (Bukkit.getOnlineMode()) {
				getLogger().warning("Bungeecord is enabled, but server is in online mode!");
			}
			Bukkit.getMessenger().registerIncomingPluginChannel(this, "NickNamer", this);
			Bukkit.getMessenger().registerOutgoingPluginChannel(this, "NickNamer");
		}

		//Replace the default NickManager
		new PluginNickManager(this);

		switch (storageType.toLowerCase()) {
			case "temporary":
				getLogger().info("Using temporary storage");
				break;
			case "local":
				getLogger().info("Using local storage");
				initStorageLocal();
				break;
			case "sql":
				getLogger().info("Using SQL storage (" + sqlUser + "@" + sqlAddress + ")");
				initStorageSQL();
				break;
			case "redis":
				getLogger().info("Using Redis storage (" + redisHost + ":" + redisPort + ")");
				initStorageRedis();
				break;
		}
	}

	@Override
	public void onDisable() {
		APIManager.disableAPI(NickNamerAPI.class);
	}

	<D> SerializationDataProvider<D> wrapAsyncProvider(Class<? extends D> clazz, SerializationDataProvider<D> dataProvider) {
		return new CachedAsyncDataProviderWrapper<>(clazz, new AsyncDataProviderWrapper<D>(dataProvider) {
			@Override
			public void dispatch(Runnable runnable) {
				storageExecutor.execute(runnable);
			}
		});
	}

	void initStorageLocal() {
		int nickCount = -1;
		int skinCount = -1;
		int dataCount = -1;
		try {
			nickCount = getDatabase().find(NickEntry.class).findRowCount();
			skinCount = getDatabase().find(SkinEntry.class).findRowCount();
			dataCount = getDatabase().find(SkinDataEntry.class).findRowCount();
		} catch (PersistenceException e) {
			getLogger().info("Installing database");
			installDDL();
		}
		if (nickCount > 0) {
			getLogger().info("Found " + nickCount + " player nick-data in database");
		}
		((PluginNickManager) NickNamerAPI.getNickManager()).setNickDataProvider(wrapAsyncProvider(String.class, new EbeanDataProvider<>(String.class, getDatabase(), NickEntry.class)));
		if (dataCount > 0) {
			getLogger().info("Found " + skinCount + " player skin-data in database");
		}
		((PluginNickManager) NickNamerAPI.getNickManager()).setSkinDataProvider(wrapAsyncProvider(String.class, new EbeanDataProvider<>(String.class, getDatabase(), SkinEntry.class)));

		if (dataCount > 0) {
			getLogger().info("Found " + dataCount + " skin textures in database");
			for (SkinDataEntry entry : getDatabase().find(SkinDataEntry.class).findSet()) {
				if (System.currentTimeMillis() - entry.getLoadTime() > 3600000/*1 hour*/) {
					getLogger().info("Deleting old skin for " + entry.getKey());
				}
			}
		}

		SkinLoader.setSkinDataProvider(new EbeanDataProvider<Object>(Object.class/*We're using a custom parser/serializer, so this class doesn't matter*/, getDatabase(), SkinDataEntry.class) {
			@Override
			public KeyValueBean newBean() {
				SkinDataEntry bean = new SkinDataEntry();
				bean.setLoadTime(System.currentTimeMillis());
				return bean;
			}
		});
	}

	void initStorageSQL() {
		if (sqlPass == null || sqlPass.isEmpty()) { sqlPass = null; }

		Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
			@Override
			public void run() {
				//				getLogger().info("Connected to SQL");

				((PluginNickManager) NickNamerAPI.getNickManager()).setNickDataProvider(wrapAsyncProvider(String.class, new SQLDataProvider<>(String.class, sqlAddress, sqlUser, sqlPass, "nicknamer_data_nick")));
				((PluginNickManager) NickNamerAPI.getNickManager()).setSkinDataProvider(wrapAsyncProvider(String.class, new SQLDataProvider<>(String.class, sqlAddress, sqlUser, sqlPass, "nicknamer_data_skin")));
				SkinLoader.setSkinDataProvider(new SQLDataProvider<>(Object.class, sqlAddress, sqlUser, sqlPass, "nicknamer_skins"));
			}
		});
	}

	void initStorageRedis() {
		if (redisPass == null || redisPass.isEmpty()) { redisPass = null; }

		Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
			@Override
			public void run() {
				JedisPoolConfig config = new JedisPoolConfig();
				config.setMaxTotal(redisMaxConnections);
				final JedisPool pool = new JedisPool(config, redisHost, redisPort, 0, redisPass);
				try (Jedis jedis = pool.getResource()) {
					jedis.ping();
					getLogger().info("Connected to Redis");

					((PluginNickManager) NickNamerAPI.getNickManager()).setNickDataProvider(wrapAsyncProvider(String.class, new RedisDataProvider<>(String.class, pool, "nn_data:%s:nick", "nn_data:(.*):nick")));
					((PluginNickManager) NickNamerAPI.getNickManager()).setSkinDataProvider(wrapAsyncProvider(String.class, new RedisDataProvider<>(String.class, pool, "nn_data:%s:skin", "nn_data:(.*):skin")));
					SkinLoader.setSkinDataProvider(new RedisDataProvider<Object>(Object.class, pool, "nn_skins:%s", "nn_skins:(.*)") {
						@Override
						public void put(@NonNull String key, Object value) {
							try (Jedis jedis = pool.getResource()) {
								jedis.setex(key(key), 3600, getSerializer().serialize(value));
							}
						}
					});
				} catch (JedisConnectionException e) {
					pool.destroy();
					throw new RuntimeException("Failed to connect to Redis", e);
				}
			}
		});
	}

	@Override
	public List<Class<?>> getDatabaseClasses() {
		List<Class<?>> list = new ArrayList<>();
		list.add(SkinDataEntry.class);
		list.add(NickEntry.class);
		list.add(SkinEntry.class);
		return list;
	}

	// Internal event listeners

	@EventHandler(priority = EventPriority.LOWEST)
	public void on(NickDisguiseEvent event) {
		if (getAPI().isNicked(event.getPlayer().getUniqueId())) {
			event.setNick(getAPI().getNick(event.getPlayer().getUniqueId()));
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void on(SkinDisguiseEvent event) {
		if (getAPI().hasSkin(event.getPlayer().getUniqueId())) {
			event.setSkin(getAPI().getSkin(event.getPlayer().getUniqueId()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void on(SkinLoadedEvent event) {
		if (bungeecord) {
			if (Bukkit.getOnlinePlayers().isEmpty()) {
				getLogger().warning("Cannot send skin data to Bungeecord: no players online");
				return;
			}
			sendPluginMessage(Bukkit.getOnlinePlayers().iterator().next(), "data", event.getOwner(), event.getGameProfile().toJson().toString());
		}
	}

	// Name replacement listeners
	@EventHandler(priority = EventPriority.NORMAL)
	public void on(final AsyncPlayerChatEvent event) {
		if (ChatReplacementEvent.getHandlerList().getRegisteredListeners().length > 0) {
			final String message = event.getMessage();
			Set<String> nickedPlayerNames = NickNamerAPI.getNickedPlayerNames();
			String replacedMessage = NickNamerAPI.replaceNames(message, nickedPlayerNames, new NameReplacer() {
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

	@EventHandler(priority = EventPriority.NORMAL)
	public void on(final PlayerJoinEvent event) {
		if (PlayerJoinReplacementEvent.getHandlerList().getRegisteredListeners().length > 0) {
			final String message = event.getJoinMessage();
			Set<String> nickedPlayerNames = NickNamerAPI.getNickedPlayerNames();
			String replacedMessage = NickNamerAPI.replaceNames(message, nickedPlayerNames, new NameReplacer() {
				@Override
				public String replace(String original) {
					Player player = Bukkit.getPlayer(original);
					if (player != null) {
						PlayerJoinReplacementEvent replacementEvent = new PlayerJoinReplacementEvent(player, Bukkit.getOnlinePlayers(), message, original, original);
						Bukkit.getPluginManager().callEvent(replacementEvent);
						if (replacementEvent.isCancelled()) { return original; }
						return replacementEvent.getReplacement();
					}
					return original;
				}
			}, true);
			event.setJoinMessage(replacedMessage);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void on(final PlayerQuitEvent event) {
		if (PlayerJoinReplacementEvent.getHandlerList().getRegisteredListeners().length > 0) {
			final String message = event.getQuitMessage();
			Set<String> nickedPlayerNames = NickNamerAPI.getNickedPlayerNames();
			String replacedMessage = NickNamerAPI.replaceNames(message, nickedPlayerNames, new NameReplacer() {
				@Override
				public String replace(String original) {
					Player player = Bukkit.getPlayer(original);
					if (player != null) {
						PlayerQuitReplacementEvent replacementEvent = new PlayerQuitReplacementEvent(player, Bukkit.getOnlinePlayers(), message, original, original);
						Bukkit.getPluginManager().callEvent(replacementEvent);
						if (replacementEvent.isCancelled()) { return original; }
						return replacementEvent.getReplacement();
					}
					return original;
				}
			}, true);
			event.setQuitMessage(replacedMessage);
		}
	}

	//// Replacement listeners

	@EventHandler(priority = EventPriority.LOW)
	public void on(ChatReplacementEvent event) {
		System.out.println(event);
		if (replaceChatPlayer) {
			if (NickNamerAPI.getNickManager().isNicked(event.getPlayer().getUniqueId())) {
				event.setReplacement(NickNamerAPI.getNickManager().getNick(event.getPlayer().getUniqueId()));
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void on(ChatOutReplacementEvent event) {
		replaceChatOut = true;
		System.out.println(event);
		if (replaceChatOut) {
			if (NickNamerAPI.getNickManager().isNicked(event.getPlayer().getUniqueId())) {
				event.setReplacement(NickNamerAPI.getNickManager().getNick(event.getPlayer().getUniqueId()));
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void on(ChatInReplacementEvent event) {
		System.out.println(event);
		if (replaceChatInGeneral || replaceChatInCommand) {
			if (replaceChatInCommand && event.getContext().startsWith("/")) { // Command
				if (NickNamerAPI.getNickManager().isNicked(event.getPlayer().getUniqueId())) {
					event.setReplacement(NickNamerAPI.getNickManager().getNick(event.getPlayer().getUniqueId()));
				}
			} else if (replaceChatInGeneral) {
				if (NickNamerAPI.getNickManager().isNicked(event.getPlayer().getUniqueId())) {
					event.setReplacement(NickNamerAPI.getNickManager().getNick(event.getPlayer().getUniqueId()));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void on(ScoreboardReplacementEvent event) {
		System.out.println(event);
		if (replaceScoreboard) {
			if (NickNamerAPI.getNickManager().isNicked(event.getPlayer().getUniqueId())) {
				event.setReplacement(NickNamerAPI.getNickManager().getNick(event.getPlayer().getUniqueId()));
			}
		}
	}

	@Override
	public NickManager getAPI() {
		return NickNamerAPI.getNickManager();
	}

	@Override
	public void sendPluginMessage(Player player, String action, String... values) {
		if (!bungeecord) { return; }
		if (player == null || !player.isOnline()) { return; }

		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(action);
		out.writeUTF(player.getUniqueId().toString());
		for (String s : values) {
			out.writeUTF(s);
		}
		player.sendPluginMessage(instance, "NickNamer", out.toByteArray());
	}

	@Override
	public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
		if (!bungeecord) { return; }
		if ("NickNamer".equals(s)) {
			ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
			String sub = in.readUTF();
			UUID who = UUID.fromString(in.readUTF());
			if ("name".equals(sub)) {
				String name = in.readUTF();

				if (name == null || "reset".equals(name)) {
					getAPI().removeNick(who);
				} else {
					getAPI().setNick(who, name);
				}
			} else if ("skin".equals(sub)) {
				String skin = in.readUTF();

				if (skin == null || "reset".equals(skin)) {
					getAPI().removeSkin(who);
				} else {
					getAPI().setSkin(who, skin);
				}
			} else if ("data".equals(sub)) {
				try {
					String owner = in.readUTF();
					JsonObject data = new JsonParser().parse(in.readUTF()).getAsJsonObject();
					SkinLoaderBridge.getSkinProvider().put(owner, new GameProfileWrapper(data));
				} catch (JsonParseException e) {
					e.printStackTrace();
				}
			} else {
				getLogger().warning("Unknown incoming plugin message: " + sub);
			}
		}
	}
}

