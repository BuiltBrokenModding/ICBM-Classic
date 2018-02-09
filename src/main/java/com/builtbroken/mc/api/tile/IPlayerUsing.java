package com.builtbroken.mc.api.tile;

import net.minecraft.entity.player.EntityPlayer;

import java.util.Collection;

/**
 * Used to track players currently using an object. Primaryly used
 * for GUI handling.
 * Created by robert on 1/12/2015.
 */
public interface IPlayerUsing
{
    Collection<EntityPlayer> getPlayersUsing();

    default boolean addPlayerToUseList(EntityPlayer player)
    {
        return getPlayersUsing().add(player);
    }

    default boolean removePlayerToUseList(EntityPlayer player)
    {
        return getPlayersUsing().remove(player);
    }
}
