# ICBM-Classic: 1.12.2 Forge → 1.21.1 NeoForge porting guide

This file is the single source of truth for conventions used while porting. Every ported file
must follow it so that classes written by different people still fit together.

## Targets

- Minecraft 1.21.1, NeoForge 21.1.x, Java 21, ModDevGradle 2, official Mojang mappings + Parchment.
- Lombok is still used (1.18.36). Keep existing Lombok annotations.
- Mod id stays `icbmclassic`. Root package stays `icbm.classic`.

## Golden rules

1. **Keep class names, package names, and ICBM-owned method names.** Only swap Minecraft/Forge types
   and calls. Other people port the classes you call, and they keep the same names, so
   cross-references keep working. If you must change an ICBM-owned public signature beyond a
   type swap from the table below, note it in `PORT_NOTES.md` under "Signature changes".
2. **Preserve behaviour.** Same numbers, same config keys, same timings, same lang keys. When
   1.21 cannot do exactly the same thing, do the closest equivalent and add a `// PORT:` comment
   explaining the difference.
3. **No silent deletions.** If something cannot be ported now, leave the code in place commented or
   stubbed with `// PORT-TODO: <reason>` and list it in `PORT_NOTES.md` under "Open items".
4. Remove `@SideOnly`; client-only classes live where they are but must never be class-loaded on a
   dedicated server (only reference them from `ClientProxy`/client event handlers).
5. Delete `datafix/` (1.12 data fixers do not apply to 1.21 worlds).

## Type and API mapping (MCP 1.12 → Mojang 1.21.1)

