package com.builtbroken.jlib.data.network;

import io.netty.buffer.ByteBuf;

/** Applied to objects that can read data from a byte buf to
 * update their own contents.
 *
 * Created by robin on 1/11/2015.
 */
public interface IByteBufReader
{
    /** Called to read an object from byte buf
     * @param buf a {@link ByteBuf} to read from
     * @return instance of the object
     */
    Object readBytes(ByteBuf buf);
}
