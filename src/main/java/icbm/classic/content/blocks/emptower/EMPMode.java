package icbm.classic.content.blocks.emptower;

import icbm.classic.content.blast.BlastEMP;

import java.util.function.Consumer;

/**
 * Created by Dark(DarkGuardsman, Robert) on 12/15/2019.
 */
public enum EMPMode
{
    ALL((emp) -> emp.setEffectBlocks().setEffectEntities()),
    MISSILES_ONLY((emp) -> emp.setEffectEntities()),
    ELECTRICITY_ONLY((emp) ->  emp.setEffectBlocks());

    public final Consumer<BlastEMP> applySettings;

    EMPMode(Consumer<BlastEMP> applySettings)
    {
        this.applySettings = applySettings;
    }
}
