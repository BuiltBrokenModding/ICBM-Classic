package icbm.classic.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Tommy Gun - West
 * Created using Tabula 7.0.0
 */
public class ModelTommyGun extends ModelBase
{
    public ModelRenderer MagwelTopPiece;
    public ModelRenderer FrontMagwel;
    public ModelRenderer MagwellTriggerGuard;
    public ModelRenderer TopTriggerGuard;
    public ModelRenderer Body;
    public ModelRenderer BackSight;
    public ModelRenderer Stock;
    public ModelRenderer CharchingHandle;
    public ModelRenderer shape18;
    public ModelRenderer Stock_1;
    public ModelRenderer Stock_2;
    public ModelRenderer Stock_3;
    public ModelRenderer Stock_4;
    public ModelRenderer Stock_5;
    public ModelRenderer Stock_6;
    public ModelRenderer Stock_7;
    public ModelRenderer Stock_8;
    public ModelRenderer Stock_9;
    public ModelRenderer Stock_10;
    public ModelRenderer Stock_11;
    public ModelRenderer Stock_12;
    public ModelRenderer Stock_13;
    public ModelRenderer shape33;
    public ModelRenderer Stock_14;
    public ModelRenderer Stock_15;
    public ModelRenderer Stock_16;
    public ModelRenderer Stock_17;
    public ModelRenderer Stock_18;
    public ModelRenderer Stock_19;
    public ModelRenderer Stock_20;
    public ModelRenderer Stock_21;
    public ModelRenderer Stock_22;
    public ModelRenderer Stock_23;
    public ModelRenderer Stock_24;
    public ModelRenderer Stock_25;
    public ModelRenderer Stock_26;
    public ModelRenderer shape33_1;
    public ModelRenderer Stock_27;
    public ModelRenderer Barrel;
    public ModelRenderer Handguard;
    public ModelRenderer FrontSight;
    public ModelRenderer TriggerGuard;
    public ModelRenderer TriggerBack;
    public ModelRenderer Grip;
    public ModelRenderer Grip_1;
    public ModelRenderer Grip_2;
    public ModelRenderer Grip_3;
    public ModelRenderer GripAngle;
    public ModelRenderer Mag;
    public ModelRenderer DrumMag;
    public ModelRenderer DrumMag_1;
    public ModelRenderer DrumMag_2;
    public ModelRenderer DrumMag_3;
    public ModelRenderer DrumMag_4;
    public ModelRenderer DrumMag_5;
    public ModelRenderer DrumMag_6;
    public ModelRenderer DrumMag_7;
    public ModelRenderer DrumMag_8;
    public ModelRenderer DrumMag_9;
    public ModelRenderer DrumMag_10;
    public ModelRenderer DrumMag_11;
    public ModelRenderer DrumMag_12;

