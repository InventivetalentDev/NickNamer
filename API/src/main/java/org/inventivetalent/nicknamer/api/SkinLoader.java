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
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import org.bukkit.Bukkit;
import org.inventivetalent.data.DataProvider;
import org.inventivetalent.data.async.DataCallback;
import org.inventivetalent.data.mapper.AsyncCacheMapper;
import org.inventivetalent.data.mapper.MapMapper;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;
import org.inventivetalent.nicknamer.api.event.skin.SkinLoadedEvent;
import org.inventivetalent.reflection.resolver.ClassResolver;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SkinLoader {

	static ClassResolver classResolver = new ClassResolver();
	static NMSClassResolver nmsClassResolver = new NMSClassResolver();

	static Class<?> TileEntitySkull = nmsClassResolver.resolveSilent("TileEntitySkull", "world.level.block.entity.TileEntitySkull");
	static Class<?> UserCache = nmsClassResolver.resolveSilent("server.players.UserCache");

	static FieldResolver TileEntitySkullFieldResolver = new FieldResolver(TileEntitySkull);

	static MethodResolver UserCacheMethodResolver = new MethodResolver(UserCache);


	protected static DataProvider<JsonObject> skinDataProvider;

	static {
		setSkinDataProvider(MapMapper.sync(new HashMap<String, JsonObject>()));
	}

	public static void setSkinDataProvider(DataProvider<JsonObject> skinDataProvider) {
		SkinLoader.skinDataProvider = skinDataProvider;
	}

	static GameProfile jsonToProfile(JsonObject jsonObject) {
		if (jsonObject == null) { return null; }
		return (com.mojang.authlib.GameProfile) new GameProfileWrapper(jsonObject).getHandle();
	}

	static JsonObject profileToJson(GameProfile profile) {
		if (profile == null) { return null; }
		JsonObject jsonObject = new GameProfileWrapper(profile).toJson();
		return jsonObject;
	}

	//Should be called asynchronously
	@Nullable
	public static GameProfile loadSkin(@Nonnull String owner) {
		return loadSkinHandle(owner);
	}

	@Nullable
	public static GameProfile getSkinProfile(@Nonnull String owner) {
		return getSkinProfileHandle(owner);
	}

	//Should be called asynchronously
	@Nullable
	public static GameProfile loadSkinHandle(@Nonnull String owner) {
		GameProfile profile = getSkinProfileHandle(owner);
		if (profile == null && skinDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			CompletableFuture<GameProfile> future = new CompletableFuture<>();
			((AsyncCacheMapper.CachedDataProvider<JsonObject>) skinDataProvider).get(owner, new DataCallback<JsonObject>() {
				@Override
				public void provide(@Nullable JsonObject jsonObject) {
					System.out.println(jsonObject);
					future.complete(jsonToProfile(jsonObject));
				}
			});
			try {
				profile = future.get(2, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
			}
			if (profile != null) {
				return profile;
			}
		}
		if (profile == null) {
			profile = getCachedProfile(owner);
		}
		return profile;
	}

	@Nullable
	public static GameProfile getSkinProfileHandle(@Nonnull String owner) {
		GameProfile profile = jsonToProfile(skinDataProvider.get(owner));
		if (profile == null) {
			profile = getCachedProfile(owner);
		}
		return profile;
	}

	public static GameProfile getCachedProfile(String owner) {
		GameProfile profile;
		try {
			Object cache = TileEntitySkullFieldResolver.resolve("skinCache", "b").get(null);
			Object profileObj = UserCacheMethodResolver.resolve(new ResolverQuery("getProfile", String.class)).invoke(cache, owner);
			if (profileObj instanceof Optional) {
				profile = (GameProfile) ((Optional) profileObj).orElse(null);
			} else {
				profile = (GameProfile) profileObj;
			}
			if (profile != null) { // make sure we have textures
				if (!profile.getProperties().containsKey("textures")) {
					MinecraftSessionService sessionService = (MinecraftSessionService) TileEntitySkullFieldResolver.resolve("sessionService", "c").get(null);
					sessionService.fillProfileProperties(profile, true);
					// put the filled profile back into the UserCache
					UserCacheMethodResolver.resolve(new ResolverQuery("a", GameProfile.class)).invoke(cache, profile);
				}
			}
			if (profile != null) {
				skinDataProvider.put(owner, profileToJson(profile));
				boolean async = !Bukkit.getServer().isPrimaryThread();
				Bukkit.getPluginManager().callEvent(new SkinLoadedEvent(owner, new GameProfileWrapper(profile), async));
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
		return profile;
	}

	public static void refreshCachedData(@Nonnull String owner) {
		if (skinDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			((AsyncCacheMapper.CachedDataProvider) skinDataProvider).refresh(owner);
		}
	}

}
