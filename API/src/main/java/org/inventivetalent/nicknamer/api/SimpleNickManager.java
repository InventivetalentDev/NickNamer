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
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;
import org.inventivetalent.nicknamer.api.event.NickNamerSelfUpdateEvent;
import org.inventivetalent.nicknamer.api.event.refresh.PlayerRefreshEvent;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.json.simple.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Basic NickManager implementation to refresh players - doesn't store any settings
 */
public class SimpleNickManager implements NickManager {

	Class DimensionManager = SkinLoader.nmsClassResolver.resolveSilent("DimensionManager");
	Class EnumDifficulty   = SkinLoader.nmsClassResolver.resolveSilent("EnumDifficulty");
	Class WorldType        = SkinLoader.nmsClassResolver.resolveSilent("WorldType");
	Class EnumGamemode     = SkinLoader.nmsClassResolver.resolveSilent("WorldSettings$EnumGamemode", "EnumGamemode");
	Class WorldData        = SkinLoader.nmsClassResolver.resolveSilent("WorldData");

	protected Plugin plugin;

	public SimpleNickManager(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void refreshPlayer(@Nonnull UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player == null) { return; }
		refreshPlayer(player);
	}

	@Override
	public void refreshPlayer(@Nonnull final Player player) {
		if (!player.isOnline()) { return; }

		boolean async = !plugin.getServer().isPrimaryThread();
		PlayerRefreshEvent refreshEvent = new PlayerRefreshEvent(player, true, async);
		Bukkit.getPluginManager().callEvent(refreshEvent);
		if (refreshEvent.isCancelled()) { return; }

		if (refreshEvent.isSelf()) {
			updateSelf(player);
		}

		Bukkit.getScheduler().runTask(plugin, new Runnable() {

			@Override
			public void run() {
				List<Player> canSee = new ArrayList<>();
				for (Player player1 : Bukkit.getOnlinePlayers()) {
					if (player1.canSee(player)) {
						canSee.add(player1);
						player1.hidePlayer(player);
					}
				}
				for (Player player1 : canSee) {
					player1.showPlayer(player);
				}
			}
		});
	}

	protected void updateSelf(final Player player) {
		if (player == null || !player.isOnline()) { return; }
		Object profile = ClassBuilder.getGameProfile(player);

		NickNamerSelfUpdateEvent event = new NickNamerSelfUpdateEvent(player, isNicked(player.getUniqueId()) ? getNick(player.getUniqueId()) : player.getPlayerListName(), profile, player.getWorld().getDifficulty(), player.getGameMode());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) { return; }

