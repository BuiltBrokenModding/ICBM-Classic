package icbm.classic.client.models;


import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

// Made with Blockbench 4.6.4
public class CruiseLauncherTopModel extends ModelICBM {

	private final ModelRenderer yaw;
	private final ModelRenderer pitch;
	private final ModelRenderer arm1;
	private final ModelRenderer arm2;

	public CruiseLauncherTopModel() {
		textureWidth = 64;
		textureHeight = 64;

		yaw = new ModelRenderer(this);
		yaw.setRotationPoint(0.0F, 24.0F, 0.0F);
		yaw.cubeList.add(new ModelBox(yaw, 0, 56, -8.0F, -2.0F, -3.0F, 16, 2, 6, 0.0F, false));

		pitch = new ModelRenderer(this);
		pitch.setRotationPoint(0.0F, -15.0F, 0.0F);
		yaw.addChild(pitch);
		pitch.cubeList.add(new ModelBox(pitch, 0, 13, 4.0F, -2.0F, -7.0F, 2, 5, 14, 0.0F, false));
		pitch.cubeList.add(new ModelBox(pitch, 30, 13, -6.0F, -2.0F, -7.0F, 2, 5, 14, 0.0F, false));
		pitch.cubeList.add(new ModelBox(pitch, 3, 0, -4.0F, 1.0F, -6.0F, 8, 2, 4, 0.0F, false));
		pitch.cubeList.add(new ModelBox(pitch, 27, 0, -4.0F, 1.0F, 2.0F, 8, 2, 4, 0.0F, false));

		arm1 = new ModelRenderer(this);
		arm1.setRotationPoint(0.0F, 0.0F, 0.0F);
		yaw.addChild(arm1);
		arm1.cubeList.add(new ModelBox(arm1, 6, 33, 6.0F, -17.0F, -3.0F, 2, 5, 6, 0.0F, false));
		arm1.cubeList.add(new ModelBox(arm1, 2, 44, 6.0F, -12.0F, -3.0F, 2, 10, 2, 0.0F, false));
		arm1.cubeList.add(new ModelBox(arm1, 18, 44, 6.0F, -12.0F, 1.0F, 2, 10, 2, 0.0F, false));

		arm2 = new ModelRenderer(this);
		arm2.setRotationPoint(-14.0F, 0.0F, 0.0F);
		yaw.addChild(arm2);
		arm2.cubeList.add(new ModelBox(arm2, 6, 33, 6.0F, -17.0F, -3.0F, 2, 5, 6, 0.0F, false));
		arm2.cubeList.add(new ModelBox(arm2, 2, 44, 6.0F, -12.0F, -3.0F, 2, 10, 2, 0.0F, false));
		arm2.cubeList.add(new ModelBox(arm2, 18, 44, 6.0F, -12.0F, 1.0F, 2, 10, 2, 0.0F, false));
	}

    public void render(float scale, float rotYaw, float rotPitch)
    {
        yaw.rotateAngleY = rotYaw;
        pitch.rotateAngleX = rotPitch;

        yaw.render(scale);
    }
}