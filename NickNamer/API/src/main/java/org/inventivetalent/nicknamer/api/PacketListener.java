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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.nicknamer.api.event.NickNamerUpdateEvent;
import org.inventivetalent.nicknamer.api.event.disguise.NickDisguiseEvent;
import org.inventivetalent.nicknamer.api.event.disguise.SkinDisguiseEvent;
import org.inventivetalent.nicknamer.api.event.replace.ChatInReplacementEvent;
import org.inventivetalent.nicknamer.api.event.replace.ChatOutReplacementEvent;
import org.inventivetalent.nicknamer.api.event.replace.NameReplacer;
import org.inventivetalent.nicknamer.api.event.replace.ScoreboardReplacementEvent;
import org.inventivetalent.nicknamer.api.wrapper.GameProfileWrapper;
import org.inventivetalent.nicknamer.api.wrapper.PropertyMapWrapper;
import org.inventivetalent.packetlistener.handler.PacketHandler;
import org.inventivetalent.packetlistener.handler.PacketOptions;
import org.inventivetalent.packetlistener.handler.ReceivedPacket;
import org.inventivetalent.packetlistener.handler.SentPacket;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class PacketListener extends PacketHandler {

	static NMSClassResolver nmsClassResolver = new NMSClassResolver();
	static OBCClassResolver obcClassResolver = new OBCClassResolver();

	static Class<?> PlayerInfoData     = nmsClassResolver.resolveSilent("PacketPlayOutPlayerInfo$PlayerInfoData");// 1.8+ only
	static Class<?> CraftChatMessage   = obcClassResolver.resolveSilent("util.CraftChatMessage");
	static Class<?> IChatBaseComponent = nmsClassResolver.resolveSilent("IChatBaseComponent");
	static Class<?> PacketPlayInChat   = nmsClassResolver.resolveSilent("PacketPlayInChat");
	static Class<?> EnumChatFormat     = nmsClassResolver.resolveSilent("EnumChatFormat");

	static FieldResolver  PlayerInfoDataFieldResolver    = PlayerInfoData != null ? new FieldResolver(PlayerInfoData) : null;// 1.8+ only
	static MethodResolver CraftChatMessageMethodResolver = new MethodResolver(CraftChatMessage);
	static FieldResolver  PacketPlayInChatFieldResolver  = new FieldResolver(PacketPlayInChat);

	public PacketListener(Plugin plugin) {
		super(plugin);
	}

	@Override
	@PacketOptions(forcePlayer = true)
	public void onSend(final SentPacket packet) {
		if (packet.hasPlayer()) {
			//// Nametag / Skin disguise
			if ("PacketPlayOutNamedEntitySpawn".equals(packet.getPacketName()) && Minecraft.VERSION.olderThan(Minecraft.Version.v1_8_R1)) {
				try {
					Object profileHandle = packet.getPacketValue("b");
					if (profileHandle != null) {
						packet.setPacketValue("b", disguiseProfile(packet.getPlayer(), new GameProfileWrapper(profileHandle)).getHandle());
					}
				} catch (Exception e) {
					getPlugin().getLogger().log(Level.SEVERE, "Failed to disguise profile", e);
				}
			}
			if ("PacketPlayOutPlayerInfo".equals(packet.getPacketName())) {
				if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_8_R1) && (int) packet.getPacketValue("action") == 4) {
					return;// Cancel here if the player is currently being removed
				}
				try {
					// (only a profile in < 1.8, otherwise it's a list of PlayerInfoData)
					Object profileHandle = packet.getPacketValue(Minecraft.VERSION.olderThan(Minecraft.Version.v1_8_R1) ? "player" : "b");
					if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_8_R1)) {
						if (profileHandle != null) {
							GameProfileWrapper disguisedWrapper = disguiseProfile(packet.getPlayer(), new GameProfileWrapper(profileHandle));
							profileHandle = disguisedWrapper.getHandle();
							packet.setPacketValue("player", profileHandle);
							packet.setPacketValue("username", disguisedWrapper.getName());
						}
					} else {// PlayerInfoData
						List list = new ArrayList<>((List) profileHandle);
						for (Object object : list) {
							Field field = PlayerInfoDataFieldResolver.resolve("d");
							field.set(object, disguiseProfile(packet.getPlayer(), new GameProfileWrapper(field.get(object))).getHandle());
						}
					}
				} catch (Exception e) {
					getPlugin().getLogger().log(Level.SEVERE, "Failed to disguise profile", e);
				}
			}

			//TODO: Check if the plugin NickManager is enabled before replacing names
			//TODO: Check if any listeners are registered for the replacement events

			//// Name replacement
			if ("PacketPlayOutChat".equalsIgnoreCase(packet.getPacketName())) {
				Object a = packet.getPacketValue("a");
				try {
					final String message = (String) CraftChatMessageMethodResolver.resolve(new ResolverQuery("fromComponent", IChatBaseComponent, EnumChatFormat)).invoke(null, a, EnumChatFormat.getEnumConstants()[15]);
					final String replacedMessage = NickNamerAPI.replaceNames(message, NickNamerAPI.getNickedPlayerNames(), new NameReplacer() {
						@Override
						public String replace(String original) {
							Player player = Bukkit.getPlayer(original);
							if (player != null) {
								ChatOutReplacementEvent replacementEvent = new ChatOutReplacementEvent(player, packet.getPlayer(), message, original, original);
								Bukkit.getPluginManager().callEvent(replacementEvent);
								if (replacementEvent.isCancelled()) { return original; }
								return replacementEvent.getReplacement();
							}
							return original;
						}
					}, true);
					//					String raw = (String) ChatSerializer.getDeclaredMethod("a", IChatBaseComponent).invoke(null, a);
					//					for (Player player : Bukkit.getOnlinePlayers()) {
					//						if (NickNamer.getNickManager().isNicked(player.getUniqueId())) {
					//							String nick = NickNamer.getNickManager().getNick(player.getUniqueId());
					//							if (raw.toLowerCase().contains(player.getName().toLowerCase())) {
					//								raw = raw.replaceAll("(?i)" + player.getName().toLowerCase(), nick);
					//							}
					//						}
					//					}

					//					//Players that just logged out
					//					for (Iterator<UUID> iterator = NickNamer.offlinePlayers.listIterator(); iterator.hasNext(); ) {
					//						UUID uuid = iterator.next();
					//
					//						if (NickNamer.getNickManager().isNicked(uuid)) {
					//							OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
					//							if (offlinePlayer != null) {
					//								if (offlinePlayer.isOnline()) {
					//									iterator.remove();
					//									break;
					//								}
					//								String name = offlinePlayer.getName();
					//								String nick = NickNamer.getNickManager().getNick(uuid);
					//								if (raw.contains(name)) {
					//									raw = raw.replaceAll("(?i)" + name, nick);
					//									iterator.remove();
					//								}
					//							}
					//						}
					//					}

					//					Object serialized = ChatSerializer.getDeclaredMethod("a", String.class).invoke(null, raw);
					Object[] components = (Object[]) CraftChatMessageMethodResolver.resolve(new ResolverQuery("fromString", String.class)).invoke(null, replacedMessage);
					packet.setPacketValue("a", components[0]);
				} catch (Exception e) {
					getPlugin().getLogger().log(Level.SEVERE, "", e);
				}
			}
			if ("PacketPlayOutScoreboardObjective".equals(packet.getPacketName())) {
				final String b = (String) packet.getPacketValue("b");
				final String replacedB = NickNamerAPI.replaceNames(b, NickNamerAPI.getNickedPlayerNames(), new NameReplacer() {
					@Override
					public String replace(String original) {
						Player player = Bukkit.getPlayer(original);
						if (player != null) {
							ScoreboardReplacementEvent replacementEvent = new ScoreboardReplacementEvent(player, packet.getPlayer(), b, original, original);
							Bukkit.getPluginManager().callEvent(replacementEvent);
							if (replacementEvent.isCancelled()) { return original; }
							return replacementEvent.getReplacement();
						}
						return original;
					}
				}, true);
				packet.setPacketValue("b", replacedB);
			}
		}
	}

	@Override
	@PacketOptions(forcePlayer = true)
	public void onReceive(final ReceivedPacket packet) {
		if ("PacketPlayInChat".equals(packet.getPacketName())) {
			if (packet.hasPlayer()) {
				try {
					final String message = (String) PacketPlayInChatFieldResolver.resolve("message", "a").get(packet.getPacket());
					String replacedMessage = NickNamerAPI.replaceNames(message, NickNamerAPI.getNickedPlayerNames(), new NameReplacer() {
						@Override
						public String replace(String original) {
							Player player = Bukkit.getPlayer(original);
							if (player != null) {
								ChatInReplacementEvent replacementEvent = new ChatInReplacementEvent(player, packet.getPlayer(), message, original, original);
								Bukkit.getPluginManager().callEvent(replacementEvent);
								if (replacementEvent.isCancelled()) { return original; }
								return replacementEvent.getReplacement();
							}
							return original;
						}
					}, true);
					PacketPlayInChatFieldResolver.resolve("message", "a").set(packet.getPacket(), replacedMessage);
				} catch (Exception e) {
					getPlugin().getLogger().log(Level.SEVERE, "", e);
				}
			}
		}
	}

	private GameProfileWrapper disguiseProfile(final Player observer, final GameProfileWrapper profileWrapper) throws Exception {
		final UUID id = profileWrapper.getId();
		final String name = profileWrapper.getName();
		final Player toDisguise = Bukkit.getPlayer(id);

		if (toDisguise == null || !toDisguise.isOnline()) {
			return profileWrapper;//Player to disguise is offline
		}

		NickDisguiseEvent nickDisguiseEvent = new NickDisguiseEvent(toDisguise, observer, profileWrapper, name);
		SkinDisguiseEvent skinDisguiseEvent = new SkinDisguiseEvent(toDisguise, observer, profileWrapper, name);
		Bukkit.getPluginManager().callEvent(nickDisguiseEvent);
		Bukkit.getPluginManager().callEvent(skinDisguiseEvent);

		if (!nickDisguiseEvent.isDisguised() && !skinDisguiseEvent.isDisguised()) {
			return profileWrapper;// Both event didn't disguise anything: return the unmodified profile
		}

		String nick = nickDisguiseEvent.isDisguised() ? nickDisguiseEvent.getNick() : name;
		String skin = skinDisguiseEvent.isDisguised() ? skinDisguiseEvent.getSkin() : name;

		//		if(name.equals(nick)&&name.equals(skin)){
		//			return profileWrapper;//Both nick & skin stayed the same
		//		}

		//		if (NickNamer.getNickManager().isNicked(id)) {
		//			nick = NickNamer.getNickManager().getNick(id);
		//		}
		//		if (NickNamer.getNickManager().hasSkin(id)) {
		//			skin = NickNamer.getNickManager().getSkin(id);
		//		}

		{//TODO: deprecate event, as it is no longer necessary
			//Call the update event
			NickNamerUpdateEvent event = new NickNamerUpdateEvent(toDisguise, observer, nick, skin);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				return profileWrapper;//Don't change anything if the event is cancelled
			}

			//Update the variables (if they aren't null)
			if (event.getNick() != null) {
				nick = event.getNick();
			}
			if (event.getSkin() != null) {
				skin = event.getSkin();
			}
		}

		GameProfileWrapper profileClone = new GameProfileWrapper(id, name);// Create a clone of the profile since the server's PlayerList will use the original profiles

		//		final Object profileClone = GameProfile.getConstructor(UUID.class, String.class).newInstance(id, name);// Create a clone of the profile since the server's PlayerList will use the original profiles
		//		Object propertyMapClone = SkinLoader.PropertyMap.newInstance();
		//		SkinLoader.PropertyMap.getSuperclass().getMethod("putAll", SkinLoader.Multimap).invoke(propertyMapClone, propertyMap);

		//		if (NickNamer.getNickManager().hasSkin(id)) {
		if (skinDisguiseEvent.isDisguised()) {
			//			JSONObject skinData = SkinLoader.getSkin(skin);
			//			if (skinData != null && skinData.containsKey("properties")) {
			//				skinData = (JSONObject) ((JSONArray) skinData.get("properties")).get(0);
			//
			//				if (skinData != null) {
			//					classPropertyMap.getMethod("clear").invoke(propertyMapClone);
			//					classPropertyMap.getSuperclass().getMethod("put", Object.class, Object.class).invoke(propertyMapClone, "textures", classProperty.getConstructor(String.class, String.class, String.class).newInstance("textures", skinData.get("value"), skinData.get("signature")));
			//				}
			//			} else {
			//				if (NickNamer.LOADING_SKIN) {
			//					// Clear the skin data to display the Steve/Alex skin
			//					classPropertyMap.getMethod("clear").invoke(propertyMapClone);
			//				}
			//			}

			GameProfileWrapper skinProfile = skin != null ? SkinLoader.getSkinProfile(skin) : null;
			if (skinProfile != null) {
				PropertyMapWrapper clonedSkinProperties = profileClone.getProperties();
				// Copy the skin properties to the cloned profile
				clonedSkinProperties.clear();
				clonedSkinProperties.putAll(skinProfile.getProperties());
			} else {
				//TODO: loading skin
			}

			//			Object skinProfile = SkinLoader.getSkinProfileHandle(skin);
			//			if (skinProfile != null) {
			//				Field propertiesField = SkinLoader.GameProfileFieldResolver.resolve("properties");
			//				propertiesField.set(profileClone, propertiesField.get(skinProfile));
			//			} else if (NickNamer.LOADING_SKIN) {
			//				// Clear the skin data to display the Steve/Alex skin
			//				SkinLoader.PropertyMap.getMethod("clear").invoke(propertyMapClone);
			//				propertyMapField.set(profileClone, propertyMapClone);
			//			}

		}

		if (nickDisguiseEvent.isDisguised()) {
			profileClone.setName(nick);
		}

		//		if (NickNamer.getNickManager().isNicked(id)) {
		//			nameField.set(profileClone, nick);
		//		}
		return profileClone;
	}
}
