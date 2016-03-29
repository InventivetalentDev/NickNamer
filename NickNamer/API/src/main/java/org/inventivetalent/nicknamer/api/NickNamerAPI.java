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

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.apihelper.API;
import org.inventivetalent.apihelper.APIManager;
import org.inventivetalent.nicknamer.api.event.disguise.NickDisguiseEvent;
import org.inventivetalent.nicknamer.api.event.disguise.SkinDisguiseEvent;
import org.inventivetalent.packetlistener.PacketListenerAPI;
import org.inventivetalent.packetlistener.handler.PacketHandler;

public class NickNamerAPI implements API, Listener {

	private static   NickManager    nickManager;
	protected static PacketListener packetListener;

	public static NickManager getNickManager() {
		return nickManager;
	}

	//	public static void setNickManager(NickManager nickManager_) {
	//		if (nickManager != null) { throw new IllegalStateException("NickManager already set"); }
	//		nickManager = nickManager_;
	//	}

	@Override
	public void load() {
		APIManager.require(PacketListenerAPI.class, null);
	}

	@Override
	public void init(Plugin plugin) {
		APIManager.initAPI(PacketListenerAPI.class);

		APIManager.registerEvents(this, this);

		nickManager = new NickManagerImpl(plugin);
		PacketHandler.addHandler(packetListener = new PacketListener(plugin));
	}

	@Override
	public void disable(Plugin plugin) {
		PacketHandler.removeHandler(packetListener);
		APIManager.disableAPI(PacketListenerAPI.class);
	}

	// Internal event listeners

	@EventHandler(priority = EventPriority.LOWEST)
	public void on(NickDisguiseEvent event) {
		if (getNickManager().isNicked(event.getPlayer().getUniqueId())) {
			event.setNick(getNickManager().getNick(event.getPlayer().getUniqueId()));
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void on(SkinDisguiseEvent event) {
		if (getNickManager().hasSkin(event.getPlayer().getUniqueId())) {
			event.setSkin(getNickManager().getSkin(event.getPlayer().getUniqueId()));
		}
	}


}


