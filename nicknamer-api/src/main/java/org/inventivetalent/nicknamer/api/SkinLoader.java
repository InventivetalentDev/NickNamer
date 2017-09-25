package org.inventivetalent.nicknamer.api;

import com.google.gson.JsonObject;
import lombok.NonNull;
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

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SkinLoader {

	protected static DataProvider<JsonObject> skinDataProvider;
	static ClassResolver classResolver = new ClassResolver();
	static NMSClassResolver nmsClassResolver = new NMSClassResolver();
	static Class<?> TileEntitySkull = nmsClassResolver.resolveSilent("TileEntitySkull");
	static Class<?> Cache = classResolver.resolveSilent("net.minecraft.util.com.google.common.cache.Cache", "com.google.common.cache.Cache");
	static Class<?> LoadingCache = classResolver.resolveSilent("net.minecraft.util.com.google.common.cache.LoadingCache", "com.google.common.cache.LoadingCache");
	static Class<?> GameProfile = classResolver.resolveSilent("net.minecraft.util.com.mojang.authlib.GameProfile", "com.mojang.authlib.GameProfile");
	static Class<?> PropertyMap = classResolver.resolveSilent("net.minecraft.util.com.mojang.authlib.properties.PropertyMap", "com.mojang.authlib.properties.PropertyMap");
	static FieldResolver TileEntitySkullFieldResolver = new FieldResolver(TileEntitySkull);
	static FieldResolver GameProfileFieldResolver = new FieldResolver(GameProfile);
	static MethodResolver CacheMethodResolver = new MethodResolver(Cache);
	static MethodResolver LoadingCacheMethodResolver = new MethodResolver(LoadingCache);

	static {
		setSkinDataProvider(MapMapper.sync(new HashMap<String, JsonObject>()));
	}

	public static void setSkinDataProvider(DataProvider<JsonObject> skinDataProvider) {
		SkinLoader.skinDataProvider = skinDataProvider;
	}

	/*
	public static void setSkinDataProvider(SerializationDataProvider<Object> skinDataProvider) {
		SkinLoader.skinDataProvider = skinDataProvider;
		SkinLoader.skinDataProvider.setSerializer(new GsonDataSerializer<Object>() {
			@Override
			public String serialize(@NonNull Object object) {
				JsonObject jsonObject = new GameProfileWrapper(object).toJson();
				jsonObject.addProperty("loadTime", System.currentTimeMillis());
				return jsonObject.toString();
			}
		});
		SkinLoader.skinDataProvider.setParser(new GsonDataParser<Object>(Object.class) {
			@Override
			public Object parse(@NonNull String string) {
				JsonObject jsonObject = new JsonParser().parse(string).getAsJsonObject();
				if (jsonObject.has("loadTime")) {
					if (System.currentTimeMillis() - jsonObject.get("loadTime").getAsLong() > 3600000 {
						return null;//return null, so the updated skin can be inserted
					}
				}
				return new GameProfileWrapper(jsonObject).getHandle();
			}
		});
	}
	*/

	static Object jsonToProfile(JsonObject jsonObject) {
		if (jsonObject == null) {
			return null;
		}
		return new GameProfileWrapper(jsonObject).getHandle();
	}

	static JsonObject profileToJson(Object profile) {
		if (profile == null) {
			return null;
		}
		return new GameProfileWrapper(profile).toJson();
	}

	//Should be called asynchronously
	public static GameProfileWrapper loadSkin(@NonNull String owner) {
		Object handle = loadSkinHandle(owner);
		if (handle == null) {
			return null;
		}
		return new GameProfileWrapper(handle);
	}

	public static GameProfileWrapper getSkinProfile(@NonNull String owner) {
		Object handle = getSkinProfileHandle(owner);
		if (handle == null) {
			return null;
		}
		return new GameProfileWrapper(handle);
	}

	//Should be called asynchronously
	public static Object loadSkinHandle(@NonNull String owner) {
		Object profile = getSkinProfileHandle(owner);
		if (profile == null && skinDataProvider instanceof AsyncCacheMapper.CachedDataProvider) {
			final CountDownLatch latch = new CountDownLatch(1);
			final Object[] profile1 = new Object[1];
			((AsyncCacheMapper.CachedDataProvider<JsonObject>) skinDataProvider).get(owner, new DataCallback<JsonObject>() {
				@Override
				public void provide(JsonObject jsonObject) {
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
					Bukkit.getPluginManager().callEvent(new SkinLoadedEvent(owner, new GameProfileWrapper(profile)));
				}
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
		return profile;
	}

	public static Object getSkinProfileHandle(@NonNull String owner) {
		Object profile = jsonToProfile(skinDataProvider.get(owner));
		if (profile == null) {
			try {
				Object cache = TileEntitySkullFieldResolver.resolve("skinCache").get(null);
				profile = CacheMethodResolver.resolve("getIfPresent").invoke(cache, owner);
				if (profile != null) {
					skinDataProvider.put(owner, profileToJson(profile));
					Bukkit.getPluginManager().callEvent(new SkinLoadedEvent(owner, new GameProfileWrapper(profile)));
				}
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
		return profile;
	}

}
