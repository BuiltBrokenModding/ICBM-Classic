package icbm.classic.client.render.item;

import com.builtbroken.mc.client.SharedAssets;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.ex.Explosion;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderItemMissile implements IItemRenderer
{
    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
        Explosives ex = Explosives.get(item.getItemDamage());

        if (ex.handler instanceof Explosion)
        {
            Explosion missile = (Explosion) ex.handler;

            float scale = 0.07f;
            if (type == ItemRenderType.INVENTORY)
            {
                scale = 0.025f;
                float right = -0.5f;

                if (missile.getTier() == 2 || !missile.hasBlockForm())
                {
                    scale = scale / 1.5f;
                }
                else if (missile.getTier() == 3)
                {
                    scale = scale / 1.7f;
                    right = -0.65f;
                }
                else if (missile.getTier() == 4)
                {
                    scale = scale / 1.4f;
                    right = -0.45f;
                }

                GL11.glTranslatef(right + 0.6f, -0.5f, -0.5f);
            }

            if (type == ItemRenderType.EQUIPPED)
            {
                GL11.glTranslatef(1f, 0.3f, 0.5f);
                GL11.glRotatef(0, 0, 0, 1f);
            }
            else if (type == ItemRenderType.EQUIPPED_FIRST_PERSON)
            {
                GL11.glTranslatef(1.15f, -1f, 0.5f);
                GL11.glRotatef(0, 0, 0, 1f);
            }
            else
            {
                GL11.glRotatef(-90, 0, 0, 1f);
            }

            if (type == ItemRenderType.ENTITY)
            {
                scale = scale / 1.5f;
            }

            GL11.glScalef(scale, scale, scale);
            //FMLClientHandler.instance().getClient().renderEngine.bindTexture(missile.getMissileResource());
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(SharedAssets.GREY_TEXTURE);
            IModelCustom model = missile.getMissileModel();
            if (model != null)
            {
                model.renderAll();
            }
        }
    }
}