| 1.12 | 1.21.1 |
|---|---|
| `World` / `WorldServer` / `IBlockAccess` | `Level` / `ServerLevel` / `BlockGetter` (or `LevelReader`) |
| `world.isRemote` | `level.isClientSide()` (field `level.isClientSide` also exists) |
| `world.provider.getDimension()` (int) | `level.dimension()` (`ResourceKey<Level>`) — see Dimensions |
| `TileEntity` | `BlockEntity` (constructor `(BlockEntityType<?>, BlockPos, BlockState)`) |
| `ITickable#update` on tiles | `BlockEntityTicker` returned from `Block#getTicker`; keep the `update()` method name and call it from the ticker |
| `IBlockState` | `BlockState` |
| `Block#onBlockActivated` | `useWithoutItem` / `useItemOn` |
| `BlockContainer` | `BaseEntityBlock` (needs `codec()`) — return `RenderShape.MODEL` from `getRenderShape` |
| `EnumFacing` | `Direction` |
| `EnumHand` | `InteractionHand` |
| `EnumActionResult` / `ActionResult<T>` | `InteractionResult` / `InteractionResultHolder<T>` (block use: `ItemInteractionResult`) |
| `EntityPlayer` / `EntityPlayerMP` | `Player` / `ServerPlayer` |
| `EntityLivingBase` | `LivingEntity` |
| `EntityItem` | `ItemEntity` |
| `Vec3d` | `Vec3` |
| `AxisAlignedBB` | `AABB` |
| `RayTraceResult` | `HitResult` / `BlockHitResult` / `EntityHitResult` |
| `MathHelper` | `Mth` |
| `NBTTagCompound` / `NBTTagList` / `NBTBase` | `CompoundTag` / `ListTag` / `Tag` |
| `nbt.setTag/setInteger/getInteger/hasKey/getCompoundTag/getTagList` | `put/putInt/getInt/contains/getCompound/getList` |
| `ResourceLocation(ns, path)` constructor | `ResourceLocation.fromNamespaceAndPath(ns, path)`; `new ResourceLocation(str)` → `ResourceLocation.parse(str)` |
| `TextComponentString` / `TextComponentTranslation` | `Component.literal` / `Component.translatable` |
| `player.sendMessage(c)` | `player.sendSystemMessage(c)`; action bar: `player.displayClientMessage(c, true)` |
| `I18n.format` (client) / `LanguageUtility.getLocal` | `I18n.get` client-side; prefer `Component.translatable` |
| `ItemStack#getItemDamage` meta | gone: use data components (see Items) |
| `stack.getTagCompound()` | `StackNbt.get(stack)` / `StackNbt.update(stack, tag -> ...)` (our helper, backed by `DataComponents.CUSTOM_DATA`) |
| `Item#onItemRightClick` | `Item#use` |
| `Item#onItemUse` | `Item#useOn(UseOnContext)` |
| `Item#addInformation` | `appendHoverText(ItemStack, Item.TooltipContext, List<Component>, TooltipFlag)` |
| `Item#getSubItems` | creative tab population in `ICBMCreativeTab` (`displayItems`) |
| `ItemBlock` | `BlockItem` |
| `Entity#writeEntityToNBT/readEntityFromNBT` | `addAdditionalSaveData/readAdditionalSaveData` |
| `Entity#entityInit` + `DataParameter` | `defineSynchedData(SynchedEntityData.Builder)` + `EntityDataAccessor` |
| `Entity#onUpdate` | `tick()` |
| `IEntityAdditionalSpawnData` | `IEntityWithComplexSpawn` (`writeSpawnData/readSpawnData(RegistryFriendlyByteBuf)`) |
| `entity.posX/posY/posZ` | `getX()/getY()/getZ()`; `setPos(...)` |
| `entity.motionX..` | `getDeltaMovement()` / `setDeltaMovement(...)` |
| `entity.rotationYaw/Pitch` | `getYRot()/setYRot()`, `getXRot()/setXRot()`; prev: `yRotO/xRotO` |
| `entity.world` | `entity.level()` |
| `entity.setDead()` / `isDead` | `discard()` / `isRemoved()` |
| `world.spawnEntity(e)` | `level.addFreshEntity(e)` |
| `world.getTileEntity(pos)` | `level.getBlockEntity(pos)` |
| `world.setBlockState(pos, s, flags)` | `level.setBlock(pos, s, flags)` |
| `world.setBlockToAir(pos)` | `level.removeBlock(pos, false)` |
| `world.isBlockLoaded(pos)` | `level.isLoaded(pos)` / `level.hasChunkAt(pos)` |
| `world.getEntitiesWithinAABB(cls, box)` | `level.getEntitiesOfClass(cls, box)` |
| `world.getHeight()` / `255` constants | `level.getMinBuildHeight()` / `level.getMaxBuildHeight()` |
| `world.spawnParticle(EnumParticleTypes.X, ...)` | `level.addParticle(ParticleTypes.X, ...)` (server: `serverLevel.sendParticles`) |
| `world.playSound(...)` | `level.playSound(...)` with `SoundSource` |
| `Material` | removed; use `BlockBehaviour.Properties` (`mapColor`, `strength`, `sound`, `requiresCorrectToolForDrops`) |
| `Potion` / `PotionEffect` | `MobEffect` / `MobEffectInstance`, held as `Holder<MobEffect>` |
| `DamageSource` constants | `level.damageSources().xxx()`; custom types are data-driven `DamageType` keys (see `ICBMDamageTypes`) |
| `Container` / `Slot` / `GuiContainer` / `GuiScreen` / `GuiButton` | `AbstractContainerMenu` / `Slot` / `AbstractContainerScreen` / `Screen` / `Button` |
| `FontRenderer` | `Font` (draw via `GuiGraphics`) |
| `TileEntitySpecialRenderer` | `BlockEntityRenderer` (register in `ClientReg`) |
| `Render<T>` / `ModelBase` | `EntityRenderer<T>` / `Model` + `ModelPart` with `LayerDefinition` |
| `Tessellator` / `BufferBuilder` / `GlStateManager` | `PoseStack` + `MultiBufferSource`/`VertexConsumer` + `RenderType`; `RenderSystem` only in GUI |
| `OreDictionary` | item tags (`c:` convention tags) |
| `IRecipe` | `Recipe<CraftingInput>` / `CustomRecipe` + `RecipeSerializer` |
| `ForgeChunkManager` | NeoForge `TicketController` |
| `@Config` (Forge) | our shim `icbm.classic.lib.config.Config` (same annotation names; see Config) |
| `MinecraftForge.EVENT_BUS` | `NeoForge.EVENT_BUS` |
| `@Mod.EventBusSubscriber` | `@EventBusSubscriber(modid = ICBMConstants.DOMAIN[, bus = Bus.MOD])` |
| `FMLCommonHandler.instance().getMinecraftServerInstance()` | `ServerLifecycleHooks.getCurrentServer()` |
| `Minecraft.getMinecraft()` | `Minecraft.getInstance()` |
| `EnumParticleTypes` | `ParticleTypes` |
| `SoundCategory` | `SoundSource` |
| `EnumDyeColor` | `DyeColor` |
| `Keyboard.isKeyDown(...)` | `Screen.hasShiftDown()` |

