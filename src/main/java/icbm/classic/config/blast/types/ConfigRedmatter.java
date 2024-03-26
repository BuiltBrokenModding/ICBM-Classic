package icbm.classic.config.blast.types;

import net.neoforged.common.config.Config;

/**
 * Created by Dark(DarkGuardsman, Robert) on 2/9/2020.
 */
public class ConfigRedmatter {
    @Config.Name("redmatter_max_size")
    @Config.Comment("Largest size the redmatter can grow into before stopping.")
    public float MAX_SIZE = 70;

    @Config.Name("redmatter_default_size")
    @Config.Comment("Default spawning size for blocks/missiles/carts/etc")
    public float DEFAULT_SIZE = 1f;

    @Config.Name("redmatter_min_size")
    @Config.Comment("Smallest size of the redmatter before it dies")
    public float MIN_SIZE = 0.25f;

    @Config.Name("redmatter_scale_rendering")
    @Config.Comment("Multiplier of size to modify the render size")
    public float RENDER_SCALE = 0.05f;

    @Config.Name("redmatter_scale_kill_radius")
    @Config.Comment("Multiplier of size to create an instant kill center")
    public float KILL_SCALE = 0.08f;

    @Config.Name("redmatter_scale_gravity")
    @Config.Comment("Multiplier of size to generate a pull towards the center")
    public float GRAVITY_SCALE = 2f;

    @Config.Name("redmatter_starve_multiplier")
    @Config.Comment("Multiplier on how much mass is kept after each starve tick. 1.0 equals no starving and 0.0 means instant death of the redmatter.")
    @Config.RangeDouble(min = 0.0, max = 1.0)
    public float STARVE_SCALE = 0.99f;


    //@Config.Name("redmatter_movement")
    //@Config.Comment("Allows red matter explosions to be moved")
    public boolean REDMATTER_MOVEMENT = true; //TODO enable

    @Config.Name("redmatter_max_edits_per_tick")
    @Config.Comment("Max number of edits per tick for the redmatter")
    public int MAX_BLOCKS_EDITS_PER_TICK = 100;

    @Config.Name("redmatter_raytrace_per_tick")
    @Config.Comment("Number of raytraces per tick for the redmatter at it's default size")
    public int DEFAULT_BLOCK_RAYTRACE_PER_TICK = 500;

    @Config.Name("redmatter_flying_block_spawn_chance")
    @Config.Comment("Chance for a flying block to spawn when destroying blocks")
    public float CHANCE_FOR_FLYING_BLOCK = 0.8f;

    @Config.Name("redmatter_audio_enabled")
    @Config.Comment("Set to true to enable audio for the redmatter")
    public boolean ENABLE_AUDIO = true;

    @Config.Name("redmatter_spawn_flying_blocks")
    @Config.Comment("Set to true to enable flying blocks to spawn")
    public boolean SPAWN_FLYING_BLOCKS = true;

    @Config.Name("redmatter_colored_beams")
    @Config.Comment("Set to true to render redmatter with colored beams")
    public boolean RENDER_COLORED_BEAMS = true;
}
