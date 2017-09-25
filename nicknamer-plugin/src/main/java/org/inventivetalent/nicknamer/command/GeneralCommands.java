package org.inventivetalent.nicknamer.command;

import org.bukkit.command.CommandSender;
import org.inventivetalent.nicknamer.NickNamerPlugin;
import org.inventivetalent.pluginannotations.command.Command;
import org.inventivetalent.pluginannotations.command.Permission;

@SuppressWarnings({"unused", "WeakerAccess"})
public class GeneralCommands {

	private NickNamerPlugin plugin;

	public GeneralCommands(NickNamerPlugin plugin) {
		this.plugin = plugin;
	}

	@Command(name = "nickreload",
			aliases = {
					"nicknamerreload",
					"reloadnick"},
			description = "Reload the configuration",
			max = 0)
	@Permission("nicknamer.reload")
	public void nickreload(final CommandSender sender) {
		plugin.reloadConfig();
		sender.sendMessage("§aConfiguration reloaded.");
		sender.sendMessage("§6Note: §eYou need to restart the server if you changed the storage type");
	}
}
