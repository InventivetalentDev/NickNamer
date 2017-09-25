package org.inventivetalent.nicknamer.api.event.replace;

import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * Event called when a name in any outgoing message is replaced
 */
public class ChatOutReplacementEvent extends NameReplacementEvent {

	public ChatOutReplacementEvent(@NonNull Player disguised, @NonNull Player receiver, @NonNull String context, @NonNull String original, String replacement) {
		super(disguised, receiver, ReplaceType.CHAT_OUT, context, original, replacement);
	}

}
