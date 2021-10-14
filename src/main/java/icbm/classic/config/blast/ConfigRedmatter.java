package icbm.classic.config.blast;

import net.minecraftforge.common.config.Config;

/**
 * Created by Dark(DarkGuardsman, Robert) on 2/9/2020.
 */
public class ConfigRedmatter
{
    @Config.Name("redmatter_MAX_radius")
    @Config.Comment("Radius of the redmatter to destroy blocks. This scales with redmatter size as it consumes blocks or fails to consume blocks it will change size.")
    public int MAX_RADIUS = 70;

    @Config.Name("redmatter_scale_rendering")
    @Config.Comment("Multiplier of size to modify the render size")
    public float RENDER_SCALE = 0.1f;

    @Config.Name("redmatter_scale_kill_radius")
    @Config.Comment("Multiplier of size to create an instant kill center")
    public float KILL_SCALE = 0.08f;

    @Config.Name("redmatter_scale_gravity")
    @Config.Comment("Multiplier of size to generate a pull towards the center")
    public float GRAVITY_SCALE = 2f;


    @Config.Name("redmatter_movement")
    @Config.Comment("Allows red matter explosions to be moved")
    public boolean REDMATTER_MOVEMENT = true;

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