    public ModelTommyGun()
    {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.DrumMag = new ModelRenderer(this, 0, 14);
        this.DrumMag.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.DrumMag.addBox(0.0F, 0.0F, 0.0F, 1, 1, 2, 0.0F);
        this.DrumMag_4 = new ModelRenderer(this, 0, 14);
        this.DrumMag_4.setRotationPoint(3.0F, 0.0F, 0.0F);
        this.DrumMag_4.addBox(0.0F, 0.0F, 0.0F, 3, 1, 2, 0.0F);
        this.setRotateAngle(DrumMag_4, 0.0F, 0.0F, 0.7853981633974483F);
        this.Stock_1 = new ModelRenderer(this, 14, 27);
        this.Stock_1.setRotationPoint(0.0F, 1.0F, 0.0F);
        this.Stock_1.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.setRotateAngle(Stock_1, 0.7853981633974483F, 0.0F, 0.0F);
        this.Stock_21 = new ModelRenderer(this, 0, 27);
        this.Stock_21.setRotationPoint(0.0F, 2.5F, -0.1F);
        this.Stock_21.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.setRotateAngle(Stock_21, 0.27314402793711257F, 0.0F, 0.0F);
        this.DrumMag_3 = new ModelRenderer(this, 21, 12);
        this.DrumMag_3.setRotationPoint(3.0F, 0.0F, 0.0F);
        this.DrumMag_3.addBox(0.0F, 0.0F, 0.0F, 3, 7, 2, 0.0F);
        this.setRotateAngle(DrumMag_3, 0.0F, 0.0F, 0.7853981633974483F);
        this.Stock_15 = new ModelRenderer(this, 14, 27);
        this.Stock_15.setRotationPoint(0.0F, 1.0F, 0.0F);
        this.Stock_15.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.setRotateAngle(Stock_15, 0.7853981633974483F, 0.0F, 0.0F);
        this.MagwelTopPiece = new ModelRenderer(this, 0, 0);
        this.MagwelTopPiece.setRotationPoint(-0.5F, -1.0F, 0.0F);
        this.MagwelTopPiece.addBox(0.0F, 0.0F, 0.0F, 2, 1, 2, 0.0F);
        this.CharchingHandle = new ModelRenderer(this, 0, 0);
        this.CharchingHandle.setRotationPoint(-0.3F, 0.5F, 0.4F);
        this.CharchingHandle.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.Stock_11 = new ModelRenderer(this, 12, 30);
        this.Stock_11.setRotationPoint(0.0F, 0.6F, 0.0F);
        this.Stock_11.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.setRotateAngle(Stock_11, 0.36425021489121656F, 0.0F, 0.0F);
        this.Stock_13 = new ModelRenderer(this, 13, 28);
        this.Stock_13.setRotationPoint(0.0F, 6.0F, -2.0F);
        this.Stock_13.addBox(0.0F, 0.0F, 0.0F, 1, 1, 2, 0.0F);
        this.setRotateAngle(Stock_13, 2.86844862565268F, 0.0F, 0.0F);
        this.Stock = new ModelRenderer(this, 0, 25);
        this.Stock.setRotationPoint(0.25F, 2.0F, 5.6F);
        this.Stock.addBox(0.0F, 0.0F, 0.0F, 1, 1, 3, 0.0F);
        this.FrontSight = new ModelRenderer(this, 0, 0);
        this.FrontSight.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.FrontSight.addBox(0.0F, -0.8F, 0.0F, 1, 1, 1, 0.0F);
        this.TopTriggerGuard = new ModelRenderer(this, 0, 0);
        this.TopTriggerGuard.setRotationPoint(0.0F, 0.6F, 2.0F);
        this.TopTriggerGuard.addBox(0.0F, 0.0F, 0.0F, 1, 1, 4, 0.0F);
        this.Stock_6 = new ModelRenderer(this, 12, 23);
        this.Stock_6.setRotationPoint(0.0F, 1.3F, 0.0F);
        this.Stock_6.addBox(0.0F, 0.0F, 0.0F, 1, 5, 1, 0.0F);
        this.setRotateAngle(Stock_6, -0.18203784098300857F, 0.0F, 0.0F);
        this.Stock_23 = new ModelRenderer(this, 4, 22);
        this.Stock_23.setRotationPoint(0.0F, 0.5F, 0.0F);
        this.Stock_23.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.setRotateAngle(Stock_23, 0.40980330836826856F, 0.0F, 0.0F);
        this.DrumMag_5 = new ModelRenderer(this, 0, 14);
        this.DrumMag_5.setRotationPoint(-0.8F, 1.2F, 0.0F);
        this.DrumMag_5.addBox(0.0F, 0.0F, 0.0F, 1, 1, 2, 0.0F);
        this.DrumMag_9 = new ModelRenderer(this, 0, 14);
        this.DrumMag_9.setRotationPoint(3.0F, 0.0F, 0.0F);
        this.DrumMag_9.addBox(0.0F, 0.0F, 0.0F, 3, 1, 2, 0.0F);
        this.setRotateAngle(DrumMag_9, 0.0F, 0.0F, 0.7853981633974483F);
        this.Stock_22 = new ModelRenderer(this, 10, 26);
        this.Stock_22.setRotationPoint(0.0F, 1.0F, 0.0F);
        this.Stock_22.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.setRotateAngle(Stock_22, 0.4553564018453205F, 0.0F, 0.0F);
        this.DrumMag_12 = new ModelRenderer(this, 0, 14);
        this.DrumMag_12.setRotationPoint(3.5F, 0.0F, 0.0F);
        this.DrumMag_12.addBox(0.0F, 0.0F, 0.0F, 1, 1, 2, 0.0F);
        this.Stock_9 = new ModelRenderer(this, 10, 26);
        this.Stock_9.setRotationPoint(0.0F, 1.0F, 0.0F);
        this.Stock_9.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.setRotateAngle(Stock_9, 0.4553564018453205F, 0.0F, 0.0F);
        this.Stock_18 = new ModelRenderer(this, 6, 25);
        this.Stock_18.setRotationPoint(0.0F, 0.0F, 0.49F);
        this.Stock_18.addBox(0.0F, 0.0F, 0.0F, 1, 4, 1, 0.0F);
        this.BackSight = new ModelRenderer(this, 0, 0);
        this.BackSight.setRotationPoint(0.5F, -0.1F, 6.0F);
        this.BackSight.addBox(0.0F, 0.0F, 0.0F, 1, 1, 2, 0.0F);
        this.Stock_2 = new ModelRenderer(this, 0, 25);
        this.Stock_2.setRotationPoint(0.5F, 0.0F, 0.0F);
        this.Stock_2.addBox(0.0F, 0.0F, 0.0F, 1, 1, 3, 0.0F);
        this.Body = new ModelRenderer(this, 0, 0);
        this.Body.setRotationPoint(0.0F, 0.0F, 2.0F);
        this.Body.addBox(0.0F, 0.0F, 0.0F, 2, 2, 9, 0.0F);
        this.Grip_1 = new ModelRenderer(this, 0, 23);
        this.Grip_1.setRotationPoint(-0.4F, 0.0F, 0.0F);
        this.Grip_1.addBox(0.0F, 0.0F, 0.0F, 1, 2, 2, 0.0F);
        this.Stock_8 = new ModelRenderer(this, 0, 27);
        this.Stock_8.setRotationPoint(0.0F, 2.5F, -0.1F);
        this.Stock_8.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.setRotateAngle(Stock_8, 0.27314402793711257F, 0.0F, 0.0F);
        this.Handguard = new ModelRenderer(this, 0, 22);
        this.Handguard.setRotationPoint(-0.5F, 0.5F, 2.0F);
        this.Handguard.addBox(0.0F, 0.0F, 0.0F, 2, 2, 8, 0.0F);
        this.GripAngle = new ModelRenderer(this, 0, 0);
        this.GripAngle.setRotationPoint(0.0F, 1.0F, 4.0F);
        this.GripAngle.addBox(0.0F, -1.0F, -1.0F, 1, 1, 1, 0.0F);
        this.setRotateAngle(GripAngle, -0.7740535232594852F, 0.0F, 0.0F);
        this.Stock_14 = new ModelRenderer(this, 5, 23);
        this.Stock_14.setRotationPoint(0.0F, 5.0F, 0.0F);
        this.Stock_14.addBox(0.0F, 0.0F, 0.0F, 1, 3, 3, 0.0F);
        this.setRotateAngle(Stock_14, 0.27314402793711257F, 0.0F, 0.0F);
        this.Stock_26 = new ModelRenderer(this, 13, 28);
        this.Stock_26.setRotationPoint(0.0F, 6.0F, -2.0F);
        this.Stock_26.addBox(0.0F, 0.0F, 0.0F, 1, 1, 2, 0.0F);
        this.setRotateAngle(Stock_26, 2.86844862565268F, 0.0F, 0.0F);
        this.TriggerGuard = new ModelRenderer(this, 0, 0);
        this.TriggerGuard.setRotationPoint(0.0F, 2.0F, 0.0F);
        this.TriggerGuard.addBox(0.0F, 0.0F, 0.0F, 1, 0, 2, 0.0F);
        this.shape33 = new ModelRenderer(this, 0, 27);
        this.shape33.setRotationPoint(0.0F, 0.0F, 1.33F);
        this.shape33.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.Stock_20 = new ModelRenderer(this, 4, 24);
        this.Stock_20.setRotationPoint(0.0F, 5.4F, 0.5F);
        this.Stock_20.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.DrumMag_10 = new ModelRenderer(this, 0, 14);
        this.DrumMag_10.setRotationPoint(-0.1F, 3.9F, 0.0F);
        this.DrumMag_10.addBox(0.0F, 0.0F, 0.0F, 1, 1, 2, 0.0F);
        this.Stock_19 = new ModelRenderer(this, 12, 23);
        this.Stock_19.setRotationPoint(0.0F, 1.3F, 0.0F);
        this.Stock_19.addBox(0.0F, 0.0F, 0.0F, 1, 5, 1, 0.0F);
        this.setRotateAngle(Stock_19, -0.18203784098300857F, 0.0F, 0.0F);
        this.DrumMag_11 = new ModelRenderer(this, 0, 14);
        this.DrumMag_11.setRotationPoint(3.5F, 3.7F, 0.0F);
        this.DrumMag_11.addBox(0.0F, 0.0F, 0.0F, 1, 1, 2, 0.0F);
        this.Mag = new ModelRenderer(this, 24, 13);
        this.Mag.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.Mag.addBox(0.0F, 0.0F, 0.0F, 1, 7, 2, 0.0F);
        this.Stock_12 = new ModelRenderer(this, 14, 24);
        this.Stock_12.setRotationPoint(0.0F, 1.0F, 1.0F);
        this.Stock_12.addBox(0.0F, 0.0F, -2.0F, 1, 6, 2, 0.0F);
        this.setRotateAngle(Stock_12, -1.3203415791337103F, 0.0F, 0.0F);
        this.Grip = new ModelRenderer(this, 0, 23);
        this.Grip.setRotationPoint(0.2F, 0.0F, 0.0F);
        this.Grip.addBox(0.0F, 0.0F, 0.0F, 1, 2, 2, 0.0F);
        this.Stock_3 = new ModelRenderer(this, 0, 25);
        this.Stock_3.setRotationPoint(0.0F, 1.0F, 0.0F);
        this.Stock_3.addBox(0.0F, 0.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(Stock_3, 0.6632251157578453F, 0.0F, 0.0F);
        this.Stock_24 = new ModelRenderer(this, 12, 30);
        this.Stock_24.setRotationPoint(0.0F, 0.6F, 0.0F);
        this.Stock_24.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.setRotateAngle(Stock_24, 0.36425021489121656F, 0.0F, 0.0F);
        this.DrumMag_7 = new ModelRenderer(this, 0, 14);
        this.DrumMag_7.setRotationPoint(3.0F, 0.0F, 0.0F);
        this.DrumMag_7.addBox(0.0F, 0.0F, 0.0F, 3, 1, 2, 0.0F);
        this.setRotateAngle(DrumMag_7, 0.0F, 0.0F, 0.7853981633974483F);
        this.shape33_1 = new ModelRenderer(this, 0, 27);
        this.shape33_1.setRotationPoint(0.0F, 0.0F, 1.33F);
        this.shape33_1.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.Grip_2 = new ModelRenderer(this, 0, 25);
        this.Grip_2.setRotationPoint(0.0F, 2.0F, 0.0F);
        this.Grip_2.addBox(0.0F, 0.0F, 0.0F, 1, 3, 2, 0.0F);
        this.setRotateAngle(Grip_2, 0.27314402793711257F, 0.0F, 0.0F);
        this.TriggerBack = new ModelRenderer(this, 0, 0);
        this.TriggerBack.setRotationPoint(0.0F, -2.0F, 2.0F);
        this.TriggerBack.addBox(0.0F, 0.0F, -0.01F, 1, 2, 2, 0.0F);
        this.DrumMag_2 = new ModelRenderer(this, 0, 14);
        this.DrumMag_2.setRotationPoint(2.0F, 0.0F, 0.0F);
        this.DrumMag_2.addBox(0.0F, 0.0F, 0.0F, 3, 1, 2, 0.0F);
        this.setRotateAngle(DrumMag_2, 0.0F, 0.0F, 0.7853981633974483F);
        this.Barrel = new ModelRenderer(this, 0, 0);
        this.Barrel.setRotationPoint(0.5F, 0.0F, -10.0F);
        this.Barrel.addBox(0.0F, 0.0F, 0.0F, 1, 1, 10, 0.0F);
        this.Stock_16 = new ModelRenderer(this, 0, 25);
        this.Stock_16.setRotationPoint(0.0F, 1.0F, 0.0F);
        this.Stock_16.addBox(0.0F, 0.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(Stock_16, 0.6632251157578453F, 0.0F, 0.0F);
        this.Grip_3 = new ModelRenderer(this, 0, 25);
        this.Grip_3.setRotationPoint(-0.4F, 0.0F, 0.0F);
        this.Grip_3.addBox(0.0F, 0.0F, 0.0F, 1, 3, 2, 0.0F);
        this.Stock_17 = new ModelRenderer(this, 10, 24);
        this.Stock_17.setRotationPoint(0.0F, 0.3F, 0.0F);
        this.Stock_17.addBox(0.0F, 0.0F, 0.0F, 1, 7, 1, 0.0F);
        this.setRotateAngle(Stock_17, -0.18203784098300857F, 0.0F, 0.0F);
        this.MagwellTriggerGuard = new ModelRenderer(this, 0, 0);
        this.MagwellTriggerGuard.setRotationPoint(0.0F, 0.5F, 2.01F);
        this.MagwellTriggerGuard.addBox(0.0F, 0.0F, 0.0F, 1, 2, 0, 0.0F);
        this.Stock_27 = new ModelRenderer(this, 5, 23);
        this.Stock_27.setRotationPoint(0.0F, 5.0F, 0.0F);
        this.Stock_27.addBox(0.0F, 0.0F, 0.0F, 1, 3, 3, 0.0F);
        this.setRotateAngle(Stock_27, 0.27314402793711257F, 0.0F, 0.0F);
        this.Stock_5 = new ModelRenderer(this, 5, 25);
        this.Stock_5.setRotationPoint(0.0F, 0.0F, 0.49F);
        this.Stock_5.addBox(0.0F, 0.0F, 0.0F, 1, 4, 1, 0.0F);
        this.shape18 = new ModelRenderer(this, 0, 0);
        this.shape18.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.shape18.addBox(0.0F, 0.0F, 0.0F, 1, 1, 2, 0.0F);
        this.setRotateAngle(shape18, 0.22759093446006054F, 0.0F, 0.0F);
        this.Stock_25 = new ModelRenderer(this, 14, 24);
        this.Stock_25.setRotationPoint(0.0F, 1.0F, 1.0F);
        this.Stock_25.addBox(0.0F, 0.0F, -2.0F, 1, 6, 2, 0.0F);
        this.setRotateAngle(Stock_25, -1.3203415791337103F, 0.0F, 0.0F);
        this.Stock_10 = new ModelRenderer(this, 4, 22);
        this.Stock_10.setRotationPoint(0.0F, 0.5F, 0.0F);
        this.Stock_10.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.setRotateAngle(Stock_10, 0.40980330836826856F, 0.0F, 0.0F);
        this.DrumMag_8 = new ModelRenderer(this, 0, 14);
        this.DrumMag_8.setRotationPoint(3.0F, 0.0F, 0.0F);
        this.DrumMag_8.addBox(0.0F, 0.0F, 0.0F, 3, 1, 2, 0.0F);
        this.setRotateAngle(DrumMag_8, 0.0F, 0.0F, 0.7853981633974483F);
        this.DrumMag_1 = new ModelRenderer(this, 23, 14);
        this.DrumMag_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.DrumMag_1.addBox(-1.0F, 0.0F, 0.0F, 3, 7, 2, 0.0F);
        this.FrontMagwel = new ModelRenderer(this, 0, 0);
        this.FrontMagwel.setRotationPoint(-0.5F, -1.0F, -2.0F);
        this.FrontMagwel.addBox(0.0F, 0.0F, 0.0F, 2, 2, 2, 0.0F);
        this.Stock_4 = new ModelRenderer(this, 10, 24);
        this.Stock_4.setRotationPoint(0.0F, 0.3F, 0.0F);
        this.Stock_4.addBox(0.0F, 0.0F, 0.0F, 1, 7, 1, 0.0F);
        this.setRotateAngle(Stock_4, -0.18203784098300857F, 0.0F, 0.0F);
        this.DrumMag_6 = new ModelRenderer(this, 0, 14);
        this.DrumMag_6.setRotationPoint(3.0F, 0.0F, 0.0F);
        this.DrumMag_6.addBox(0.0F, 0.0F, 0.0F, 3, 1, 2, 0.0F);
        this.setRotateAngle(DrumMag_6, 0.0F, 0.0F, 0.7853981633974483F);
        this.Stock_7 = new ModelRenderer(this, 4, 24);
        this.Stock_7.setRotationPoint(0.0F, 5.4F, 0.5F);
        this.Stock_7.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);

        this.DrumMag.addChild(this.DrumMag_1);
        this.DrumMag_1.addChild(this.DrumMag_2);
        this.DrumMag_2.addChild(this.DrumMag_3);
        this.DrumMag_3.addChild(this.DrumMag_4);
        this.DrumMag_3.addChild(this.DrumMag_5);
        this.DrumMag_4.addChild(this.DrumMag_6);
        this.DrumMag_5.addChild(this.DrumMag_10);
        this.DrumMag_5.addChild(this.DrumMag_11);
        this.DrumMag_5.addChild(this.DrumMag_12);
        this.DrumMag_6.addChild(this.DrumMag_7);
        this.DrumMag_7.addChild(this.DrumMag_8);
        this.DrumMag_8.addChild(this.DrumMag_9);



        this.Stock.addChild(this.Stock_1);
        this.Stock_18.addChild(this.Stock_21);

        this.Stock_2.addChild(this.Stock_15);
        this.Body.addChild(this.CharchingHandle);
        this.Stock_10.addChild(this.Stock_11);
        this.Stock_12.addChild(this.Stock_13);
        this.Body.addChild(this.Stock);
        this.Barrel.addChild(this.FrontSight);
        this.Stock_4.addChild(this.Stock_6);
        this.Stock_22.addChild(this.Stock_23);


        this.Stock_21.addChild(this.Stock_22);

        this.Stock_8.addChild(this.Stock_9);
        this.Stock_17.addChild(this.Stock_18);
        this.Body.addChild(this.BackSight);
        this.Stock.addChild(this.Stock_2);
        this.MagwelTopPiece.addChild(this.Body);
        this.Grip.addChild(this.Grip_1);
        this.Stock_5.addChild(this.Stock_8);
        this.Barrel.addChild(this.Handguard);
        this.TopTriggerGuard.addChild(this.GripAngle);
        this.Stock_6.addChild(this.Stock_14);
        this.Stock_25.addChild(this.Stock_26);
        this.MagwellTriggerGuard.addChild(this.TriggerGuard);
        this.Stock_13.addChild(this.shape33);
        this.Stock_17.addChild(this.Stock_20);

        this.Stock_17.addChild(this.Stock_19);

        this.Stock_11.addChild(this.Stock_12);
        this.TriggerBack.addChild(this.Grip);
        this.Stock_1.addChild(this.Stock_3);
        this.Stock_23.addChild(this.Stock_24);

        this.Stock_26.addChild(this.shape33_1);
        this.Grip.addChild(this.Grip_2);
        this.TriggerGuard.addChild(this.TriggerBack);

        this.FrontMagwel.addChild(this.Barrel);
        this.Stock_15.addChild(this.Stock_16);
        this.Grip_2.addChild(this.Grip_3);
        this.Stock_16.addChild(this.Stock_17);
        this.Stock_19.addChild(this.Stock_27);
        this.Stock_4.addChild(this.Stock_5);
        this.BackSight.addChild(this.shape18);
        this.Stock_24.addChild(this.Stock_25);
        this.Stock_9.addChild(this.Stock_10);

        this.Stock_3.addChild(this.Stock_4);

        this.Stock_4.addChild(this.Stock_7);
    }

    public void render(float f5)
    {
        this.DrumMag.render(f5);
        this.MagwelTopPiece.render(f5);
        this.TopTriggerGuard.render(f5);
        this.Mag.render(f5);
        this.MagwellTriggerGuard.render(f5);
        this.FrontMagwel.render(f5);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        this.render(f5);
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
