package org.inventivetalent.nicknamer.api;

import org.bukkit.entity.Player;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;

import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ClassBuilder {

	static NMSClassResolver nmsClassResolver = new NMSClassResolver();
	static OBCClassResolver obcClassResolver = new OBCClassResolver();
	static Class<?> PacketPlayOutPlayerInfo = nmsClassResolver.resolveSilent("PacketPlayOutPlayerInfo");
	static Class<?> PlayerInfoData = nmsClassResolver.resolveSilent("PlayerInfoData", "PacketPlayOutPlayerInfo$PlayerInfoData");
	static Class<?> EnumPlayerInfoAction = nmsClassResolver.resolveSilent("EnumPlayerInfoAction", "PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
	static Class<?> EnumGamemode = nmsClassResolver.resolveSilent("EnumGamemode", "WorldSettings$EnumGamemode");
	static Class<?> EntityHuman = nmsClassResolver.resolveSilent("EntityHuman");
	static Class<?> IChatBaseComponent = nmsClassResolver.resolveSilent("IChatBaseComponent");
	static Class<?> CraftChatMessage = obcClassResolver.resolveSilent("util.CraftChatMessage");
	static MethodResolver EntityHumanMethodResolver = new MethodResolver(EntityHuman);
	static MethodResolver CraftChatMessageMethodResolver = new MethodResolver(CraftChatMessage);
	static FieldResolver PacketPlayOutPlayerInfoFieldResolver = new FieldResolver(PacketPlayOutPlayerInfo);

	@SuppressWarnings({
			"rawtypes",
			"unchecked"})
	public static Object buildPlayerInfoPacket(int action, Object profile, int ping, int gamemodeOrdinal, String name) {
		try {
			Object packet = PacketPlayOutPlayerInfo.newInstance();

			if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_8_R1)) {
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
		if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_8_R1)) {
			return Class.forName("net.minecraft.util." + name);
		} else {
			return Class.forName(name);
		}
	}

}
