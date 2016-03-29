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

import org.inventivetalent.nicknamer.api.wrapper.GameProfileWrapper;
import org.inventivetalent.reflection.resolver.ClassResolver;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

	protected static Map<String, Object> skinMap = new ConcurrentHashMap<>();

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
		if (profile == null) {
			try {
				Object cache = TileEntitySkullFieldResolver.resolve("skinCache").get(null);
				profile = LoadingCacheMethodResolver.resolve("getUnchecked").invoke(cache, owner.toLowerCase());
				if (profile != null) { skinMap.put(owner, profile); }
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
		return profile;
	}

	@Nullable
	public static Object getSkinProfileHandle(@Nonnull String owner) {
		Object profile = skinMap.get(owner);
		if (profile == null) {
			try {
				Object cache = TileEntitySkullFieldResolver.resolve("skinCache").get(null);
				profile = CacheMethodResolver.resolve("getIfPresent").invoke(cache, owner);
				if (profile != null) { skinMap.put(owner, profile); }
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
		return profile;
	}

}
