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
import org.bukkit.entity.Player;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;
import org.json.simple.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

/**
 * Note: If you only use the API artifact without the plugin installed, you will only be able to use the {@link #refreshPlayer(UUID)} methods. <p> It's recommended to use the {@link org.inventivetalent.nicknamer.api.event.disguise.ProfileDisguiseEvent}s, but you can also use these methods if the plugin is installed
 */
public interface NickManager {

	/**
	 * @param uuid {@link UUID} of the player
	 * @return <code>true</code> if the player has a nickname
	 */
	boolean isNicked(@Nonnull UUID uuid);

	/**
	 * @param nick nickname
	 * @return <code>true</code> if the name is used by a player
	 */
	boolean isNickUsed(@Nonnull String nick);

	/**
	 * @param uuid {@link UUID} of the player
	 * @return nickname of the player
	 * @see #isNicked(UUID)
	 */
	@Nullable
	String getNick(@Nonnull UUID uuid);

	/**
	 * @param uuid {@link UUID} of the player
	 * @param nick nickname
	 */
	void setNick(@Nonnull UUID uuid, @Nonnull String nick);

	/**
	 * @param uuid {@link UUID} of the player
	 */
	void removeNick(@Nonnull UUID uuid);

	/**
	 * @param nick nickname
	 * @return Collection of players with the nickname
	 */
	@Nonnull
	Collection<UUID> getPlayersWithNick(@Nonnull String nick);

	/**
	 * @return Collection of used nicks
	 */
	@Nonnull
	Collection<String> getUsedNicks();

	////////////////////////

	/**
	 * @param uuid     {@link UUID} of the player
	 * @param skin     skin owner
	 * @param callback Callback to be called when the skin has been loaded and applied to the player
	 */
	void setSkin(@Nonnull UUID uuid, @Nonnull String skin, @Nullable Callback callback);

	/**
	 * @param uuid {@link UUID} of the player
	 * @param skin skin owner
	 */
	void setSkin(@Nonnull UUID uuid, @Nonnull String skin);

	/**
	 * @param key         unique identifier for the custom skin
	 * @param gameProfile GameProfile
	 * @see #loadCustomSkin(String, GameProfileWrapper)
	 */
	void loadCustomSkin(@Nonnull String key, @Nonnull Object gameProfile);

	/**
	 * @param key            unique identifier for the custom skin
	 * @param profileWrapper GameProfile
	 */
	void loadCustomSkin(@Nonnull String key, @Nonnull GameProfileWrapper profileWrapper);

	/**
	 * @param key  unique identifier for the custom skin
	 * @param data {@link JsonObject} data
	 * @see #loadCustomSkin(String, GameProfileWrapper)
	 */
	void loadCustomSkin(@Nonnull String key, @Nonnull JsonObject data);

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
	void setCustomSkin(@Nonnull UUID id, @Nonnull String key);

	/**
	 * @param uuid {@link UUID} of the player
	 */
	void removeSkin(@Nonnull UUID uuid);

	/**
	 * @param uuid {@link UUID} of the player
	 * @return the used skin's owner
	 */
	@Nullable
	String getSkin(@Nonnull UUID uuid);

	/**
	 * @param uuid {@link UUID} of the player
	 * @return <code>true</code> if the player has a skin
	 */
	boolean hasSkin(@Nonnull UUID uuid);

	/**
	 * @param skin skin owner
	 * @return Collection of players with the skin
	 */
	@Nonnull
	Collection<UUID> getPlayersWithSkin(@Nonnull String skin);

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
	void refreshPlayer(@Nonnull UUID uuid);

	/**
	 * Refresh a player, calls the {@link org.inventivetalent.nicknamer.api.event.refresh.PlayerRefreshEvent}
	 *
	 * @param player {@link Player}
	 */
	void refreshPlayer(@Nonnull Player player);

	boolean isSimple();

}
