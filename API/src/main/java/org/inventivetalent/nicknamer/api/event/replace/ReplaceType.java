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