		try {
			final Object removePlayer = ClassBuilder.buildPlayerInfoPacket(4, event.getGameProfile(), 0, event.getGameMode().ordinal(), event.getName());
			final Object addPlayer = ClassBuilder.buildPlayerInfoPacket(0, event.getGameProfile(), 0, event.getGameMode().ordinal(), event.getName());

			NickNamerAPI.packetListener.sendPacket(player, removePlayer);

			//TODO: might want to send two respawn packets to get rid of the chunk unloading weirdness
			//  (https://wiki.vg/Protocol#Respawn)
			//   -> tried that, turns out the client needs a chunk update right after the respawn packet, so we'd have to add that aswell

			final Object respawnPlayer;
			if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_13_R1)) {
				Object dimensionManager;
				switch (player.getWorld().getEnvironment()) {
					case NETHER:
						dimensionManager = DimensionManager.getDeclaredField("NETHER").get(null);
						break;
					case THE_END:
						dimensionManager = DimensionManager.getDeclaredField("THE_END").get(null);
						break;
					case NORMAL:
					default:
						dimensionManager = DimensionManager.getDeclaredField("OVERWORLD").get(null);
						break;
				}

				Object difficulty = EnumDifficulty.getEnumConstants()[event.getDifficulty().ordinal()];

				Object type = ((Object[]) WorldType.getDeclaredField("types").get(null))[0];
				Object gamemode = EnumGamemode.getDeclaredMethod("getById", int.class).invoke(null, event.getGameMode().getValue());

				if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_15_R1)) {// https://wiki.vg/Protocol_History#19w36a - new hashed seed field in respawn packet
					long seedHash = (long) WorldData.getDeclaredMethod("c", Long.TYPE).invoke(null, player.getWorld().getSeed());

					respawnPlayer = SkinLoader.nmsClassResolver.resolve("PacketPlayOutRespawn")
							.getConstructor(DimensionManager, Long.TYPE, WorldType, EnumGamemode)
							.newInstance(dimensionManager, seedHash, type, gamemode);
				} else if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_14_R1)) {
					respawnPlayer = SkinLoader.nmsClassResolver.resolve("PacketPlayOutRespawn")
							.getConstructor(DimensionManager, WorldType, EnumGamemode)
							.newInstance(dimensionManager, type, gamemode);
				} else {
					respawnPlayer = SkinLoader.nmsClassResolver.resolve("PacketPlayOutRespawn")
							.getConstructor(DimensionManager, EnumDifficulty, WorldType, EnumGamemode)
							.newInstance(dimensionManager, difficulty, type, gamemode);
				}
			} else {
				int dimension = player.getWorld().getEnvironment().getId();
				Object difficulty = EnumDifficulty.getDeclaredMethod("getById", int.class).invoke(null, event.getDifficulty().getValue());
				Object type = ((Object[]) WorldType.getDeclaredField("types").get(null))[0];
				Object gamemode = EnumGamemode.getDeclaredMethod("getById", int.class).invoke(null, event.getGameMode().getValue());

				respawnPlayer = SkinLoader.nmsClassResolver.resolve("PacketPlayOutRespawn")
						.getConstructor(int.class, EnumDifficulty, WorldType, EnumGamemode)
						.newInstance(dimension, difficulty, type, gamemode);
			}

			final boolean flying = player.isFlying();
			final Location location = player.getLocation();
			final int level = player.getLevel();
			final float xp = player.getExp();
			final double maxHealth = player.getMaxHealth();
			final double health = player.getHealth();

			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					NickNamerAPI.packetListener.sendPacket(player, respawnPlayer);

					player.setFlying(flying);
					player.teleport(location);
					player.updateInventory();
					player.setLevel(level);
					player.setExp(xp);
					player.setMaxHealth(maxHealth);
					player.setHealth(health);

					NickNamerAPI.packetListener.sendPacket(player, addPlayer);
				}
			}, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	@Deprecated
	public void updatePlayer(Player player, boolean updateName, boolean updateSkin, boolean updateSelf) {
		refreshPlayer(player.getUniqueId());
	}

	@Override
	@Deprecated
	public void updatePlayer(UUID uuid, boolean updateName, boolean updateSkin, boolean updateSelf) {
		refreshPlayer(uuid);
	}

	@Override
	public boolean isNicked(@Nonnull UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isNickUsed(@Nonnull String nick) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNick(@Nonnull UUID id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNick(@Nonnull UUID uuid, @Nonnull String nick) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeNick(@Nonnull UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public List<UUID> getPlayersWithNick(@Nonnull String nick) {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public List<String> getUsedNicks() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSkin(@Nonnull UUID uuid, @Nonnull String skin, @Nullable Callback callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSkin(@Nonnull final UUID uuid, @Nonnull final String skinOwner) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadCustomSkin(@Nonnull String key, @Nonnull Object gameProfile) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadCustomSkin(@Nonnull String key, @Nonnull GameProfileWrapper profileWrapper) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadCustomSkin(@Nonnull String key, @Nonnull JsonObject data) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void loadCustomSkin(String key, JSONObject data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCustomSkin(@Nonnull UUID uuid, @Nonnull String skin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeSkin(@Nonnull UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSkin(@Nonnull UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasSkin(@Nonnull UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public List<UUID> getPlayersWithSkin(@Nonnull String skin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<String> getUsedSkins() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSimple() {
		return true;
	}
}
