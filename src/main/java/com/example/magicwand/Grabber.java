package com.example.magicwand;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Логика «Захвата»:
 *  Shift + ПКМ по блоку или существу: взять;
 *  ПКМ: поставить;
 *  то, что несёшь, висит перед игроком в 3D и следует за взглядом.
 *
 * Блок показывается настоящей сущностью block_display (рисует игра, поэтому он всегда в 3D),
 * существо просто «держится» перед игроком без ИИ и гравитации.
 */
public class Grabber {

    private static final double REACH = 6.0;
    private static final float MAX_WIDTH = 1.6F;    // слишком большие существа не берём
    private static final float MAX_HEIGHT = 2.2F;

    // Метки сущностей: по ним можно найти «потерянные» объекты командой
    private static final String TAG_DISPLAY = "mw_carry";

    // игрок -> сущность block_display / игрок -> несомое существо
    private static final Map<UUID, UUID> DISPLAYS = new HashMap<>();
    private static final Map<UUID, UUID> ENTITIES = new HashMap<>();

    public static boolean isCarrying(ItemStack stack) {
        return stack.has(GrabberItems.CARRIED_BLOCK) || stack.has(GrabberItems.CARRIED_ENTITY);
    }

    public static void init() {
        UseBlockCallback.EVENT.register(Grabber::onUseBlock);
        UseEntityCallback.EVENT.register(Grabber::onUseEntity);
        ServerTickEvents.END_SERVER_TICK.register(Grabber::tick);

        // Защищаем переносимых мобов от урона, пока на них стоит наша метка.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
                (entity, source, amount) -> !ENTITIES.containsValue(entity.getUUID())
        );

