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

package org.inventivetalent.nicknamer;

import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.apihelper.APIManager;
import org.inventivetalent.nicknamer.api.NickNamerAPI;
import org.inventivetalent.nicknamer.command.NickCommands;
import org.inventivetalent.nicknamer.command.SkinCommands;
import org.inventivetalent.packetlistener.PacketListenerAPI;
import org.inventivetalent.pluginannotations.PluginAnnotations;

public class NickNamerPlugin extends JavaPlugin {

	public static NickNamerPlugin instance;

	public NickCommands nickCommands;
	public SkinCommands skinCommands;

	@Override
	public void onLoad() {
		APIManager.require(PacketListenerAPI.class, this);
		APIManager.registerAPI(new NickNamerAPI(), this);
	}

	@Override
	public void onEnable() {
		instance = this;
		APIManager.initAPI(PacketListenerAPI.class);
		APIManager.initAPI(NickNamerAPI.class);

		saveDefaultConfig();

		PluginAnnotations.COMMAND.registerCommands(this, nickCommands = new NickCommands(this));
		PluginAnnotations.COMMAND.registerCommands(this, skinCommands = new SkinCommands(this));
	}

	@Override
	public void onDisable() {
		APIManager.disableAPI(NickNamerAPI.class);
	}
}
