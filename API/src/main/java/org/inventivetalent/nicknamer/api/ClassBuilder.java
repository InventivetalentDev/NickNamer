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
import org.bukkit.entity.Player;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.minecraft.MinecraftVersion;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ClassBuilder {

    @SuppressWarnings({
            "rawtypes",
            "unchecked"})
    public static Object buildPlayerInfoPacket(int action, GameProfile profile, int ping, int gamemodeOrdinal, String name) {
        try {
            Object packet = PacketPlayOutPlayerInfo.getConstructor(EnumPlayerInfoAction, Class.forName("[L" + EntityPlayer.getName() + ";"))
                    .newInstance(EnumPlayerInfoAction.getEnumConstants()[action], Array.newInstance(EntityPlayer, 0));

            List list = (List) PacketPlayOutPlayerInfoFieldResolver.resolve("b").get(packet);

            Object data = buildPlayerInfoData(profile, ping, gamemodeOrdinal, name);
            list.add(data);

            return packet;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object buildPlayerInfoData(GameProfile gameProfile, int latency, int gameModeOrdinal, String displayName) {
        return buildPlayerInfoData(gameProfile, latency, EnumGamemode.getEnumConstants()[gameModeOrdinal], buildChatComponent(displayName));
    }

    public static Object buildPlayerInfoData(GameProfile gameProfile, int latency, Object gameMode, Object displayName) {
        try {
            return PlayerInfoData
                    .getConstructor(GameProfile.class, int.class, EnumGamemode, IChatBaseComponent)
                    .newInstance(gameProfile, latency, gameMode, displayName);
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

    public static Object buildScoreboardObjective(Object scoreboard, String name, Object criterion, Object displayName, Object renderType) {
        try {
            return ScoreboardObjective
                    .getConstructor(Scoreboard, String.class, IScoreboardCriteria, IChatBaseComponent, EnumScoreboardHealthDisplay)
                    .newInstance(scoreboard, name, criterion, displayName, renderType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object buildPacketPlayOutScoreboard(Object objective, int mode) {
        try {
            return PacketPlayOutScoreboardObjective
                    .getConstructor(ScoreboardObjective, int.class)
                    .newInstance(objective, mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object buildPacketPlayOutScoreboardScore(Object action, String objectiveName, String playerName, int score) {
        try {
            return PacketPlayOutScoreboardScore
                    .getConstructor(ScoreboardServer$Action, String.class, String.class, int.class)
                    .newInstance(action, objectiveName, playerName, score);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object buildPacketPlayOutScoreboardTeam(String teamName, int packetType, Optional teamOptional, Collection<String> playerNames) {
        try {
            Constructor constructor = PacketPlayOutScoreboardTeam
                    .getDeclaredConstructor(String.class, int.class, Optional.class, Collection.class);
            constructor.setAccessible(true);
            return constructor
                    .newInstance(teamName, packetType, teamOptional, playerNames);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object buildPacketPlayInChat(String message) {
        try {
            return PacketPlayInChat
                    .getConstructor(String.class)
                    .newInstance(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static GameProfile getGameProfile(Player player) {
        try {
            return (GameProfile) EntityHumanMethodResolver.resolve("getProfile").invoke(Minecraft.getHandle(player));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getNMUtilClass(String name) throws ClassNotFoundException {
        if (MinecraftVersion.VERSION.olderThan(Minecraft.Version.v1_8_R1)) { return Class.forName("net.minecraft.util." + name); } else {
            return Class.forName(name);
        }
    }

    static NMSClassResolver nmsClassResolver = new NMSClassResolver();
    static OBCClassResolver obcClassResolver = new OBCClassResolver();

    static Class<?> PacketPlayOutPlayerInfo = nmsClassResolver.resolveSilent("PacketPlayOutPlayerInfo", "network.protocol.game.PacketPlayOutPlayerInfo");
    static Class<?> PlayerInfoData = nmsClassResolver.resolveSilent("PlayerInfoData", "PacketPlayOutPlayerInfo$PlayerInfoData", "network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData");
    static Class<?> EnumPlayerInfoAction = nmsClassResolver.resolveSilent("EnumPlayerInfoAction", "PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
    static Class<?> EnumGamemode = nmsClassResolver.resolveSilent("EnumGamemode", "WorldSettings$EnumGamemode", "world.level.EnumGamemode");
    static Class<?> EntityHuman = nmsClassResolver.resolveSilent("EntityHuman", "world.entity.player.EntityHuman");
    static Class<?> EntityPlayer = nmsClassResolver.resolveSilent("EntityPlayer", "server.level.EntityPlayer");
    static Class<?> IChatBaseComponent = nmsClassResolver.resolveSilent("IChatBaseComponent", "network.chat.IChatBaseComponent");
    static Class<?> ScoreboardObjective = nmsClassResolver.resolveSilent("world.scores.ScoreboardObjective");
    static Class<?> Scoreboard = nmsClassResolver.resolveSilent("world.scores.Scoreboard");
    static Class<?> IScoreboardCriteria = nmsClassResolver.resolveSilent("world.scores.criteria.IScoreboardCriteria");
    static Class<?> EnumScoreboardHealthDisplay = nmsClassResolver.resolveSilent("world.scores.criteria.IScoreboardCriteria$EnumScoreboardHealthDisplay");
    static Class<?> PacketPlayOutScoreboardObjective = nmsClassResolver.resolveSilent("network.protocol.game.PacketPlayOutScoreboardObjective");
    static Class<?> PacketPlayOutScoreboardScore = nmsClassResolver.resolveSilent("network.protocol.game.PacketPlayOutScoreboardScore");
    static Class<?> ScoreboardServer = nmsClassResolver.resolveSilent("server.ScoreboardServer");
    static Class<?> ScoreboardServer$Action = nmsClassResolver.resolveSilent("server.ScoreboardServer$Action");
    static Class<?> PacketPlayOutScoreboardTeam = nmsClassResolver.resolveSilent("network.protocol.game.PacketPlayOutScoreboardTeam");
    static Class<?> PacketPlayInChat = nmsClassResolver.resolveSilent("network.protocol.game.PacketPlayInChat");

    static Class<?> CraftChatMessage = obcClassResolver.resolveSilent("util.CraftChatMessage");

    static MethodResolver EntityHumanMethodResolver = new MethodResolver(EntityHuman);
    static MethodResolver CraftChatMessageMethodResolver = new MethodResolver(CraftChatMessage);

    static FieldResolver PacketPlayOutPlayerInfoFieldResolver = new FieldResolver(PacketPlayOutPlayerInfo);

}
