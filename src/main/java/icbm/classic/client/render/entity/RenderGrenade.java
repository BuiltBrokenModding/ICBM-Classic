package icbm.classic.client.render.entity;

import icbm.classic.world.entity.GrenadeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItemEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderGrenade extends Render<GrenadeEntity> {
    private ItemEntity entityItem;
    private RenderItemEntity renderItemEntity;

    public RenderGrenade(RenderManager renderManagerIn) {
        super(renderManagerIn);
        renderItemEntity = new RenderItemEntity(renderManagerIn, Minecraft.getMinecraft().getRenderItem());
        this.shadowSize = 0.15F;
        this.shadowOpaque = 0.75F;
    }

    @Override
    public void doRender(GrenadeEntity entity, double x, double y, double z, float par8, float par9) {
        setupFakeItem(entity);
        renderItemEntity.doRender(entityItem, x, y, z, par8, par9);
    }

    protected void setupFakeItem(GrenadeEntity entity) {

        //Create fake item if missing
        if (entityItem == null) {
            entityItem = new ItemEntity(entity.world);
        }

        //Apply data from entity
        entityItem.setLevel(entity.world);
        entityItem.setPosition(entity.getX(), entity.getY(), entity.getZ());
        entityItem.setItem(entity.explosive.toStack());
    }

    @Override
    protected ResourceLocation getEntityTexture(GrenadeEntity entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
