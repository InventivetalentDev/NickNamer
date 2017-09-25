package org.inventivetalent.nicknamer;

import org.bukkit.entity.Player;
import org.inventivetalent.nicknamer.api.INickNamer;
import org.inventivetalent.nicknamer.api.NickManager;
import org.inventivetalent.nicknamer.api.NickNamerAPI;

@Deprecated
public class NickNamer implements INickNamer {

	@Deprecated
	public static NickManager getNickManager() {
		return NickNamerAPI.getNickManager();
	}

	@Deprecated
	@Override
	public NickManager getAPI() {
		return getNickManager();
	}

	@Deprecated
	public void sendPluginMessage(Player player, String action, String... values) {
		NickNamerPlugin.instance.sendPluginMessage(player, action, values);
	}

}
