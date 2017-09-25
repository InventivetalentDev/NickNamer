package org.inventivetalent.nicknamer.api;

import org.bukkit.entity.Player;

@Deprecated
@SuppressWarnings({"unused", "WeakerAccess"})
public interface INickNamer {
	NickManager getAPI();

	void sendPluginMessage(Player player, String action, String... values);
}
