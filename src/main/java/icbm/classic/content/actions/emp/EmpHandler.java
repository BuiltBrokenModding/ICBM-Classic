package icbm.classic.content.actions.emp;

import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.config.util.BlockStateConfigList;

public class EmpHandler {
    public static final BlockStateConfigList.BlockChanceOut empBlockSwaps = new BlockStateConfigList.BlockChanceOut("[EmpReplacements]", (configList) -> {
        configList.load("icbmclassic/blast/emp/replacements/list", ConfigBlast.emp.replacements.blockStates);
    });

    public static void loadFromConfig() {
        empBlockSwaps.reload();
    }

    public static void setup() {
        loadFromConfig();
    }
}
