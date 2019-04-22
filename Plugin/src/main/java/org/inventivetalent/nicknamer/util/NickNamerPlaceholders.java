package org.inventivetalent.nicknamer.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.inventivetalent.nicknamer.NickNamerPlugin;
import org.inventivetalent.nicknamer.api.NickNamerAPI;

public class NickNamerPlaceholders extends PlaceholderExpansion
{

    @Override
    public String getIdentifier()
    {
        return "nicknamer";
    }

    @Override
    public String getAuthor()
    {
        return NickNamerPlugin.instance.getDescription().getAuthors().get(0);
    }

    @Override
    public String getVersion()
    {
        return NickNamerPlugin.instance.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier){

        if(player == null){
            return "";
        }

        if(identifier.equals("is_disguised")){
            return String.valueOf( NickNamerAPI.getNickManager().hasSkin(player.getUniqueId()) );
        }

        if(identifier.equals("is_nicked")){
            return String.valueOf( NickNamerAPI.getNickManager().isNicked(player.getUniqueId()) );
        }

        if(identifier.equals("display_name")){
            return NickNamerAPI.getNickManager().isNicked(player.getUniqueId()) ? NickNamerAPI.getNickManager().getNick(player.getUniqueId()) : player.getName();
        }

        return null;
    }

}