        // Игрок вышел: отпускаем существо и убираем «висящий» блок (сам блок остаётся в предмете)
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> clearAll(handler.getPlayer()));
    }

    // ------------------------------------------------------------------ события игрока

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getMainHandItem();
        if (!stack.is(GrabberItems.GRABBER) || !player.mayBuild()) {
            return InteractionResult.PASS;
        }

        // Руки заняты: ПКМ ставит, Shift + ПКМ напоминает, что сначала нужно поставить
        if (isCarrying(stack)) {
            if (player.isShiftKeyDown()) {
                if (player instanceof ServerPlayer sp) {
                    sp.sendOverlayMessage(Component.literal("Руки заняты: поставьте то, что несёте (ПКМ)"));
                }
                return InteractionResult.SUCCESS;
            }
            if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer sp) {
                place(sp, serverLevel, stack, hit);
            }
            return InteractionResult.SUCCESS;
        }

        // Shift + ПКМ по блоку: взять
        if (player.isShiftKeyDown()) {
            BlockPos pos = hit.getBlockPos();
            BlockState state = level.getBlockState(pos);

            if (!canPickBlock(level, pos, state)) {
                return InteractionResult.PASS;
            }
            if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer sp) {
                pickBlock(sp, serverLevel, stack, pos, state);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    // Shift + ПКМ по существу: взять (срабатывает раньше обычного взаимодействия: торговли, седла и т.д.)
    private static InteractionResult onUseEntity(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getMainHandItem();
        if (!stack.is(GrabberItems.GRABBER) || !player.isShiftKeyDown() || !player.mayBuild()) {
            return InteractionResult.PASS;
        }

        if (isCarrying(stack)) {
            if (player instanceof ServerPlayer sp) {
                sp.sendOverlayMessage(Component.literal("Руки заняты: поставьте то, что несёте (ПКМ)"));
            }
            return InteractionResult.SUCCESS;
        }

        if (!canPickEntity(entity)) {
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer sp) {
            pickEntity(sp, serverLevel, stack, entity);
        }
        return InteractionResult.SUCCESS;
    }

    // ------------------------------------------------------------------ взять

    private static boolean canPickBlock(Level level, BlockPos pos, BlockState state) {
        // Разрешаем блоки с BlockEntity (сундуки, печи, бочки и т.п.).
        // Жидкости и неразрушаемые блоки по-прежнему нельзя поднять.
        return !state.isAir()
                && state.getDestroySpeed(level, pos) >= 0
                && state.getFluidState().isEmpty();
    }

    private static void pickBlock(ServerPlayer player, ServerLevel level, ItemStack stack, BlockPos pos, BlockState state) {
        stack.set(GrabberItems.CARRIED_BLOCK, state);

        // Сохраняем полные данные BlockEntity в стандартный компонент предмета.
        // Это включает инвентарь сундука/бочки, топливо и прогресс печи,
        // пользовательское имя и прочие данные конкретного блока.
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            CompoundTag blockEntityTag = blockEntity.saveWithFullMetadata(level.registryAccess());
            stack.set(
                    DataComponents.BLOCK_ENTITY_DATA,
                    TypedEntityData.of(blockEntity.getType(), blockEntityTag)
            );
        } else {
            stack.remove(DataComponents.BLOCK_ENTITY_DATA);
        }

        level.removeBlock(pos, false);

        level.playSound(null, pos, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.PLAYERS, 1.0F, 1.4F);
        level.sendParticles(ParticleTypes.PORTAL,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.2);

        player.sendOverlayMessage(Component.literal("Взят блок: ").append(state.getBlock().getName()));
    }

    private static boolean canPickEntity(Entity entity) {
        return entity instanceof Mob
                && entity.isAlive()
                && entity.getBbWidth() <= MAX_WIDTH
                && entity.getBbHeight() <= MAX_HEIGHT
                && !ENTITIES.containsValue(entity.getUUID());
    }

    private static void pickEntity(ServerPlayer player, ServerLevel level, ItemStack stack, Entity entity) {
        entity.setNoGravity(true);
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }

        ENTITIES.put(player.getUUID(), entity.getUUID());
        stack.set(GrabberItems.CARRIED_ENTITY, BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());

        level.playSound(null, player.blockPosition(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.PLAYERS, 1.0F, 1.4F);
        level.sendParticles(ParticleTypes.PORTAL,
                entity.getX(), entity.getY() + 0.5, entity.getZ(), 20, 0.3, 0.3, 0.3, 0.2);

        player.sendOverlayMessage(Component.literal("Взято: ").append(entity.getName()));
    }

    // ------------------------------------------------------------------ поставить

    // ПКМ в воздух: считаем, куда смотрит игрок
    public static void placeFromRay(ServerPlayer player, ServerLevel level, ItemStack stack) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(REACH));

        BlockHitResult hit = level.clip(new ClipContext(
                start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        place(player, level, stack, hit);
    }

    private static void place(ServerPlayer player, ServerLevel level, ItemStack stack, BlockHitResult hit) {
        BlockState carriedBlock = stack.get(GrabberItems.CARRIED_BLOCK);

        // --- блок
        if (carriedBlock != null) {
            if (hit.getType() == HitResult.Type.MISS) {
                player.sendOverlayMessage(Component.literal("Некуда ставить: наведитесь на блок"));
                return;
            }

            BlockPos target = hit.getBlockPos().relative(hit.getDirection());
            BlockState existing = level.getBlockState(target);

            if (!existing.canBeReplaced() || !level.isUnobstructed(carriedBlock, target, CollisionContext.empty())) {
                player.sendOverlayMessage(Component.literal("Здесь нельзя поставить"));
                return;
            }

            level.setBlock(target, carriedBlock, 3);

            // Восстанавливаем BlockEntity только после установки правильного BlockState.
            TypedEntityData<BlockEntityType<?>> carriedData =
                    stack.get(DataComponents.BLOCK_ENTITY_DATA);
            if (carriedData != null) {
                BlockEntity restored = level.getBlockEntity(target);
                if (restored != null && restored.getType() == carriedData.type()) {
                    carriedData.loadInto(restored, level.registryAccess());
                    restored.setChanged();
                    level.sendBlockUpdated(target, carriedBlock, carriedBlock, 3);
                }
            }

            stack.remove(GrabberItems.CARRIED_BLOCK);
            stack.remove(DataComponents.BLOCK_ENTITY_DATA);

            level.playSound(null, target, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6F, 1.6F);
            level.sendParticles(ParticleTypes.PORTAL,
                    target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.2);
            return;
        }

        // --- существо
        if (stack.has(GrabberItems.CARRIED_ENTITY)) {
            Vec3 at = hit.getType() == HitResult.Type.MISS
                    ? player.getEyePosition().add(player.getLookAngle().scale(2.0))
                    : Vec3.atBottomCenterOf(hit.getBlockPos().relative(hit.getDirection()));

            placeEntity(player, level, stack, at);
        }
    }

    private static void placeEntity(ServerPlayer player, ServerLevel level, ItemStack stack, Vec3 at) {
        Entity entity = lookup(level, ENTITIES.get(player.getUUID()));

        if (entity != null) {
            entity.snapTo(at.x, at.y, at.z, player.getYRot() + 180.0F, 0.0F);
            entity.setDeltaMovement(Vec3.ZERO);
            freeEntity(entity);

            level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6F, 1.6F);
            level.sendParticles(ParticleTypes.PORTAL, at.x, at.y + 0.5, at.z, 20, 0.3, 0.3, 0.3, 0.2);
        }

        ENTITIES.remove(player.getUUID());
        stack.remove(GrabberItems.CARRIED_ENTITY);
    }

    private static void freeEntity(Entity entity) {
        entity.setNoGravity(false);
        if (entity instanceof Mob mob) {
            mob.setNoAi(false);
        }
    }

    // ------------------------------------------------------------------ каждый тик: несомое следует за игроком

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            follow(player);
        }
    }

    private static ItemStack findCarrier(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(GrabberItems.GRABBER) && isCarrying(stack)) {
                return stack;
            }
        }
        return null;
    }

    private static Entity lookup(ServerLevel level, UUID id) {
        return id == null ? null : level.getEntity(id);
    }

    // Точка перед игроком, где «висит» блок (учитывает наклон взгляда)
    private static Vec3 blockPoint(Player player) {
        Vec3 look = player.getLookAngle();
        return player.getEyePosition().add(look.x * 1.15, look.y * 1.15 - 0.45, look.z * 1.15);
    }

    // Точка перед игроком, где стоят ноги существа (только по горизонтали)
    private static Vec3 entityPoint(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z).normalize();
        Vec3 eye = player.getEyePosition();
        return new Vec3(eye.x + flat.x * 1.6, eye.y - 1.3, eye.z + flat.z * 1.6);
    }

    private static void follow(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        UUID pid = player.getUUID();
        ItemStack carrier = findCarrier(player);

        // ----- блок
        BlockState block = carrier == null ? null : carrier.get(GrabberItems.CARRIED_BLOCK);
        Entity display = lookup(level, DISPLAYS.get(pid));

        if (block == null || !player.isAlive()) {
            if (display != null) {
                display.discard();
            }
            DISPLAYS.remove(pid);
        } else {
            if (display == null || display.isRemoved()) {
                display = spawnDisplay(player, level, block);
            }
            if (display != null) {
                Vec3 t = blockPoint(player);
                display.snapTo(t.x, t.y, t.z, player.getYRot(), 0.0F);
            }
        }

        // ----- существо
        String marker = carrier == null ? null : carrier.get(GrabberItems.CARRIED_ENTITY);
        UUID entityId = ENTITIES.get(pid);
        Entity carried = lookup(level, entityId);

        if (entityId != null) {
            boolean keep = marker != null && player.isAlive() && carried != null && !carried.isRemoved();

            if (!keep) {
                if (carried != null) {
                    freeEntity(carried);
                }
                ENTITIES.remove(pid);
                if (carrier != null && marker != null) {
                    carrier.remove(GrabberItems.CARRIED_ENTITY);
                }
            } else {
                Vec3 t = entityPoint(player);
                carried.snapTo(t.x, t.y, t.z, player.getYRot() + 180.0F, 0.0F);
                carried.setDeltaMovement(Vec3.ZERO);
            }
        } else if (marker != null) {
            // устаревшая метка (например, после перезапуска сервера)
            carrier.remove(GrabberItems.CARRIED_ENTITY);
        }
    }

    // Создаём настоящую сущность block_display командой summon (так не зависим от внутренних методов игры)
    private static Entity spawnDisplay(ServerPlayer player, ServerLevel level, BlockState state) {
        Vec3 t = blockPoint(player);

        String command = String.format(Locale.ROOT,
                "summon minecraft:block_display %.3f %.3f %.3f "
                        + "{block_state:%s,teleport_duration:2,Tags:[\"%s\"],"
                        + "transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],"
                        + "translation:[-0.3f,-0.3f,-0.3f],scale:[0.6f,0.6f,0.6f]}}",
                t.x, t.y, t.z, NbtUtils.writeBlockState(state), TAG_DISPLAY);

        MinecraftServer server = level.getServer();
        CommandSourceStack source = server.createCommandSourceStack()
                .withLevel(level)
                .withSuppressedOutput();
        server.getCommands().performPrefixedCommand(source, command);

        List<Entity> found = level.getEntities(
                (Entity) null,
                new AABB(t, t).inflate(0.5),
                e -> Identifier.fromNamespaceAndPath("minecraft", "block_display")
                        .equals(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()))
        );
        if (found.isEmpty()) {
            return null;
        }

        Entity display = found.get(0);
        DISPLAYS.put(player.getUUID(), display.getUUID());
        return display;
    }

    private static void clearAll(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        UUID pid = player.getUUID();

        Entity display = lookup(level, DISPLAYS.remove(pid));
        if (display != null) {
            display.discard();
        }

        Entity carried = lookup(level, ENTITIES.remove(pid));
        if (carried != null) {
            freeEntity(carried);
        }
    }
}