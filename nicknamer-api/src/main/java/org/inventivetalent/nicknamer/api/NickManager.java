package org.inventivetalent.nicknamer.api;

import com.google.gson.JsonObject;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.UUID;

/**
 * Note: If you only use the API artifact without the plugin installed, you will only be able to use the {@link #refreshPlayer(UUID)} methods.
 * <p>
 * It's recommended to use the {@link org.inventivetalent.nicknamer.api.event.disguise.ProfileDisguiseEvent}s, but you can also use these methods if the plugin is installed
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public interface NickManager {

	/**
	 * @param uuid {@link UUID} of the player
	 * @return <code>true</code> if the player has a nickname
	 */
	boolean isNicked(@NonNull UUID uuid);

	/**
	 * @param nick nickname
	 * @return <code>true</code> if the name is used by a player
	 */
	boolean isNickUsed(@NonNull String nick);

	/**
	 * @param uuid {@link UUID} of the player
	 * @return nickname of the player
	 * @see #isNicked(UUID)
	 */
	String getNick(@NonNull UUID uuid);

	/**
	 * @param uuid {@link UUID} of the player
	 * @param nick nickname
	 */
	void setNick(@NonNull UUID uuid, @NonNull String nick);

	/**
	 * @param uuid {@link UUID} of the player
	 */
	void removeNick(@NonNull UUID uuid);

	/**
	 * @param nick nickname
	 * @return Collection of players with the nickname
	 */
	@NonNull
	Collection<UUID> getPlayersWithNick(@NonNull String nick);

	/**
	 * @return Collection of used nicks
	 */
	@NonNull
	Collection<String> getUsedNicks();

	////////////////////////

	/**
	 * @param uuid {@link UUID} of the player
	 * @param skin skin owner
	 */
	void setSkin(@NonNull UUID uuid, @NonNull String skin);

	/**
	 * @param key         unique identifier for the custom skin
	 * @param gameProfile GameProfile
	 * @see #loadCustomSkin(String, GameProfileWrapper)
	 */
	void loadCustomSkin(@NonNull String key, @NonNull Object gameProfile);

	/**
	 * @param key            unique identifier for the custom skin
	 * @param profileWrapper GameProfile
	 */
	void loadCustomSkin(@NonNull String key, @NonNull GameProfileWrapper profileWrapper);

	/**
	 * @param key  unique identifier for the custom skin
	 * @param data {@link JsonObject} data
	 * @see #loadCustomSkin(String, GameProfileWrapper)
	 */
	void loadCustomSkin(@NonNull String key, @NonNull JsonObject data);

	/**
	 * @param key  unique identifier for the custom skin
	 * @param data {@link JSONObject} data
	 * @see #loadCustomSkin(String, GameProfileWrapper)
	 */
	@Deprecated
	void loadCustomSkin(String key, JSONObject data);

	/**
	 * Set a previously loaded custom skin
	 *
	 * @param id  {@link UUID} of the player
	 * @param key unique identifier of the custom skin
	 * @see #loadCustomSkin(String, JSONObject)
	 * @see #loadCustomSkin(String, Object)
	 */
	void setCustomSkin(@NonNull UUID id, @NonNull String key);

	/**
	 * @param uuid {@link UUID} of the player
	 */
	void removeSkin(@NonNull UUID uuid);

	/**
	 * @param uuid {@link UUID} of the player
	 * @return the used skin's owner
	 */
	String getSkin(@NonNull UUID uuid);

	/**
	 * @param uuid {@link UUID} of the player
	 * @return <code>true</code> if the player has a skin
	 */
	boolean hasSkin(@NonNull UUID uuid);

	/**
	 * @param skin skin owner
	 * @return Collection of players with the skin
	 */
	@NonNull
	Collection<UUID> getPlayersWithSkin(@NonNull String skin);

	@NonNull
	Collection<String> getUsedSkins();

	/**
	 * Updates a player
	 *
	 * @param player     {@link Player}
	 * @param updateName whether to update the name
	 * @param updateSkin whether to update the skin
	 * @param updateSelf whether to send the update to the player themselves
	 */
	@Deprecated
	void updatePlayer(Player player, boolean updateName, boolean updateSkin, boolean updateSelf);

	/**
	 * Updates a player
	 *
	 * @param uuid       {@link UUID} of the player
	 * @param updateName whether to update the name
	 * @param updateSkin whether to update the skin
	 * @param updateSelf whether to send the update to the player themselves
	 */
	@Deprecated
	void updatePlayer(UUID uuid, boolean updateName, boolean updateSkin, boolean updateSelf);

	/**
	 * Refresh a player, calls the {@link org.inventivetalent.nicknamer.api.event.refresh.PlayerRefreshEvent}
	 *
	 * @param uuid {@link UUID} of the player
	 */
	void refreshPlayer(@NonNull UUID uuid);

	/**
	 * Refresh a player, calls the {@link org.inventivetalent.nicknamer.api.event.refresh.PlayerRefreshEvent}
	 *
	 * @param player {@link Player}
	 */
	void refreshPlayer(@NonNull Player player);

	boolean isSimple();

}
