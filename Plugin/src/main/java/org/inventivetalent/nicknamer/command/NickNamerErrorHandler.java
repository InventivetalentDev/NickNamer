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
