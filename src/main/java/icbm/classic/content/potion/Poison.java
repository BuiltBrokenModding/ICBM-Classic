package icbm.classic.content.potion;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.HashMap;

/**
 * A poison registry class used to register different types of poison effects.
 *
 * @author Calclavia
 */
public abstract class Poison
{
    static HashMap<String, Poison> poisons = new HashMap();
    static BiMap<String, Integer> poisonIDs = HashBiMap.create();
    private static int maxID = 0;

    protected String name;

    public static Poison getPoison(String name)
    {
        return poisons.get(name);
    }

    public static Poison getPoison(int id)
    {
        return poisons.get(getName(id));
    }

    public static String getName(int fluidID)
    {
        return poisonIDs.inverse().get(fluidID);
    }

    public static int getID(String name)
    {
        return poisonIDs.get(name);
    }

    public Poison(String name)
    {
        this.name = name;
        poisons.put(name, this);
        poisonIDs.put(name, ++maxID);
    }

    public String getName()
    {
        return this.name;
    }

    public final int getID()
    {
        return getID(this.getName());
    }

    /**
     * Called to poison this specific entity with this specific type of poison.
     *
     * @param entity
     * @amiplifier - The amplification value.
     * @armorRequired - The amount of pieces of armor required to be protected.
     */
    public void poisonEntity(Pos emitPosition, EntityLivingBase entity, int amplifier)
    {
        if (!isEntityProtected(emitPosition, entity, amplifier))
        {
            doPoisonEntity(emitPosition, entity, amplifier);
        }
    }

    public void poisonEntity(Pos emitPosition, EntityLivingBase entity)
    {
        this.poisonEntity(emitPosition, entity, 0);
    }

    public boolean isEntityProtected(Pos emitPosition, EntityLivingBase entity, int amplifier)
    {
        if(entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode)
        {
            return true;
        }
        /* EnumSet<ArmorType> armorWorn = EnumSet.noneOf(ArmorType.class);

        if (entity instanceof EntityPlayer)
        {
            EntityPlayer entityPlayer = (EntityPlayer) entity;

            for (int i = 0; i < entityPlayer.inventory.armorInventory.length; i++)
            {
                if (entityPlayer.inventory.armorInventory[i] != null)
                {
                    if (entityPlayer.inventory.armorInventory[i].getItem() instanceof IAntiPoisonArmor)
                    {
                        IAntiPoisonArmor armor = (IAntiPoisonArmor) entityPlayer.inventory.armorInventory[i].getItem();

                        if (armor.isProtectedFromPoison(entityPlayer.inventory.armorInventory[i], entity, this.getName()))
                        {
                            armorWorn.add(ArmorType.values()[armor.getArmorType() % ArmorType.values().length]);
                            // TODO: Consider putting this in another method.
                            armor.onProtectFromPoison(entityPlayer.inventory.armorInventory[i], entity, this.getName());
                        }
                    }
                }
            }
        }

        return armorWorn.containsAll(this.armorRequired);
         */
        return false;
    }

    public int getAntiPoisonBlockCount(World world, Pos startingPosition, Pos endingPosition)
    {
        /* Pos delta = endingPosition.clone().subtract(startingPosition).normalize();
        Pos targetPosition = startingPosition.clone();
        double totalDistance = startingPosition.distance(endingPosition);

        int count = 0;

        if (totalDistance > 1)
        {
            while (targetPosition.distance(endingPosition) <= totalDistance)
            {
                int block = targetPosition.getBlockID(world);

                if (block > 0)
                {
                    if (Block.blocksList[block] instanceof IAntiPoisonBlock)
                    {
                        if (((IAntiPoisonBlock) Block.blocksList[block]).isPoisonPrevention(world, targetPosition.intX(), targetPosition.intY(), targetPosition.intZ(), this.getName()))
                        {
                            count++;
                        }
                    }
                }

                targetPosition.add(delta);
            }
        }

        return count;
        */
        return 0;
    }

    protected abstract void doPoisonEntity(Pos emitPosition, EntityLivingBase entity, int amplifier);
}