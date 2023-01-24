package icbm.classic.datafix;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.missiles.IMissileSource;
import icbm.classic.api.refs.ICBMEntities;
import icbm.classic.content.blocks.launcher.LauncherMissileSource;
import icbm.classic.content.missile.logic.flight.BallisticFlightLogic;
import icbm.classic.content.missile.logic.flight.DeadFlightLogic;
import icbm.classic.content.missile.targeting.BallisticTargetingData;
import icbm.classic.content.missile.targeting.BasicTargetData;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.datafix.IFixableData;

import java.util.Optional;
import java.util.UUID;

public class EntityMissileDataFixer implements IFixableData
{
    private static final String ENTITY_ID = "id";

    // TODO wrap everything in optionals in case any field is null

    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound existingSave)
    {
        // Missile rewrite [v????] changed registry name and with it the entire save/load
        if (existingSave.hasKey(ENTITY_ID) && existingSave.getString(ENTITY_ID).equalsIgnoreCase(ICBMClassicAPI.ID + ":missile"))
        {
            // Update registry name
            existingSave.setString("id", ICBMEntities.MISSILE_EXPLOSIVE.toString());

            // EntityProjectile v4 to missileRewrite
            convertProjectileTags(existingSave);

            // EntityMissile
            convertProjectileTags(existingSave);

            // EntityExplosiveMissile
            //additionalMissileData -> compound
            //explosiveID -> int
        }
        return existingSave;
    }
    private void convertEntityMissileTags(NBTTagCompound existingSave) {

        // missileType -> int
        // 0 -> launcher
        // 1 -> cruise
        // 2 -> rpg
        // 3 -> homing
        // 4 -> dead_aim
        final int missileType = existingSave.getInteger("missileType");

        final NBTTagCompound missile = new NBTTagCompound();
        existingSave.setTag("missile", missile);

        // Set doFlight to true as it can be assumed any missile in world is moving
        final NBTTagCompound missileFlags = new NBTTagCompound();
        missile.setTag("flags", missileFlags);
        missileFlags.setByte("doFlight", (byte)1);

        convertTargetData(existingSave, missile, missileType);
        convertFlightLogic(existingSave, missile, missileType);
        convertTargetSource(existingSave, missile, missileType);

        // Remove missile data
        removeTags(existingSave, "missileType");
        removeTags(existingSave, "target", "targetHeight");
        removeTags(existingSave, "launcherPos");
        removeTags(existingSave, "acceleration", "lockHeight", "preLaunchTimer");
    }

    private void convertTargetData(NBTTagCompound existingSave, NBTTagCompound missile, int missileType) {

        final NBTTagCompound targetData = new NBTTagCompound();
        missile.setTag("target", targetData);

        final NBTTagCompound data = new NBTTagCompound();
        missile.setTag("data", data);

        // Set id, old saves would have used missile type for this
        data.setString("id", missileType == 0 ? BallisticTargetingData.REG_NAME.toString() : BasicTargetData.REG_NAME.toString());

        // target -> compound with xyz
        data.setDouble("x", existingSave.getCompoundTag("target").getDouble("x"));
        data.setDouble("y", existingSave.getCompoundTag("target").getDouble("y"));
        data.setDouble("z", existingSave.getCompoundTag("target").getDouble("z"));

        // targetHeight -> int
        if(missileType == 0) {
            data.setDouble("impact_height", existingSave.getInteger("targetHeight"));
        }

    }

    private void convertFlightLogic(NBTTagCompound existingSave, NBTTagCompound missile, int missileType) {
        final NBTTagCompound targetData = new NBTTagCompound();
        missile.setTag("flight", targetData);

        final NBTTagCompound data = new NBTTagCompound();
        missile.setTag("data", data);

        // Set id, old saves would have used missile type for this
        data.setString("id", missileType == 0 ? BallisticFlightLogic.REG_NAME.toString() : DeadFlightLogic.REG_NAME.toString()); //TODO verify for cruise and hand held


        if(missileType == 0) {

            int preLauncherTime = existingSave.getInteger("preLaunchTimer");
            double lockHeight = existingSave.getDouble("lockHeight");

            // Assume always false so we force reset calculations
            final NBTTagCompound flags = new NBTTagCompound();
            data.setTag("flags", flags);
            flags.setByte("flight_started",  (byte)0);


            final NBTTagCompound inputs = new NBTTagCompound();
            data.setTag("inputs", inputs);

            // Use vanilla Pos as start to keep missiles from completely missing targets
            // ...will still have odd results due to math changes
            data.setDouble("start_x", existingSave.getTagList("Pos", 6).getDoubleAt(0));
            data.setDouble("start_y", existingSave.getTagList("Pos", 6).getDoubleAt(1));
            data.setDouble("start_z", existingSave.getTagList("Pos", 6).getDoubleAt(2));

            // target -> compound with xyz, using it for end
            data.setDouble("end_x", existingSave.getCompoundTag("target").getDouble("x"));
            data.setDouble("end_y", existingSave.getCompoundTag("target").getDouble("y"));
            data.setDouble("end_z", existingSave.getCompoundTag("target").getDouble("z"));

            // lockHeight -> double
            // preLaunchTimer -> int
            final NBTTagCompound timers = new NBTTagCompound();
            data.setTag("time", timers);
            timers.setInteger("engine_warm_up", preLauncherTime);
            timers.setDouble("lock_height", lockHeight);
        }
    }

    private void convertTargetSource(NBTTagCompound existingSave, NBTTagCompound missile, int missileType) {
        if(missileType == 0) {

            final NBTTagCompound targetData = new NBTTagCompound();
            missile.setTag("source", targetData);

            final NBTTagCompound data = new NBTTagCompound();
            missile.setTag("data", data);

            // Set id, old saves would have used missile type for this
            data.setString("id", LauncherMissileSource.REG_NAME.toString());

            final NBTTagCompound blockPos = new NBTTagCompound();
            data.setTag("block_pos", blockPos);

            // launcherPos -> compound with x y z
            blockPos.setInteger("x", (int)Math.floor(existingSave.getCompoundTag("launcherPos").getDouble("x")));
            blockPos.setInteger("y", (int)Math.floor(existingSave.getCompoundTag("launcherPos").getDouble("y")));
            blockPos.setInteger("z", (int)Math.floor(existingSave.getCompoundTag("launcherPos").getDouble("z")));

            // old save didn't store source dimension, this might cause odd event messages in the future
        }
    }

    private void convertProjectileTags(NBTTagCompound existingSave) {

        // inGround -> byte
        final NBTTagCompound flags = new NBTTagCompound();
        existingSave.setTag("flags", flags);
        flags.setByte("ground", existingSave.getByte("inGround"));
        existingSave.removeTag("inGround");

        // inTileState -> int
        // xTilePos -> int
        // yTilePos -> int
        // zTilePos -> int
        // sideTilePos -> byte
        final NBTTagCompound ground = new NBTTagCompound();
        existingSave.setTag("ground", ground);

        final NBTTagCompound tilePos = new NBTTagCompound();
        tilePos.setInteger("x", existingSave.getInteger("xTilePos"));
        tilePos.setInteger("y", existingSave.getInteger("yTilePos"));
        tilePos.setInteger("z", existingSave.getInteger("zTilePos"));
        removeTags(existingSave, "xTilePos", "yTilePos", "zTilePos");
        ground.setTag("pos", tilePos);

        ground.setByte("side", existingSave.getByte("sideTilePos"));
        ground.setInteger("state", existingSave.getInteger("inTileState"));
        removeTags(existingSave, "sideTilePos", "inTileState");

        // life -> short
        // ticksInAir -> int
        final NBTTagCompound ticks = new NBTTagCompound();
        existingSave.setTag("ticks", ticks);
        ticks.setInteger("air", existingSave.getInteger("ticksInAir"));
        ticks.setInteger("ground", existingSave.getInteger("life"));
        removeTags(existingSave, "life", "ticksInAir");

        // sourcePos -> compound with x y z
        // Shooter-UUID -> string
        final NBTTagCompound source = new NBTTagCompound();
        existingSave.setTag("source", source);
        final UUID uuid = UUID.fromString(existingSave.getString("Shooter-UUID"));
        source.setTag("uuid", NBTUtil.createUUIDTag(uuid));
        source.setTag("pos", existingSave.getCompoundTag("sourcePos"));
        removeTags(existingSave, "sourcePos", "Shooter-UUID");
    }

    private void removeTags(NBTTagCompound compound, String... tags) {
        for(String str : tags) {
            compound.removeTag(str);
        }
    }

    @Override
    public int getFixVersion()
    {
        return 1;
    }
}