## Dimensions

Integer dimension ids are gone. Everywhere an `int dimId` / `worldId` was stored or sent, use
`ResourceKey<Level>` in memory, `ResourceLocation` string in NBT (`key.location().toString()`), and
`buf.writeResourceKey(key)` / `buf.readResourceKey(Registries.DIMENSION)` in packets. Keep the
ICBM method names (e.g. `IGPSData#getWorldId()` now returns `ResourceKey<Level>`).
Look up a level with `server.getLevel(key)`.

## Registries (foundation, already written)

Registration uses `DeferredRegister`. Holders live in the existing reg classes; access with `.get()`:

- `icbm.classic.content.reg.BlockReg` — blocks (`DeferredBlock<...>` fields keep the old field names,
  e.g. `BlockReg.blockLaunchBase.get()`), plus block entity types: `BlockReg.TILE_LAUNCHER_BASE` etc.
- `icbm.classic.content.reg.ItemReg` — items, same field names (`ItemReg.itemGrenade.get()`).
- `icbm.classic.content.reg.EntityReg` — entity types: `EntityReg.MISSILE_EXPLOSIVE.get()` etc.
- `icbm.classic.content.reg.MenuReg` — menu types.
- `icbm.classic.content.reg.ComponentReg` — data components.
- `icbm.classic.content.reg.RecipeReg` — recipe serializers for the custom/dynamic recipes.
- Mob effects: `ICBMClassic.EFFECTS`; `ICBMClassic.chemicalPotion` / `ICBMClassic.contagiousPotion` are
  `DeferredHolder<MobEffect, ContagiousPoison>` (already a `Holder<MobEffect>`, pass directly to `MobEffectInstance`).
  `ContagiousPoison(String name, int id, boolean isContagious)` extends `MobEffect`.
- `icbm.classic.content.reg.AttachmentReg` — data attachments (`SPACE_CHICKEN`).
- Creative tab: `ICBMCreativeTab.TAB`. Items do **not** set a tab; every ICBM item is listed automatically.
  Items with variants implement `icbm.classic.prefab.item.ISubItemProvider#getSubItems(NonNullList<ItemStack>)`
  (replaces `Item#getSubItems`); JEI uses the same list.
- `icbm.classic.client.ICBMSounds` — sounds (enum kept; `getSound()` returns `SoundEvent`).

Registry names keep the 1.12 names lowercased (1.21 forbids upper case): `radarGun` → `radargun`,
`launcherbase`, `emptower`, `explosives`, `bombcart`, `poisonpowder`, `sulfurdust` ... The exact names
are the strings in `BlockReg`/`ItemReg`/`EntityReg`; never invent new ones. Multi-meta items/blocks are flattened:
`ingot:steel` → `steel_ingot` (copper ingot replaced by vanilla `minecraft:copper_ingot`),
`clump` → `steel_clump`, `plate:*` → `steel_plate`/`iron_plate`, `circuit:*` → `basic_circuit`/
`advanced_circuit`/`elite_circuit`, `wire:*` → `copper_wire`/`gold_wire`, `spikes:*` → `spikes`/
`spikes_poison`/`spikes_fire`, `concrete:*` → `concrete`/`concrete_compact`/`concrete_reinforced`,
`radioactive:*` → `radioactive_dirt`/`radioactive_stone`, `emptower` item meta 1 → `emptower_extender`.
Flattened blocks take their variant in the constructor (`new BlockSpikes(EnumSpikes.POISON)`,
`new BlockConcrete(EnumType.COMPACT)`, `new BlockRadioactive(EnumType.STONE)`) and keep the enum for logic;
the blockstate `variant` property is removed.

