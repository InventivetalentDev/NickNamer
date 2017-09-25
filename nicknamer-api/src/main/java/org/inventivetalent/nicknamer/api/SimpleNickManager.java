package org.inventivetalent.nicknamer.api;

import com.google.gson.JsonObject;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;
import org.inventivetalent.nicknamer.api.event.NickNamerSelfUpdateEvent;
import org.inventivetalent.nicknamer.api.event.refresh.PlayerRefreshEvent;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Basic NickManager implementation to refresh players - doesn't store any settings
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class SimpleNickManager implements NickManager {

	protected Plugin plugin;
	Class EnumDifficulty = SkinLoader.nmsClassResolver.resolveSilent("EnumDifficulty");
	Class WorldType = SkinLoader.nmsClassResolver.resolveSilent("WorldType");
	Class EnumGamemode = SkinLoader.nmsClassResolver.resolveSilent("WorldSettings$EnumGamemode", "EnumGamemode");

	public SimpleNickManager(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void refreshPlayer(@NonNull UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player == null) {
			return;
		}
		refreshPlayer(player);
	}

	@Override
	public void refreshPlayer(@NonNull final Player player) {
		if (!player.isOnline()) {
			return;
		}

		PlayerRefreshEvent refreshEvent = new PlayerRefreshEvent(player, true);
		Bukkit.getPluginManager().callEvent(refreshEvent);
		if (refreshEvent.isCancelled()) {
			return;
		}

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

	@SuppressWarnings("unchecked")
	protected void updateSelf(final Player player) {
		if (player == null || !player.isOnline()) {
			return;
		}
		Object profile = ClassBuilder.getGameProfile(player);

		NickNamerSelfUpdateEvent event = new NickNamerSelfUpdateEvent(player, isNicked(player.getUniqueId()) ? getNick(player.getUniqueId()) : player.getPlayerListName(), profile, player.getWorld().getDifficulty(), player.getGameMode());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}

		try {
			final Object removePlayer = ClassBuilder.buildPlayerInfoPacket(4, event.getGameProfile(), 0, event.getGameMode().ordinal(), event.getName());
			final Object addPlayer = ClassBuilder.buildPlayerInfoPacket(0, event.getGameProfile(), 0, event.getGameMode().ordinal(), event.getName());
			Object difficulty = EnumDifficulty.getDeclaredMethod("getById", int.class).invoke(null, event.getDifficulty().getValue());
			Object type = ((Object[]) WorldType.getDeclaredField("types").get(null))[0];
			Object gamemode = EnumGamemode.getDeclaredMethod("getById", int.class).invoke(null, event.getGameMode().getValue());
			final Object respawnPlayer = SkinLoader.nmsClassResolver.resolve("PacketPlayOutRespawn").getConstructor(int.class, EnumDifficulty, WorldType, EnumGamemode).newInstance(0, difficulty, type, gamemode);

			NickNamerAPI.packetListener.sendPacket(player, removePlayer);

			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					boolean flying = player.isFlying();
					Location location = player.getLocation();
					int level = player.getLevel();
					float xp = player.getExp();
					double maxHealth = player.getMaxHealth();
					double health = player.getHealth();

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
	public boolean isNicked(@NonNull UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isNickUsed(@NonNull String nick) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNick(@NonNull UUID id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNick(@NonNull UUID uuid, @NonNull String nick) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeNick(@NonNull UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@NonNull
	@Override
	public List<UUID> getPlayersWithNick(@NonNull String nick) {
		throw new UnsupportedOperationException();
	}

	@NonNull
	@Override
	public List<String> getUsedNicks() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSkin(@NonNull final UUID uuid, @NonNull final String skinOwner) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadCustomSkin(@NonNull String key, @NonNull Object gameProfile) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadCustomSkin(@NonNull String key, @NonNull GameProfileWrapper profileWrapper) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadCustomSkin(@NonNull String key, @NonNull JsonObject data) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void loadCustomSkin(String key, JSONObject data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCustomSkin(@NonNull UUID uuid, @NonNull String skin) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeSkin(@NonNull UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSkin(@NonNull UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasSkin(@NonNull UUID uuid) {
		throw new UnsupportedOperationException();
	}

	@NonNull
	@Override
	public List<UUID> getPlayersWithSkin(@NonNull String skin) {
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
