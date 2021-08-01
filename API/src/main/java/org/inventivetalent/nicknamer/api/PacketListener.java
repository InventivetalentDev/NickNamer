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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.nicknamer.api.event.NickNamerUpdateEvent;
import org.inventivetalent.nicknamer.api.event.disguise.NickDisguiseEvent;
import org.inventivetalent.nicknamer.api.event.disguise.SkinDisguiseEvent;
import org.inventivetalent.nicknamer.api.event.replace.*;
import org.inventivetalent.packetlistener.handler.PacketHandler;
import org.inventivetalent.packetlistener.handler.PacketOptions;
import org.inventivetalent.packetlistener.handler.ReceivedPacket;
import org.inventivetalent.packetlistener.handler.SentPacket;
import org.inventivetalent.reflection.accessor.FieldAccessor;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.minecraft.MinecraftVersion;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.logging.Level;

public class PacketListener extends PacketHandler {

    static NMSClassResolver nmsClassResolver = new NMSClassResolver();
    static OBCClassResolver obcClassResolver = new OBCClassResolver();

    static Class<?> PlayerInfoData = nmsClassResolver.resolveSilent("PlayerInfoData", "PacketPlayOutPlayerInfo$PlayerInfoData", "network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData");// 1.8+ only
    static Class<?> IChatBaseComponent = nmsClassResolver.resolveSilent("IChatBaseComponent", "network.chat.IChatBaseComponent");
    static Class<?> PacketPlayInChat = nmsClassResolver.resolveSilent("PacketPlayInChat", "network.protocol.game.PacketPlayInChat");
    static Class<?> ChatSerializer = nmsClassResolver.resolveSilent("ChatSerializer", "IChatBaseComponent$ChatSerializer", "network.chat.IChatBaseComponent$ChatSerializer");

    static FieldResolver PlayerInfoDataFieldResolver = PlayerInfoData != null ? new FieldResolver(PlayerInfoData) : null;// 1.8+ only
    static FieldResolver PacketPlayInChatFieldResolver = new FieldResolver(PacketPlayInChat);
    static MethodResolver ChatSerializerMethodResolver = new MethodResolver(ChatSerializer);

    public PacketListener(Plugin plugin) {
        super(plugin);
    }

