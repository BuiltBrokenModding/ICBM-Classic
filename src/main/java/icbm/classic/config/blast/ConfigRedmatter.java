package icbm.classic.config.blast;

import net.minecraftforge.common.config.Config;

/**
 * Created by Dark(DarkGuardsman, Robert) on 2/9/2020.
 */
public class ConfigRedmatter
{
    @Config.Name("redmatter_default_radius")
    @Config.Comment("Radius of the redmatter to destroy blocks. This scales with redmatter size as it consumes blocks or fails to consume blocks it will change size.")
    public float NORMAL_RADIUS = 70;

    @Config.Name("redmatter_entity_destroy_radius")
    @Config.Comment("Radius to kill entities, this also controls render size of the ball in the center. Needs to be smaller than the full radius.")
    public float ENTITY_DESTROY_RADIUS = 6;

    @Config.Name("redmatter_runtime_limit_milliseconds")
    @Config.Comment("Max time to allow redmatter to break blocks before stopping. This helps prevent the redmatter from stalling out the runtime of the game.")
    public int MAX_RUNTIME_MS = 5;

    @Config.Name("redmatter_movement")
    @Config.Comment("Allows red matter explosions to be moved")
    public boolean REDMATTER_MOVEMENT = true;

    @Config.Name("redmatter_max_edits_per_tick")
    @Config.Comment("Max number of edits per tick for the redmatter")
    public int MAX_BLOCKS_EDITS_PER_TICK = 100;

    @Config.Name("redmatter_edits_per_tick")
    @Config.Comment("Number of edits per tick for the redmatter at it's default size")
    public int DEFAULT_BLOCK_EDITS_PER_TICK = 20;

    @Config.Name("redmatter_life_span_ticks")
    @Config.Comment("Number of ticks for the redmatter to exist before despawning")
    public int MAX_LIFESPAN = 36000; // 30 minutes

    @Config.Name("redmatter_flying_block_spawn_chance")
    @Config.Comment("Chance for a flying block to spawn when destroying blocks")
    public float CHANCE_FOR_FLYING_BLOCK = 0.8f;

    @Config.Name("redmatter_can_despawn")
    @Config.Comment("Set to true to allow the redmatter to dispawn")
    public boolean DO_DESPAWN = true;

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
