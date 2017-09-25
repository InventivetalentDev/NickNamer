package org.inventivetalent.nicknamer.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.inventivetalent.nicknamer.NickNamerPlugin;
import org.inventivetalent.pluginannotations.PluginAnnotations;
import org.inventivetalent.pluginannotations.command.CommandErrorHandler;
import org.inventivetalent.pluginannotations.command.exception.*;
import org.inventivetalent.pluginannotations.message.MessageFormatter;
import org.inventivetalent.pluginannotations.message.MessageLoader;

import java.util.logging.Level;

@SuppressWarnings({"unused", "WeakerAccess"})
public class NickNamerErrorHandler extends CommandErrorHandler {

	static MessageLoader MESSAGE_LOADER = PluginAnnotations.MESSAGE.newMessageLoader(NickNamerPlugin.instance, "config.yml", "messages.command.error", null);

	@Override
	public void handleCommandException(CommandException exception, CommandSender sender, Command command, String[] args) {
		sender.sendMessage(MESSAGE_LOADER.getMessage("unknown", "unknown"));
		NickNamerPlugin.instance.getLogger().log(Level.SEVERE, "Unknown exception while executing '/" + command.getName() + "' for " + sender.getName(), exception);
	}

	@Override
	public void handlePermissionException(final PermissionException exception, CommandSender sender, Command command, String[] args) {
		sender.sendMessage(MESSAGE_LOADER.getMessage("permission", "permission", new MessageFormatter() {
			@Override
			public String format(String key, String message) {
				return String.format(message, exception.getPermission());
			}
		}));
	}

	@Override
	public void handleIllegalSender(IllegalSenderException exception, CommandSender sender, Command command, String[] args) {
		sender.sendMessage(MESSAGE_LOADER.getMessage("illegalSender", "illegalSender"));
	}

	@Override
	public void handleUnhandled(UnhandledCommandException exception, CommandSender sender, Command command, String[] args) {
		sender.sendMessage(MESSAGE_LOADER.getMessage("unhandled", "unhandled"));
		NickNamerPlugin.instance.getLogger().log(Level.SEVERE, "Unhandled exception while executing '/" + command.getName() + "' for " + sender.getName(), exception);
	}

	@Override
	public void handleLength(InvalidLengthException exception, CommandSender sender, final Command command, String[] args) {
		MessageFormatter usageFormatter = new MessageFormatter() {
			@Override
			public String format(String key, String message) {
				return String.format(message, command.getUsage());
			}
		};
		if (exception.getGivenLength() > exception.getExpectedLength()) {
			sender.sendMessage(MESSAGE_LOADER.getMessage("length.long", "length.long", usageFormatter));
		} else {
			sender.sendMessage(MESSAGE_LOADER.getMessage("length.short", "length.short", usageFormatter));
		}
	}

	@Override
	public void handleArgumentParse(final ArgumentParseException exception, CommandSender sender, Command command, String[] args) {
		sender.sendMessage(MESSAGE_LOADER.getMessage("parse", "parse", new MessageFormatter() {
			@Override
			public String format(String key, String message) {
				return String.format(message, exception.getArgument(), exception.getParameterType().getSimpleName());
			}
		}));
	}

}
