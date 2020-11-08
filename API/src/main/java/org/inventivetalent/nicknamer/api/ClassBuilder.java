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

import org.bukkit.entity.Player;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.minecraft.MinecraftVersion;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;

import java.util.List;

public class ClassBuilder {

	@SuppressWarnings({
							  "rawtypes",
							  "unchecked" })
	public static Object buildPlayerInfoPacket(int action, Object profile, int ping, int gamemodeOrdinal, String name) {
		try {
			Object packet = PacketPlayOutPlayerInfo.newInstance();

			if (MinecraftVersion.VERSION.olderThan(Minecraft.Version.v1_8_R1)) {
				PacketPlayOutPlayerInfoFieldResolver.resolve("action").set(packet, action);
				PacketPlayOutPlayerInfoFieldResolver.resolve("player").set(packet, profile);
				PacketPlayOutPlayerInfoFieldResolver.resolve("gamemode").set(packet, gamemodeOrdinal);
				PacketPlayOutPlayerInfoFieldResolver.resolve("ping").set(packet, ping);
				PacketPlayOutPlayerInfoFieldResolver.resolve("username").set(packet, name);
			} else {
				PacketPlayOutPlayerInfoFieldResolver.resolve("a").set(packet, EnumPlayerInfoAction.getEnumConstants()[action]);
				List list = (List) PacketPlayOutPlayerInfoFieldResolver.resolve("b").get(packet);

				Object data;
				// if (NPCLib.getServerVersion() <= 181) {
				data = PlayerInfoData.getConstructor(PacketPlayOutPlayerInfo, getNMUtilClass("com.mojang.authlib.GameProfile"), int.class, EnumGamemode, IChatBaseComponent).newInstance(packet, profile, ping, EnumGamemode.getEnumConstants()[gamemodeOrdinal], buildChatComponent(name));
				// } else {
				// data = PlayerInfoData.getConstructor(getNMUtilClass("com.mojang.authlib.GameProfile"), int.class, EnumGamemode,
				// Reflection.getNMSClass("IChatBaseComponent")).newInstance(profile, ping, EnumGamemode.getEnumConstants()[gamemodeOrdinal], buildChatComponent(name));
				// }
				list.add(data);
			}
			return packet;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object buildChatComponent(String string) {
		try {
			Object[] components = (Object[]) CraftChatMessageMethodResolver.resolve(new ResolverQuery("fromString", String.class)).invoke(null, string);
			if (components.length > 0) {
				return components[0];
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public static Object getGameProfile(Player player) {
		try {
			return EntityHumanMethodResolver.resolve("getProfile").invoke(Minecraft.getHandle(player));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Class<?> getNMUtilClass(String name) throws ClassNotFoundException {
		if (MinecraftVersion.VERSION.olderThan(Minecraft.Version.v1_8_R1)) { return Class.forName("net.minecraft.util." + name); } else { return Class.forName(name); }
	}

	static NMSClassResolver nmsClassResolver = new NMSClassResolver();
	static OBCClassResolver obcClassResolver = new OBCClassResolver();

	static Class<?> PacketPlayOutPlayerInfo = nmsClassResolver.resolveSilent("PacketPlayOutPlayerInfo");
	static Class<?> PlayerInfoData          = nmsClassResolver.resolveSilent("PlayerInfoData", "PacketPlayOutPlayerInfo$PlayerInfoData");
	static Class<?> EnumPlayerInfoAction    = nmsClassResolver.resolveSilent("EnumPlayerInfoAction", "PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
	static Class<?> EnumGamemode            = nmsClassResolver.resolveSilent("EnumGamemode", "WorldSettings$EnumGamemode");
	static Class<?> EntityHuman             = nmsClassResolver.resolveSilent("EntityHuman");
	static Class<?> IChatBaseComponent      = nmsClassResolver.resolveSilent("IChatBaseComponent");

	static Class<?> CraftChatMessage = obcClassResolver.resolveSilent("util.CraftChatMessage");

	static MethodResolver EntityHumanMethodResolver      = new MethodResolver(EntityHuman);
	static MethodResolver CraftChatMessageMethodResolver = new MethodResolver(CraftChatMessage);

	static FieldResolver PacketPlayOutPlayerInfoFieldResolver = new FieldResolver(PacketPlayOutPlayerInfo);

}
