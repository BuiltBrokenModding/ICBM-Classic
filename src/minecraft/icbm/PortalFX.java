package icbm;

import net.minecraft.src.EntityPortalFX;
import net.minecraft.src.World;
import universalelectricity.prefab.Vector3;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class PortalFX extends EntityPortalFX
{
	public PortalFX(World par1World, Vector3 position, float red, float green, float blue, float scale, double distance)
	{
		super(par1World, position.x, position.y, position.z, 0, 0, 0);
		this.particleScale = scale;
		try
		{
			ReflectionHelper.setPrivateValue(EntityPortalFX.class, this, this.particleScale, "portalParticleScale", "field_70571_a");
		}
		catch(Exception e)
		{
			FMLLog.warning("Failed to correctly spawn ICBM portal effects.");
		}
		this.renderDistanceWeight = distance;
	}

}
