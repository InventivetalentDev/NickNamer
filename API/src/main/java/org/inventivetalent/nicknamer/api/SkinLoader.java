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
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SkinLoader {

    static ClassResolver classResolver = new ClassResolver();
    static NMSClassResolver nmsClassResolver = new NMSClassResolver();

    static Class<?> TileEntitySkull = nmsClassResolver.resolveSilent("TileEntitySkull", "world.level.block.entity.TileEntitySkull");
    static Class<?> Cache = classResolver.resolveSilent("net.minecraft.util.com.google.common.cache.Cache", "com.google.common.cache.Cache");
    static Class<?> LoadingCache = classResolver.resolveSilent("net.minecraft.util.com.google.common.cache.LoadingCache", "com.google.common.cache.LoadingCache");
    static Class<?> GameProfile = classResolver.resolveSilent("net.minecraft.util.com.mojang.authlib.GameProfile", "com.mojang.authlib.GameProfile");
    static Class<?> PropertyMap = classResolver.resolveSilent("net.minecraft.util.com.mojang.authlib.properties.PropertyMap", "com.mojang.authlib.properties.PropertyMap");

    static FieldResolver TileEntitySkullFieldResolver = new FieldResolver(TileEntitySkull);
    static FieldResolver GameProfileFieldResolver = new FieldResolver(GameProfile);

    static MethodResolver CacheMethodResolver = new MethodResolver(Cache);
    static MethodResolver LoadingCacheMethodResolver = new MethodResolver(LoadingCache);

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
					future.complete(jsonToProfile(jsonObject));
				}
			});
			try {
				profile = future.get(10, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw new RuntimeException(e);
			}
			if (profile != null) {
				return profile;
			}
		}
		if (profile == null) {
			try {
                Object cache = TileEntitySkullFieldResolver.resolve("skinCache", "b").get(null);
				profile = (GameProfile) LoadingCacheMethodResolver.resolve("getUnchecked").invoke(cache, owner.toLowerCase());
                if (profile != null) {
                    skinDataProvider.put(owner, profileToJson(profile));
                    boolean async = !Bukkit.getServer().isPrimaryThread();
                    Bukkit.getPluginManager().callEvent(new SkinLoadedEvent(owner, new GameProfileWrapper(profile), async));
                }
            } catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
		return profile;
	}

	@Nullable
	public static GameProfile getSkinProfileHandle(@Nonnull String owner) {
		GameProfile profile = jsonToProfile(skinDataProvider.get(owner));
		if (profile == null) {
			try {
				Object cache = TileEntitySkullFieldResolver.resolve("skinCache", "b").get(null);
				profile = (GameProfile) CacheMethodResolver.resolve("getIfPresent").invoke(cache, owner);
				if (profile != null) {
					skinDataProvider.put(owner, profileToJson(profile));
					boolean async = !Bukkit.getServer().isPrimaryThread();
					Bukkit.getPluginManager().callEvent(new SkinLoadedEvent(owner, new GameProfileWrapper(profile), async));
				}
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
		return profile;
	}

	public static void refreshCachedData(@Nonnull String owner) {
		if (skinDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			((AsyncCacheMapper.CachedDataProvider) skinDataProvider).refresh(owner);
		}
	}

}
