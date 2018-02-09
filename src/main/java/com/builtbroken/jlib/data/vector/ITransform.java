package com.builtbroken.jlib.data.vector;

/**
 * Applied to objects that can transform vectors
 *
 * @Calclavia
 */
public interface ITransform
{
	IPos3D transform(IPos3D vector);
}
