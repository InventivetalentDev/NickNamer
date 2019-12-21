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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SkinLoader {

	static ClassResolver    classResolver    = new ClassResolver();
	static NMSClassResolver nmsClassResolver = new NMSClassResolver();

	static Class<?> TileEntitySkull = nmsClassResolver.resolveSilent("TileEntitySkull");
	static Class<?> Cache           = classResolver.resolveSilent("net.minecraft.util.com.google.common.cache.Cache", "com.google.common.cache.Cache");
	static Class<?> LoadingCache    = classResolver.resolveSilent("net.minecraft.util.com.google.common.cache.LoadingCache", "com.google.common.cache.LoadingCache");
	static Class<?> GameProfile     = classResolver.resolveSilent("net.minecraft.util.com.mojang.authlib.GameProfile", "com.mojang.authlib.GameProfile");
	static Class<?> PropertyMap     = classResolver.resolveSilent("net.minecraft.util.com.mojang.authlib.properties.PropertyMap", "com.mojang.authlib.properties.PropertyMap");

	static FieldResolver TileEntitySkullFieldResolver = new FieldResolver(TileEntitySkull);
	static FieldResolver GameProfileFieldResolver     = new FieldResolver(GameProfile);

	static MethodResolver CacheMethodResolver        = new MethodResolver(Cache);
	static MethodResolver LoadingCacheMethodResolver = new MethodResolver(LoadingCache);

	protected static DataProvider<JsonObject> skinDataProvider;

	static {
		setSkinDataProvider(MapMapper.sync(new HashMap<String, JsonObject>()));
	}

	public static void setSkinDataProvider(DataProvider<JsonObject> skinDataProvider) {
		SkinLoader.skinDataProvider = skinDataProvider;
	}

	//	public static void setSkinDataProvider(SerializationDataProvider<Object> skinDataProvider) {
	//		SkinLoader.skinDataProvider = skinDataProvider;
	//		SkinLoader.skinDataProvider.setSerializer(new GsonDataSerializer<Object>() {
	//			@Override
	//			public String serialize(@NonNull Object object) {
	//				JsonObject jsonObject = new GameProfileWrapper(object).toJson();
	//				jsonObject.addProperty("loadTime", System.currentTimeMillis());
	//				return jsonObject.toString();
	//			}
	//		});
	//		SkinLoader.skinDataProvider.setParser(new GsonDataParser<Object>(Object.class) {
	//			@Override
	//			public Object parse(@NonNull String string) {
	//				JsonObject jsonObject = new JsonParser().parse(string).getAsJsonObject();
	//				if (jsonObject.has("loadTime")) {
	//					if (System.currentTimeMillis() - jsonObject.get("loadTime").getAsLong() > 3600000/* 1 hour */) {
	//						return null;//return null, so the updated skin can be inserted
	//					}
	//				}
	//				return new GameProfileWrapper(jsonObject).getHandle();
	//			}
	//		});
	//	}

	static Object jsonToProfile(JsonObject jsonObject) {
		if (jsonObject == null) { return null; }
		return new GameProfileWrapper(jsonObject).getHandle();
	}

	static JsonObject profileToJson(Object profile) {
		if (profile == null) { return null; }
		JsonObject jsonObject = new GameProfileWrapper(profile).toJson();
		return jsonObject;
	}

	//Should be called asynchronously
	@Nullable
	public static GameProfileWrapper loadSkin(@Nonnull String owner) {
		Object handle = loadSkinHandle(owner);
		if (handle == null) { return null; }
		return new GameProfileWrapper(handle);
	}

	@Nullable
	public static GameProfileWrapper getSkinProfile(@Nonnull String owner) {
		Object handle = getSkinProfileHandle(owner);
		if (handle == null) { return null; }
		return new GameProfileWrapper(handle);
	}

	//Should be called asynchronously
	@Nullable
	public static Object loadSkinHandle(@Nonnull String owner) {
		Object profile = getSkinProfileHandle(owner);
		if (profile == null && skinDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			final CountDownLatch latch = new CountDownLatch(1);
			final Object[] profile1 = new Object[1];
			((AsyncCacheMapper.CachedDataProvider<JsonObject>) skinDataProvider).get(owner, new DataCallback<JsonObject>() {
				@Override
				public void provide(@Nullable JsonObject jsonObject) {
					profile1[0] = jsonToProfile(jsonObject);
					latch.countDown();
				}
			});
			try {
				latch.await(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			if (profile1[0] != null) {
				return profile1[0];
			}
		}
		if (profile == null) {
			try {
				Object cache = TileEntitySkullFieldResolver.resolve("skinCache").get(null);
				profile = LoadingCacheMethodResolver.resolve("getUnchecked").invoke(cache, owner.toLowerCase());
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
	public static Object getSkinProfileHandle(@Nonnull String owner) {
		Object profile = jsonToProfile(skinDataProvider.get(owner));
		if (profile == null) {
			try {
				Object cache = TileEntitySkullFieldResolver.resolve("skinCache").get(null);
				profile = CacheMethodResolver.resolve("getIfPresent").invoke(cache, owner);
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