### Translation keys stay verbatim

Lang keys are not renamed. `ItemBase#setName(name)` (and the block equivalent in `BlockICBM`) stores the
1.12 unlocalized key and `getDescriptionId()` returns it, e.g. `item.icbmclassic:radarGun`,
`tile.icbmclassic:launcherbase`. Variant items return their variant key (e.g.
`item.icbmclassic:circuit.basic`) by overriding `getDescriptionId()`. Any 1.12 code doing
`getUnlocalizedName() + ".info"` keeps working with `getDescriptionId() + ".info"`. The converted
lang JSON contains both `<key>.name` (as in 1.12) and `<key>` (what 1.21 looks up).

## Constructor conventions (already relied on by the registries)

| Kind | Constructor |
|---|---|
| Block | no-arg, or the variant enum for flattened blocks (see above). Build `BlockBehaviour.Properties` inside. |
| Block entity | `TileX(BlockPos pos, BlockState state)` calling `super(BlockReg.TILE_X.get(), pos, state)` |
| Entity | `EntityX(EntityType<? extends EntityX> type, Level level)` **and** convenience `EntityX(Level level)` using `EntityReg.X.get()` |
| Item | no-arg (`ItemRocketLauncher(boolean ballistic)`, `ItemCrafting(String group, String type)`, `ItemBlockAbstract(Block)`, `ItemBlockExplosive(Block)`, `ItemBlockEmpTower(Block, boolean extender)`) |
| Menu | `ContainerX(int windowId, Player player, TileX tile)`; `MenuReg` resolves the tile client side from a `BlockPos` |
| Recipes | `RecipeCargoData(CraftingBookCategory, Supplier<ItemStack> output, Supplier<IProjectileData> factory, Supplier<RecipeSerializer<?>> serializer)`, `RecipeCargoDataDecraft(CraftingBookCategory, DeferredItem<?> item, Supplier<RecipeSerializer<?>> serializer)`, `RecipeCluster(CraftingBookCategory, Supplier<ItemStack>)`, `RecipeHeldItemMissile(CraftingBookCategory, Supplier<ItemStack>)`, `RecipeHeldItemMissileDecraft(CraftingBookCategory, DeferredItem<?>)` — all extend `CustomRecipe` |

Tickers: blocks with ticking tiles return `createTickerHelper(type, BlockReg.TILE_X.get(), (l, p, s, t) -> t.update())`
from `getTicker`; keep the tile's `update()` method.

## Mod entry (already written)

`ICBMClassic` constructor `(IEventBus, ModContainer)` registers configs, then registries, then listeners.
Contracts other code must satisfy:
- `TileEMPTower/TileRadarStation/TileLauncherBase/TileLauncherScreen/TileCruiseLauncher.register()` only
  register their packet codexes now (block entity types live in `BlockReg`). Called from the constructor.
- `ICBMCommands.register(CommandDispatcher<CommandSourceStack>)` registers `/icbm` with Brigadier.
- Explosive/flight/target registries and the `*Handler.setup()` calls run in `FMLCommonSetupEvent`
  (common configs are loaded by then). Nothing may read `ICBMClassicAPI.EXPLOSIVE_REGISTRY` during
  registry construction; read it lazily (creative tab, tooltips, runtime).
- `CommonProxy`: `preInit(IEventBus)`, `init()`, `isShiftHeld()`, `spawnSmoke/spawnAirParticle/
  spawnExplosionParticles/spawnMissileSmoke/spawnPadSmoke`. `client.ClientProxy` extends it with a public
  no-arg constructor (created reflectively) and registers client listeners on the mod bus in `preInit`.
- The 1.12 `IGuiHandler` is gone; GUI ids are replaced by menus.

**Explosive variants are NOT flattened.** Grenades, missiles, bomb carts, bomblets and the explosive
block stay one item each; the explosive type lives in data component `ComponentReg.EXPLOSIVE`
(`ResourceLocation` of the explosive). The `IExplosiveData` registry (`ExplosiveRegistry`) is kept
as-is. `ICBMClassicHelpers.getExplosive(stack)` etc. still work. Helpers:
`ExplosiveStacks.create(item, explosiveData)` and `ExplosiveStacks.getExplosive(stack)` in
`icbm.classic.lib.explosive` (written with the core library).

