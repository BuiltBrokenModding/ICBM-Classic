package icbm.classic.datafix;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.refs.ICBMEntities;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.lib.NBTConstants;
import icbm.classic.world.missile.logic.flight.BallisticFlightLogicOld;
import icbm.classic.world.missile.logic.flight.DeadFlightLogic;
import icbm.classic.world.missile.logic.targeting.BallisticTargetingData;
import icbm.classic.world.missile.logic.targeting.BasicTargetData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EntityMissileDataFixer implements IFixableData {
    private static final String ENTITY_ID = "id";

    public static final EntityMissileDataFixer INSTANCE = new EntityMissileDataFixer();

    // TODO wrap everything in optionals in case any field is null

    @Override
    public CompoundTag fixTagCompound(CompoundTag existingSave) {
        // Missile rewrite [v4.2.0] changed registry name and with it the entire save/load, save is based on [v4.0.1] code
        if (existingSave.contains(ENTITY_ID) && existingSave.getString(ENTITY_ID).equalsIgnoreCase(ICBMClassicAPI.ID + ":missile")) {
            // Update registry name
            existingSave.putString("id", ICBMEntities.MISSILE_EXPLOSIVE.toString());

            // EntityProjectile v4 to missileRewrite
            convertProjectileTags(existingSave);

            // EntityMissile
            convertEntityMissileTags(existingSave);

            // EntityExplosiveMissile
            convertExplosiveData(existingSave);

            // Remove missile data
            DataFixerHelpers.removeTags(existingSave, "missileType");
            DataFixerHelpers.removeTags(existingSave, "target", "targetHeight");
            DataFixerHelpers.removeTags(existingSave, "launcherPos");
            DataFixerHelpers.removeTags(existingSave, "acceleration", "lockHeight", "preLaunchSmokeTimer");
            DataFixerHelpers.removeTags(existingSave, "additionalMissileData", "explosiveID");
            DataFixerHelpers.removeTags(existingSave, "sourcePos", "Shooter-UUID");
            DataFixerHelpers.removeTags(existingSave, "xTilePos", "yTilePos", "zTilePos");
            DataFixerHelpers.removeTags(existingSave, "sideTilePos", "inTileState");
            DataFixerHelpers.removeTags(existingSave, "life", "ticksInAir");
        } else if (existingSave.contains(ENTITY_ID) && existingSave.getString(ENTITY_ID).equalsIgnoreCase(ICBMEntities.MISSILE_EXPLOSIVE.toString())) {

            // Move hypersonic to sonic
            if (existingSave.contains("explosive")) {

                final CompoundTag stackSave = existingSave.getCompound(NBTConstants.EXPLOSIVE_STACK);
                if (stackSave.contains("Damage")) {
                    final int damage = stackSave.getInteger("Damage");

                    if (damage == ICBMExplosives.HYPERSONIC.getRegistryID()) {

                        // Change to sonic id
                        stackSave.setInteger("Damage", ICBMExplosives.SONIC.getRegistryID());

                        // Wipe out custom data, shouldn't exist but could crash a 3rd-party's code
                        stackSave.remove("tag");
                        stackSave.remove("ForgeCaps");
                    }
                }
            }
        }
        return existingSave;
    }

    private void convertEntityMissileTags(CompoundTag existingSave) {

        // missileType -> int
        // 0 -> launcher
        // 1 -> cruise
        // 2 -> rpg
        // 3 -> homing
        // 4 -> dead_aim
        final int missileType = existingSave.getInteger("missileType");

        final CompoundTag missile = new CompoundTag();
        existingSave.put("missile", missile);

        // Set doFlight to true as it can be assumed any missile in world is moving
        final CompoundTag missileFlags = new CompoundTag();
        missile.put("flags", missileFlags);
        missileFlags.setByte("doFlight", (byte) 1);

        convertTargetData(existingSave, missile, missileType);
        convertFlightLogic(existingSave, missile, missileType);
        convertMissileSource(existingSave, missile, missileType);
    }

    private void convertExplosiveData(CompoundTag existingSave) {
        //additionalMissileData -> compound
        //explosiveID -> int
        final int explosiveId = existingSave.getInteger("explosiveID");
        final CompoundTag explosiveData = existingSave.getCompound("additionalMissileData");

        final ExplosiveType data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(explosiveId);
        final ItemStack stack = ICBMClassicAPI.EX_MISSILE_REGISTRY.getDeviceStack(data);
        if (stack.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null)) {
            final IExplosive explosive = stack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
            if (explosive != null) {
                explosive.getCustomBlastData().merge(explosiveData);
            }
        }

        existingSave.put("explosive", stack.serializeNBT());
    }

    private void convertTargetData(CompoundTag existingSave, CompoundTag missile, int missileType) {
        if (existingSave.contains("target")) {
            final CompoundTag targetData = new CompoundTag();
            missile.put("target", targetData);

            final CompoundTag data = new CompoundTag();
            targetData.put("data", data);

            // Set id, old saves would have used missile type for this
            targetData.putString("id", missileType == 0 ? BallisticTargetingData.REG_NAME.toString() : BasicTargetData.REG_NAME.toString());

            // target -> compound with xyz
            data.setDouble("x", existingSave.getCompound("target").getDouble("x"));
            data.setDouble("y", existingSave.getCompound("target").getDouble("y"));
            data.setDouble("z", existingSave.getCompound("target").getDouble("z"));

            // targetHeight -> int
            if (missileType == 0) {
                data.setDouble("impact_height", existingSave.getInteger("targetHeight"));
            }
        }
    }

    private void convertFlightLogic(CompoundTag existingSave, CompoundTag missile, int missileType) {
        final CompoundTag targetData = new CompoundTag();
        missile.put("flight", targetData);

        final CompoundTag data = new CompoundTag();
        targetData.put("data", data);

        // Set id, old saves would have used missile type for this
        targetData.putString("id", missileType == 0 ? BallisticFlightLogicOld.REG_NAME.toString() : DeadFlightLogic.REG_NAME.toString()); //TODO verify for cruise and hand held

        // Ballistic missile
        if (missileType == 0) {

            int preLauncherTime = existingSave.getInteger("preLaunchSmokeTimer");
            double lockHeight = existingSave.getDouble("lockHeight");

            // Assume always false so we force reset calculations
            final CompoundTag flags = new CompoundTag();
            data.put("flags", flags);
            flags.setByte("flight_started", (byte) 0);


            final CompoundTag inputs = new CompoundTag();
            data.put("inputs", inputs);

            // Use vanilla Pos as start to keep missiles from completely missing targets
            // ...will still have odd results due to math changes
            inputs.setDouble("start_x", existingSave.getTagList("Pos", 6).getDoubleAt(0));
            inputs.setDouble("start_y", existingSave.getTagList("Pos", 6).getDoubleAt(1));
            inputs.setDouble("start_z", existingSave.getTagList("Pos", 6).getDoubleAt(2));

            // target -> compound with xyz, using it for end
            inputs.setDouble("end_x", existingSave.getCompound("target").getDouble("x"));
            inputs.setDouble("end_y", existingSave.getCompound("target").getDouble("y"));
            inputs.setDouble("end_z", existingSave.getCompound("target").getDouble("z"));

            // lockHeight -> double
            // preLaunchSmokeTimer -> int
            final CompoundTag timers = new CompoundTag();
            data.put("timers", timers);
            timers.setInteger("engine_warm_up", preLauncherTime);
            timers.setDouble("lock_height", lockHeight);
        }
        // Cruise
        else if (missileType == 1) {
            data.setInteger("fuel", ConfigMissile.CRUISE_FUEL);
        }
        // RPG
        else if (missileType == 2) {
            data.setInteger("fuel", ConfigMissile.HANDHELD_FUEL);
        }
    }

    private void convertMissileSource(CompoundTag existingSave, CompoundTag missile, int missileType) {

        // Ballistic
        if (missileType == 0) {

            final CompoundTag missileSource = new CompoundTag();
            missile.put("source", missileSource);

            final CompoundTag pos = new CompoundTag();
            missileSource.put("pos", pos);

            // launcherPos -> compound with x y z
            pos.setDouble("x", existingSave.getCompound("launcherPos").getDouble("x"));
            pos.setDouble("y", existingSave.getCompound("launcherPos").getDouble("y"));
            pos.setDouble("z", existingSave.getCompound("launcherPos").getDouble("z"));

            // old save didn't store source dimension, this might cause odd event messages in the future
        }
        // cruise missiles || rpg missile
        else if (missileType == 1 || missileType == 2) {
            final CompoundTag missileSource = new CompoundTag();
            missile.put("source", missileSource);

            final CompoundTag pos = new CompoundTag();
            missileSource.put("pos", pos);

            // launcherPos -> compound with x y z
            pos.setDouble("x", existingSave.getCompound("sourcePos").getDouble("x"));
            pos.setDouble("y", existingSave.getCompound("sourcePos").getDouble("y"));
            pos.setDouble("z", existingSave.getCompound("sourcePos").getDouble("z"));

            // old save didn't store source dimension, this might cause odd event messages in the future
        }
    }

    private void convertProjectileTags(CompoundTag existingSave) {

        // inGround -> byte
        final CompoundTag flags = new CompoundTag();
        existingSave.put("flags", flags);
        flags.setByte("ground", existingSave.getByte("inGround"));
        existingSave.remove("inGround");

        // inTileState -> int
        // xTilePos -> int
        // yTilePos -> int
        // zTilePos -> int
        // sideTilePos -> byte
        final CompoundTag ground = new CompoundTag();
        existingSave.put("ground", ground);

        final CompoundTag tilePos = new CompoundTag();
        tilePos.setInteger("x", existingSave.getInteger("xTilePos"));
        tilePos.setInteger("y", existingSave.getInteger("yTilePos"));
        tilePos.setInteger("z", existingSave.getInteger("zTilePos"));
        ground.put("pos", tilePos);

        ground.setByte("side", existingSave.getByte("sideTilePos"));
        final int oldBlockState = existingSave.getInteger("inTileState");
        final BlockState blockState = Block.getStateById(oldBlockState);
        ground.put("state", NBTUtil.writeBlockState(new CompoundTag(), blockState));

        // life -> short
        // ticksInAir -> int
        final CompoundTag ticks = new CompoundTag();
        existingSave.put("ticks", ticks);
        ticks.setInteger("air", existingSave.getInteger("ticksInAir"));
        ticks.setInteger("ground", existingSave.getInteger("life"));
    }

    @Override
    public int getFixVersion() {
        return 2;
    }
}
