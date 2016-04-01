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

import com.google.common.base.Predicate;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class UUIDResolver {

	static final String URL_FORMAT = "https://api.mojang.com/users/profiles/minecraft/%s";

	final Executor executor = Executors.newSingleThreadExecutor();
	private Plugin plugin;
	private long   expiration;
	File              dataFile;
	YamlConfiguration configuration;

	public UUIDResolver(Plugin plugin, long expiration) {
		this.plugin = plugin;
		this.expiration = expiration;
		File pluginDir = new File(plugin.getDataFolder().getParentFile(), "NickNamer");// Make sure to use NickNamer, in case it's been loaded with APIManager
		dataFile = new File(pluginDir, "uuids.map");
		configuration = YamlConfiguration.loadConfiguration(dataFile);
	}

	UUID getLocalUUID(String name) {
		name = name.toLowerCase();
		if (!configuration.contains(name)) {
			return null;
		} else {
			long expiration = configuration.getLong(name + ".expiration");
			if (expiration > System.currentTimeMillis()) {
				configuration.set(name, null);
				return null;
			}
			return UUID.fromString(configuration.getString(name + ".id"));
		}
	}

	void setLocalUUID(String name, UUID uuid) {
		name = name.toLowerCase();
		configuration.set(name + ".id", uuid.toString());
		configuration.set(name + ".expiration", System.currentTimeMillis() + expiration);
	}

	public void getIdForName(final String name, final Predicate<UUID> callback) {
		UUID local = getLocalUUID(name);
		if (local != null) {
			if (callback.apply(local)) { return; }
		}

		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					UUID liveId = getLiveUUID(name);
					if (liveId != null) {
						setLocalUUID(name, liveId);
						try {
							configuration.save(dataFile);
						} catch (Exception e) {
							plugin.getLogger().log(Level.SEVERE, "Failed to save UUID-Map", e);
						}

						callback.apply(liveId);
					}
				} catch (IOException e) {
					plugin.getLogger().log(Level.SEVERE, "Failed to get live UUID for " + name, e);
				}
			}
		});

	}

	UUID getLiveUUID(String name) throws IOException {
		URLConnection connection = new URL(String.format(URL_FORMAT, name)).openConnection();
		JsonElement parsed = new JsonParser().parse(new InputStreamReader(connection.getInputStream()));
		if (parsed != null && parsed instanceof JsonObject) {
			String id = parsed.getAsJsonObject().get("id").getAsString();
			return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
		}
		return null;
	}

}