## Capabilities (foundation, already written)

NeoForge capabilities are typed per container. In `ICBMClassicAPI`:

| 1.12 | 1.21 |
|---|---|
| `EXPLOSIVE_CAPABILITY` | `EXPLOSIVE_ITEM_CAPABILITY`, `EXPLOSIVE_ENTITY_CAPABILITY`, `EXPLOSIVE_BLOCK_CAPABILITY` |
| `EMP_CAPABILITY` | `EMP_ITEM_CAPABILITY`, `EMP_ENTITY_CAPABILITY`, `EMP_BLOCK_CAPABILITY` |
| `MISSILE_CAPABILITY` | `MISSILE_CAPABILITY` (entity) |
| `MISSILE_HOLDER_CAPABILITY` / `MISSILE_LAUNCHER_CAPABILITY` | same names (block, `@Nullable Direction` context) |
| `RADIO_CAPABILITY` | `RADIO_CAPABILITY` (block) + `RADIO_ITEM_CAPABILITY` |
| `GPS_CAPABILITY` / `PROJECTILE_STACK_CAPABILITY` / `MISSILE_STACK_CAPABILITY` | same names (item) |
| `BLAST_CAPABILITY` / `BLAST_VELOCITY_CAPABILITY` | same names (entity) |
| `CapabilityEnergy.ENERGY` | `Capabilities.EnergyStorage.BLOCK/ITEM/ENTITY` |
| `CapabilityItemHandler.ITEM_HANDLER_CAPABILITY` | `Capabilities.ItemHandler.BLOCK/ENTITY/ITEM` |

To keep the 1.12 shape, ICBM classes expose capabilities through small interfaces in
`icbm.classic.lib.capability.holder` that `CapabilityReg` wires up for every ICBM block entity,
entity and item:

```java
// BlockEntity
public class TileFoo extends TileMachine implements ICapabilityHolderTile {
    @Override
    public <T> @Nullable T getCapability(BlockCapability<T, @Nullable Direction> cap, @Nullable Direction side) {
        if (cap == ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY) return (T) missileHolder;
        return null;
    }
}
// Entity: ICapabilityHolderEntity#getCapability(EntityCapability<T, Void>)
// Item:   ICapabilityHolderItem#getCapability(ItemCapability<T, Void>, ItemStack) — replaces initCapabilities
```

`x.hasCapability(cap, side)` becomes `x.getCapability(cap, side) != null` (vanilla API for block caps:
`level.getCapability(cap, pos, side)`; entity: `entity.getCapability(cap)`; stack: `stack.getCapability(cap)`).
Remember to invalidate (`level.invalidateCapabilities(pos)`) when a block entity's cap changes.

Item capabilities are created fresh per lookup and are not stored. Any item cap that had state
(`ICapabilitySerializable`) must read and write that state through the stack each call
(`StackNbt` helper, or a dedicated data component). Vanilla-entity attachments (EMP on creepers,
space chicken) use data attachments (`AttachmentReg`).

## Networking (foundation, already written)

`ICBMClassic.packetHandler` (`PacketManager`) keeps its API (`sendToPlayer`, `sendToAllAround`,
`sendToAllInDimension`, `sendToServer`, `sendToAll`). Internally all ICBM packets ride one
`CustomPacketPayload`. `IPacket` changed to:

```java
void encodeInto(RegistryFriendlyByteBuf buffer);
void decodeInto(RegistryFriendlyByteBuf buffer);   // must consume all bytes
default void handleClientSide(Minecraft mc, Player player)
default void handleServerSide(Player player)
```

Handlers are already scheduled on the main thread. `PacketCodex` builders are unchanged except the
encoder/decoder lambdas take `RegistryFriendlyByteBuf`. `ByteBufUtils.writeUTF8String/readUTF8String`
→ `buf.writeUtf/readUtf`; tags → `buf.writeNbt/readNbt`; stacks → `ItemStack.OPTIONAL_STREAM_CODEC`.
Block entity client sync: `getUpdatePacket()` → `ClientboundBlockEntityDataPacket.create(this)`, and
`getUpdateTag(HolderLookup.Provider)`.