    @Override
    @PacketOptions(forcePlayer = true)
    public void onSend(final SentPacket packet) {
        if (packet.hasPlayer()) {
            if ("PacketPlayOutPlayerInfo".equals(packet.getPacketName())) {
                try {
                    Object profileHandle = packet.getPacketValue("b");
                    List list = (List) profileHandle;
                    for (ListIterator<Object> iterator = list.listIterator(); iterator.hasNext(); ) {
                        Object originalData = iterator.next();
                        if (originalData == null) continue;

                        FieldAccessor latencyField = PlayerInfoDataFieldResolver.resolveIndexAccessor(0);
                        FieldAccessor gameModeField = PlayerInfoDataFieldResolver.resolveIndexAccessor(1);
                        FieldAccessor profileField = PlayerInfoDataFieldResolver.resolveIndexAccessor(2);
                        FieldAccessor nameField = PlayerInfoDataFieldResolver.resolveIndexAccessor(3);

                        GameProfile originalProfile = profileField.get(originalData);
                        GameProfile disguisedProfile = disguiseProfile(packet.getPlayer(), originalProfile);
                        if (disguisedProfile != null) {
                            Object newData = ClassBuilder.buildPlayerInfoData(disguisedProfile, (int) latencyField.get(originalData), gameModeField.get(originalData), nameField.get(originalData));
                            iterator.set(newData);
                        }
                    }
                } catch (Exception e) {
                    getPlugin().getLogger().log(Level.SEVERE, "Failed to disguise profile", e);
                }
            }

            //// Name replacement
            if (!NickNamerAPI.getNickManager().isSimple()) {
                if ("PacketPlayOutChat".equalsIgnoreCase(packet.getPacketName())) {
                    if (ChatOutReplacementEvent.getHandlerList().getRegisteredListeners().length > 0) {
                        Object a = packet.getPacketValue("a");
                        try {
                            final String message = serializeChat(a);
                            final String replacedMessage = NickNamerAPI.replaceNames(message, NickNamerAPI.getNickedPlayerNames(), new NameReplacer() {
                                @Override
                                public String replace(String original) {
                                    Player player = Bukkit.getPlayer(original);
                                    if (player != null) {
                                        boolean async = !getPlugin().getServer().isPrimaryThread();
                                        ChatOutReplacementEvent replacementEvent = new ChatOutReplacementEvent(player, packet.getPlayer(), message, original, original, async);
                                        Bukkit.getPluginManager().callEvent(replacementEvent);
                                        if (replacementEvent.isCancelled()) { return original; }
                                        return replacementEvent.getReplacement();
                                    }
                                    return original;
                                }
                            }, true);
                            packet.setPacketValue("a", deserializeChat(replacedMessage));
                        } catch (Exception e) {
                            getPlugin().getLogger().log(Level.SEVERE, "", e);
                        }
                    }
                    if (ChatOutReverseReplacementEvent.getHandlerList().getRegisteredListeners().length > 0) {
                        Object a = packet.getPacketValue("a");
                        try {
                            final String message = serializeChat(a);
                            final String replacedMessage = NickNamerAPI.replaceNames(message, NickNamerAPI.getNickManager().getUsedNicks(), new NameReplacer() {
                                @Override
                                public String replace(String original) {
                                    Collection<UUID> playersWithNick = NickNamerAPI.getNickManager().getPlayersWithNick(original);
                                    if (playersWithNick.size() > 0) {
                                        Player player = Bukkit.getPlayer(playersWithNick.iterator().next());
                                        if (player != null) {
                                            boolean async = !getPlugin().getServer().isPrimaryThread();
                                            ChatOutReverseReplacementEvent replacementEvent = new ChatOutReverseReplacementEvent(player, packet.getPlayer(), message, original, original, async);
                                            Bukkit.getPluginManager().callEvent(replacementEvent);
                                            if (replacementEvent.isCancelled()) { return original; }
                                            return replacementEvent.getReplacement();
                                        }
                                    }
                                    return original;
                                }
                            }, true);
                            packet.setPacketValue("a", deserializeChat(replacedMessage));
                        } catch (Exception e) {
                            getPlugin().getLogger().log(Level.SEVERE, "", e);
                        }
                    }
                }
                if ("PacketPlayOutScoreboardObjective".equals(packet.getPacketName())) {
                    if (ScoreboardReplacementEvent.getHandlerList().getRegisteredListeners().length > 0) {
                        boolean isChatComponent = MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_13_R1);// It's a chat component instead of a string since 1.13
                        final String b = isChatComponent ? serializeChat(packet.getPacketValue("b")) : (String) packet.getPacketValue("b");
                        final String replacedB = NickNamerAPI.replaceNames(b, NickNamerAPI.getNickedPlayerNames(), new NameReplacer() {
                            @Override
                            public String replace(String original) {
                                Player player = Bukkit.getPlayer(original);
                                if (player != null) {
                                    boolean async = !getPlugin().getServer().isPrimaryThread();
                                    ScoreboardReplacementEvent replacementEvent = new ScoreboardReplacementEvent(player, packet.getPlayer(), b, original, original, async);
                                    Bukkit.getPluginManager().callEvent(replacementEvent);
                                    if (replacementEvent.isCancelled()) { return original; }
                                    return replacementEvent.getReplacement();
                                }
                                return original;
                            }
                        }, true);
                        packet.setPacketValue("b", isChatComponent ? deserializeChat(replacedB) : replacedB);
                    }
                }
                if ("PacketPlayOutScoreboardScore".equals(packet.getPacketName())) {
                    if (ScoreboardScoreReplacementEvent.getHandlerList().getRegisteredListeners().length > 0) {
                        final String a = (String) packet.getPacketValue("a");
                        final String replacedA = NickNamerAPI.replaceNames(a, NickNamerAPI.getNickedPlayerNames(), new NameReplacer() {
                            @Override
                            public String replace(String original) {
                                Player player = Bukkit.getPlayer(original);
                                if (player != null) {
                                    boolean async = !getPlugin().getServer().isPrimaryThread();
                                    ScoreboardScoreReplacementEvent replacementEvent = new ScoreboardScoreReplacementEvent(player, packet.getPlayer(), a, original, original, async);
                                    Bukkit.getPluginManager().callEvent(replacementEvent);
                                    if (replacementEvent.isCancelled()) { return original; }
                                    return replacementEvent.getReplacement();
                                }
                                return original;
                            }
                        }, true);
                        packet.setPacketValue("a", replacedA);
                    }
                }
                if ("PacketPlayOutScoreboardTeam".equals(packet.getPacketName())) {
                    if (ScoreboardTeamReplacementEvent.getHandlerList().getRegisteredListeners().length > 0) {
                        final List<String> h = (List<String>)
                                (MinecraftVersion.VERSION.olderThan(Minecraft.Version.v1_8_R1) ? packet.getPacketValue("e") :
                                        MinecraftVersion.VERSION.olderThan(Minecraft.Version.v1_9_R1) ? packet.getPacketValue("g") :
                                                packet.getPacketValue("h"));
                        for (ListIterator<String> iterator = h.listIterator(); iterator.hasNext(); ) {
                            final String entry = iterator.next();
                            final String replacedEntry = NickNamerAPI.replaceNames(entry, NickNamerAPI.getNickedPlayerNames(), new NameReplacer() {
                                @Override
                                public String replace(String original) {
                                    Player player = Bukkit.getPlayer(original);
                                    if (player != null) {
                                        boolean async = !getPlugin().getServer().isPrimaryThread();
                                        ScoreboardTeamReplacementEvent replacementEvent = new ScoreboardTeamReplacementEvent(player, packet.getPlayer(), entry, original, original, async);
                                        Bukkit.getPluginManager().callEvent(replacementEvent);
                                        if (replacementEvent.isCancelled()) { return original; }
                                        return replacementEvent.getReplacement();
                                    }
                                    return original;
                                }
                            }, true);
                            iterator.set(replacedEntry);
                        }
                    }
                }
            }
        }
    }

    @Override
    @PacketOptions(forcePlayer = true)
    public void onReceive(final ReceivedPacket packet) {
        if (!NickNamerAPI.getNickManager().isSimple()) {
            if (packet.hasPlayer()) {
                if ("PacketPlayInChat".equals(packet.getPacketName())) {
                    if (ChatInReplacementEvent.getHandlerList().getRegisteredListeners().length > 0) {
                        try {
                            final String message = (String) PacketPlayInChatFieldResolver.resolve("message", "a").get(packet.getPacket());
                            String replacedMessage = NickNamerAPI.replaceNames(message, NickNamerAPI.getNickedPlayerNames(), new NameReplacer() {
                                @Override
                                public String replace(String original) {
                                    Player player = Bukkit.getPlayer(original);
                                    if (player != null) {
                                        boolean async = !getPlugin().getServer().isPrimaryThread();
                                        ChatInReplacementEvent replacementEvent = new ChatInReplacementEvent(player, packet.getPlayer(), message, original, original, async);
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
                    if (ChatInReverseReplacementEvent.getHandlerList().getRegisteredListeners().length > 0) {
                        try {
                            final String message = (String) PacketPlayInChatFieldResolver.resolve("message", "a").get(packet.getPacket());
                            String replacedMessage = NickNamerAPI.replaceNames(message, NickNamerAPI.getNickManager().getUsedNicks(), new NameReplacer() {
                                @Override
                                public String replace(String original) {
                                    Collection<UUID> playersWithNick = NickNamerAPI.getNickManager().getPlayersWithNick(original);
                                    if (playersWithNick.size() > 0) {
                                        Player player = Bukkit.getPlayer(playersWithNick.iterator().next());
                                        if (player != null) {
                                            boolean async = !getPlugin().getServer().isPrimaryThread();
                                            ChatInReverseReplacementEvent replacementEvent = new ChatInReverseReplacementEvent(player, packet.getPlayer(), message, original, original, async);
                                            Bukkit.getPluginManager().callEvent(replacementEvent);
                                            if (replacementEvent.isCancelled()) { return original; }
                                            return replacementEvent.getReplacement();
                                        }
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
        }
    }

    @Nullable
    private GameProfile disguiseProfile(final Player observer, final GameProfile profile) {
        final UUID id = profile.getId();
        final String name = profile.getName();
        final OfflinePlayer toDisguise = Bukkit.getOfflinePlayer(id);

        if (toDisguise == null /*|| !toDisguise.isOnline()*/) {
            return null;//Player to disguise doesn't exist
        }

        boolean async = !getPlugin().getServer().isPrimaryThread();

        NickDisguiseEvent nickDisguiseEvent = new NickDisguiseEvent(toDisguise, observer, profile, name, async);
        SkinDisguiseEvent skinDisguiseEvent = new SkinDisguiseEvent(toDisguise, observer, profile, name, async);
        Bukkit.getPluginManager().callEvent(nickDisguiseEvent);
        Bukkit.getPluginManager().callEvent(skinDisguiseEvent);

        if (!nickDisguiseEvent.isDisguised() && !skinDisguiseEvent.isDisguised()) {
            return null;// Both events didn't disguise anything: return the unmodified profile
        }

        String nick = nickDisguiseEvent.isDisguised() ? nickDisguiseEvent.getNick() : name;
        String skin = skinDisguiseEvent.isDisguised() ? skinDisguiseEvent.getSkin() : name;

        if (nick == null) { nick = name; }

        {//TODO: deprecate event, as it is no longer necessary
            //Call the update event
            NickNamerUpdateEvent event = new NickNamerUpdateEvent(toDisguise, observer, nick, skin);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return profile;//Don't change anything if the event is cancelled
            }

            //Update the variables (if they aren't null)
            if (event.getNick() != null) {
                nick = event.getNick();
            }
            if (event.getSkin() != null) {
                skin = event.getSkin();
            }
        }

        GameProfile profileClone = new GameProfile(id, nick);// Create a clone of the profile since the server's PlayerList will use the original profiles

        {
            GameProfile skinProfile = skin != null ? SkinLoader.getSkinProfile(skin) : null;
            if (skinProfile != null) {
                PropertyMap clonedSkinProperties = profileClone.getProperties();
                // Copy the skin properties to the cloned profile
                clonedSkinProperties.clear();
                clonedSkinProperties.putAll(skinProfile.getProperties());
            } else {
                //TODO: loading skin
            }
        }

        return profileClone;
    }

    String serializeChat(Object chatComponent) {
        if (chatComponent == null) { return null; }
        try {
            return (String) ChatSerializerMethodResolver.resolve(new ResolverQuery("a", IChatBaseComponent), new ResolverQuery("serialize", IChatBaseComponent)).invoke(null, chatComponent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Object deserializeChat(String serialized) {
        if (serialized == null) { return null; }
        try {
            return ChatSerializerMethodResolver.resolve(new ResolverQuery("a", String.class), new ResolverQuery("deserialize", String.class)).invoke(null, serialized);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
