package com.builtbroken.jlib.data.network;

import io.netty.buffer.ByteBuf;

/** Applied to objects that can write there own
 * data to the ByteBuf stream. If used with the
 * packet handler you need to have a constructor
 * to create the object on the other end.
 *
 * Created by robert on 1/11/2015.
 */
@Deprecated
public interface IByteBufWriter
{
    /**
     * @param buf a {@link ByteBuf} to write to.
     */
    ByteBuf writeBytes(ByteBuf buf);
}