## Saving

- Block entity: `saveAdditional(CompoundTag, HolderLookup.Provider)` / `loadAdditional(CompoundTag, HolderLookup.Provider)`.
- `NbtSaveHandler#save(obj, tag)` / `load(obj, tag)` keep their signatures. Nodes needing registries
  (item stacks, block states) use `RegistryHelper.access()` internally.
- `ItemStack` NBT: `stack.save(provider)` / `ItemStack.parseOptional(provider, tag)`.
- `NBTUtil.readBlockState/writeBlockState` → `NbtUtils.readBlockState(holderGetter, tag)` / `NbtUtils.writeBlockState`.

## Config

The Forge 1.12 annotation config is kept via a shim in `icbm.classic.lib.config`:
`@Config(modid, name)`, `@Config.Name`, `@Config.Comment`, `@Config.RangeInt`, `@Config.RangeDouble`,
`@Config.LangKey`, `@Config.RequiresMcRestart`, `@Config.Ignore`. `ConfigLoader` turns each annotated
class into a `ModConfigSpec` file (`config/icbmclassic/<name>.toml`) and copies values into the
static fields on load and reload. Only change imports in config classes:
`net.minecraftforge.common.config.Config` → `icbm.classic.lib.config.Config`.
`ConfigChangedEvent` → `ConfigLoader` calls `ConfigMain.onConfigReloaded()` after every (re)load once
common setup is done. `ConfigItems` is a STARTUP config (read during item registration); `ConfigClient` is CLIENT.

## GUIs

- `IGuiTile` stays but now extends `MenuProvider`-style: tiles implement `MenuProvider`
  (`createMenu(int, Inventory, Player)`, `getDisplayName()`), and open with
  `player.openMenu(tile, buf -> buf.writeBlockPos(pos))`.
- `ContainerBase` extends `AbstractContainerMenu`; each container gets a `MenuType` in `MenuReg`
  using `IMenuTypeExtension.create((id, inv, buf) -> new ContainerX(id, inv, buf.readBlockPos()))`.
- `GuiContainerBase` extends `AbstractContainerScreen`; screens registered in `ClientReg` via
  `RegisterMenuScreensEvent`. Drawing goes through `GuiGraphics`.

## Items/stacks

- Old metadata variants for explosives → `ComponentReg.EXPLOSIVE`.
- Old stack NBT → `StackNbt` helper (custom data) unless a dedicated component exists.
- Energy items use `ComponentReg.ENERGY` with NeoForge `ComponentEnergyStorage`.

## Recipes, tags, loot

JSON recipes live in `src/main/resources/data/icbmclassic/recipe/` (singular folder in 1.21).
Ore dictionary names → tags: `dustSulfur` → `c:dusts/sulfur`, `dustSaltpeter` → `c:dusts/saltpeter`,
`ingotSteel` → `c:ingots/steel`, `plateSteel` → `c:plates/steel`, `plateIron` → `c:plates/iron`,
`circuitBasic|Advanced|Elite` → `c:circuits/basic|advanced|elite`, `wireCopper|Gold` → `c:wires/copper|gold`.
Explosive-variant ingredients use `{"type": "neoforge:components", "items": "icbmclassic:...",
"components": {"icbmclassic:explosive": "icbmclassic:condensed"}}`.
Loot injection stays in code (`ICBMClassic#registerLoot`): `LootTableLoadEvent` adds an ICBM pool to
the vanilla tables, gated by `ConfigItems`. Smelting recipes and ore names become JSON recipes and tags.

## Client

- `ClientProxy` keeps the proxy pattern (`ICBMClassic.proxy`) for particle/smoke helpers.
- Model registration (`ModelLoader.setCustomModelResourceLocation`) is replaced by static JSON
  models. Explosive variants use item property `icbmclassic:explosive` (value = explosive registry
  index / 100) with model `overrides`.
- Entity renderers, BE renderers, layers, screens, item properties, colour handlers are all
  registered in `client/ClientReg.java` on the mod bus.
