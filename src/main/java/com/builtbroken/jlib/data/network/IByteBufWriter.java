package com.builtbroken.jlib.data.network;

import io.netty.buffer.ByteBuf;

/** Applied to objects that can write there own
 * data to the ByteBuf stream. If used with the
 * packet handler you need to have a constructor
 * to create the object on the other end.
 *
 */
@Deprecated
public interface IByteBufWriter
{
    /**
     * @param buf a {@link ByteBuf} to write to.
     */
    ByteBuf writeBytes(ByteBuf buf);
}
// PORTED
