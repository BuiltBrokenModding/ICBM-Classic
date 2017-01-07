package icbm.classic.content.potion;

import com.builtbroken.mc.lib.transform.vector.Pos;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * A poison registry class used to register different types of poison effects.
 *
 * @author Calclavia
 */
public abstract class Poison
{
    public enum ArmorType
    {
        HELM,
        BODY,
        LEGGINGS,
        BOOTS
    }

    static HashMap<String, Poison> poisons = new HashMap();
    static BiMap<String, Integer> poisonIDs = HashBiMap.create();
    private static int maxID = 0;

    protected String name;
    protected EnumSet<ArmorType> armorRequired = EnumSet.range(ArmorType.HELM, ArmorType.BOOTS);
    protected boolean isDisabled = false;

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
        //isDisabled = References.CONFIGURATION.get("Disable Poison", "Disable " + this.name, false).getBoolean(false);
    }

    public String getName()
    {
        return this.name;
    }

    public final int getID()
    {
        return getID(this.getName());
    }

    public EnumSet<ArmorType> getArmorRequired()
    {
        return this.armorRequired;
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
                int blockID = targetPosition.getBlockID(world);

                if (blockID > 0)
                {
                    if (Block.blocksList[blockID] instanceof IAntiPoisonBlock)
                    {
                        if (((IAntiPoisonBlock) Block.blocksList[blockID]).isPoisonPrevention(world, targetPosition.intX(), targetPosition.intY(), targetPosition.intZ(), this.getName()))
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