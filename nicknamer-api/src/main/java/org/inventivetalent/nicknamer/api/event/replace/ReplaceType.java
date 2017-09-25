package org.inventivetalent.nicknamer.api.event.replace;

public enum ReplaceType {

	/**
	 * Outgoing chat messages sent to a player
	 * All messages sent to players are included
	 */
	CHAT_OUT,
	/**
	 * Incoming chat message sent by a player
	 * Commands are included as well (but have a <code>/</code> prefix)
	 */
	CHAT_IN,
	/**
	 * Chat message sent by a player
	 * Called by the default {@link org.bukkit.event.player.AsyncPlayerChatEvent}
	 */
	PLAYER_CHAT,
	/**
	 * Names included in a scoreboard
	 */
	SCOREBOARD,
	SCOREBOARD_SCORE,
	SCOREBOARD_TEAM,
	/**
	 * Name in player join messages
	 */
	PLAYER_JOIN,
	/**
	 * Name in player quit messages
	 */
	PLAYER_QUIT,
	/**
	 * Name in tab-completion suggestions
	 */
	CHAT_TAB_COMPLETE

}
