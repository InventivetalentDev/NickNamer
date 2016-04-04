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

package org.inventivetalent.nicknamer.api.wrapper;

import com.google.gson.JsonObject;
import org.inventivetalent.reflection.resolver.ClassResolver;
import org.inventivetalent.reflection.resolver.ConstructorResolver;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.UUID;

public class GameProfileWrapper {

	static ClassResolver classResolver = new ClassResolver();

	static Class<?> GameProfile = classResolver.resolveSilent("net.minecraft.util.com.mojang.authlib.GameProfile", "com.mojang.authlib.GameProfile");

	static FieldResolver GameProfileFieldResolver = new FieldResolver(GameProfile);

	static ConstructorResolver GameProfileConstructorResolver = new ConstructorResolver(GameProfile);

	private final Object handle;

	public GameProfileWrapper(UUID uuid, String name) {
		try {
			this.handle = GameProfileConstructorResolver.resolve(new Class[] {
					UUID.class,
					String.class }).newInstance(uuid, name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public GameProfileWrapper(Object handle) {
		this.handle = handle;
	}

	public GameProfileWrapper(JsonObject jsonObject) {
		this(UUID.fromString(jsonObject.get("id").getAsString()), jsonObject.get("name").getAsString());
		getProperties().clear();
		if(jsonObject.has("properties")) {
			getProperties().putAll(new PropertyMapWrapper(jsonObject.get("properties").getAsJsonArray()));
		}
	}

	@Deprecated
	public GameProfileWrapper(JSONObject jsonObject) {
		this(UUID.fromString((String) jsonObject.get("id")), (String) jsonObject.get("name"));
		getProperties().clear();
		if(jsonObject.containsKey("properties")) {
			getProperties().putAll(new PropertyMapWrapper((JSONArray) jsonObject.get("properties")));
		}
	}

	public UUID getId() {
		try {
			return (UUID) GameProfileFieldResolver.resolve("id").get(getHandle());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getName() {
		try {
			return (String) GameProfileFieldResolver.resolve("name").get(getHandle());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setName(String name) {
		try {
			GameProfileFieldResolver.resolve("name").set(getHandle(), name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object getPropertiesHandle() {
		try {
			return GameProfileFieldResolver.resolve("properties").get(getHandle());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public PropertyMapWrapper getProperties() {
		return new PropertyMapWrapper(getPropertiesHandle());
	}

	public JsonObject toJson() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("id", getId()!=null?getId().toString():"");
		jsonObject.addProperty("name", getName());
		if(getProperties()!=null) {
			jsonObject.add("properties", getProperties().toJson());
		}
		return jsonObject;
	}

	public Object getHandle() {
		return handle;
	}

}
