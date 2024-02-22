//so wie die flugbahn auf das target wird rot angezeigt, die wie du aimst wird blau angezeigt und wenn die
// beiden sich überschneiden wirds kp lila angezeigt
package meteordevelopment.meteorclient.systems.modules.own;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.render.Trajectories;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.ProjectileEntitySimulator;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AimAssist extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Items to display trajectories for.")
        .defaultValue(getDefaultItems())
        .filter(this::itemFilter)
        .build()
    );

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to target.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<Boolean> firedProjectiles = sgGeneral.add(new BoolSetting.Builder()
        .name("fired-projectiles")
        .description("Calculates trajectories for already fired projectiles.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> accurate = sgGeneral.add(new BoolSetting.Builder()
        .name("accurate")
        .description("Whether or not to calculate more accurate.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Integer> simulationSteps = sgGeneral.add(new IntSetting.Builder()
        .name("simulation-steps")
        .description("How many steps to simulate projectiles. Zero for no limit")
        .defaultValue(500)
        .sliderMax(5000)
        .build()
    );

    // Render

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color.")
        .defaultValue(new SettingColor(255, 150, 0, 35))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color.")
        .defaultValue(new SettingColor(255, 150, 0))
        .build()
    );

    private final ProjectileEntitySimulator simulator = new ProjectileEntitySimulator();

    private final Pool<Vector3d> vec3s = new Pool<>(Vector3d::new);
    private final List<Path> paths = new ArrayList<>();

    private static final double MULTISHOT_OFFSET = Math.toRadians(10); // accurate-ish offset of crossbow multishot in radians (10° degrees)

    public AimAssist() {
        super(Categories.Own, "Aim Assist", "Shows you the best angle to shoot with arrows / trajectories.");
    }

    private boolean itemFilter(Item item) {
        return item instanceof BowItem || item instanceof CrossbowItem || item instanceof FishingRodItem || item instanceof TridentItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderPearlItem || item instanceof ExperienceBottleItem || item instanceof ThrowablePotionItem;
    }

    private List<Item> getDefaultItems() {
        List<Item> items = new ArrayList<>();

        for (Item item : Registries.ITEM) {
            if (itemFilter(item)) items.add(item);
        }

        return items;
    }

    private Path getEmptyPath() {
        for (Path path : paths) {
            if (path.points.isEmpty()) return path;
        }

        Path path = new Path();
        paths.add(path);
        return path;
    }

    private void calculatePath(PlayerEntity player, double tickDelta) {
        // Clear paths
        for (Path path : paths) path.clear();

        // Get item
        ItemStack itemStack = player.getMainHandStack();
        if (itemStack == null) itemStack = player.getOffHandStack();
        if (itemStack == null) return;
        if (!items.get().contains(itemStack.getItem())) return;

        // Calculate paths
        if (!simulator.set(player, itemStack, 0, accurate.get(), tickDelta, true)) return;
        getEmptyPath().calculate();

        if (itemStack.getItem() instanceof CrossbowItem && EnchantmentHelper.getLevel(Enchantments.MULTISHOT, itemStack) > 0) {
            if (!simulator.set(player, itemStack, MULTISHOT_OFFSET, accurate.get(), tickDelta, true)) return; // left multishot arrow
            getEmptyPath().calculate();

            if (!simulator.set(player, itemStack, -MULTISHOT_OFFSET, accurate.get(), tickDelta, true)) return; // right multishot arrow
            getEmptyPath().calculate();
        }
    }

    private void calculateFiredPath(Entity entity, double tickDelta) {
        for (Path path : paths) path.clear();

        // Calculate paths
        if (!simulator.set(entity, accurate.get(), tickDelta)) return;
        getEmptyPath().calculate();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {

        // Handle fired Projectiles option
        if (firedProjectiles.get()) {
            for (Entity ent : mc.world.getEntities()) {
                if (ent instanceof ProjectileEntity) {
                    calculateFiredPath(ent, event.tickDelta);
                    for (Path path : paths) path.render(event);
                }
            }
        }

        // Calculate the path
        calculatePath(mc.player, event.tickDelta);
        for (Path path : paths) path.render(event);

        // Get entities from setting and create entity list
        List<LivingEntity> _entities = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) if (entities.get().contains(entity.getType()) && entity != mc.player) _entities.add((LivingEntity) entity);

        // Render for every entity
        for (LivingEntity entity : _entities) {
            // Sanity check
            if (entity == null) return;
            float speed = BowItem.getPullProgress(mc.player.getItemUseTime()) * 3;
            if (speed <= 0) return;

            // Draw a box around the determined entity
            double x = MathHelper.lerp(event.tickDelta, entity.lastRenderX, entity.getX()) - entity.getX();
            double y = MathHelper.lerp(event.tickDelta, entity.lastRenderY, entity.getY()) - entity.getY();
            double z = MathHelper.lerp(event.tickDelta, entity.lastRenderZ, entity.getZ()) - entity.getZ();
            Box box = entity.getBoundingBox();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, sideColor.get(), lineColor.get(), shapeMode.get(), 0);

            // Draw the movement predicting box
            Vec3d entityVelocity = entity.getVelocity();
            if (entity.isOnGround()) entityVelocity = entityVelocity.add(0, -entityVelocity.getY(), 0);
            float distancePlayerToTarget = mc.player.distanceTo(entity);
            float flightTime = distancePlayerToTarget / speed;
            Vec3d offsetVector = entityVelocity.multiply(flightTime);
            x += offsetVector.getX();
            y += offsetVector.getY();
            z += offsetVector.getZ();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }


    }

    private class Path {
        private final List<Vector3d> points = new ArrayList<>();

        private boolean hitQuad, hitQuadHorizontal;
        private double hitQuadX1, hitQuadY1, hitQuadZ1, hitQuadX2, hitQuadY2, hitQuadZ2;

        private Entity entity;

        public void clear() {
            for (Vector3d point : points) vec3s.free(point);
            points.clear();

            hitQuad = false;
            entity = null;
        }

        public void calculate() {
            addPoint();

            for (int i = 0; i < (simulationSteps.get() > 0 ? simulationSteps.get() : Integer.MAX_VALUE); i++) {
                HitResult result = simulator.tick();

                if (result != null) {
                    processHitResult(result);
                    break;
                }

                addPoint();
            }

        }

        private void addPoint() {
            points.add(vec3s.get().set(simulator.pos));
        }

        private void processHitResult(HitResult result) {
            if (result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult r = (BlockHitResult) result;

                hitQuad = true;
                hitQuadX1 = r.getPos().x;
                hitQuadY1 = r.getPos().y;
                hitQuadZ1 = r.getPos().z;
                hitQuadX2 = r.getPos().x;
                hitQuadY2 = r.getPos().y;
                hitQuadZ2 = r.getPos().z;

                if (r.getSide() == Direction.UP || r.getSide() == Direction.DOWN) {
                    hitQuadHorizontal = true;
                    hitQuadX1 -= 0.25;
                    hitQuadZ1 -= 0.25;
                    hitQuadX2 += 0.25;
                    hitQuadZ2 += 0.25;
                }
                else if (r.getSide() == Direction.NORTH || r.getSide() == Direction.SOUTH) {
                    hitQuadHorizontal = false;
                    hitQuadX1 -= 0.25;
                    hitQuadY1 -= 0.25;
                    hitQuadX2 += 0.25;
                    hitQuadY2 += 0.25;
                }
                else {
                    hitQuadHorizontal = false;
                    hitQuadZ1 -= 0.25;
                    hitQuadY1 -= 0.25;
                    hitQuadZ2 += 0.25;
                    hitQuadY2 += 0.25;
                }

                points.add(Utils.set(vec3s.get(), result.getPos()));
            }
            else if (result.getType() == HitResult.Type.ENTITY) {
                entity = ((EntityHitResult) result).getEntity();

                points.add(Utils.set(vec3s.get(), result.getPos()).add(0, entity.getHeight() / 2, 0));
            }
        }

        public void render(Render3DEvent event) {
            // Render path
            Vector3d lastPoint = null;

            for (Vector3d point : points) {
                if (lastPoint != null) event.renderer.line(lastPoint.x, lastPoint.y, lastPoint.z, point.x, point.y, point.z, lineColor.get());
                lastPoint = point;
            }

            // Render hit quad
            if (hitQuad) {
                if (hitQuadHorizontal) event.renderer.sideHorizontal(hitQuadX1, hitQuadY1, hitQuadZ1, hitQuadX1 + 0.5, hitQuadZ1 + 0.5, sideColor.get(), lineColor.get(), shapeMode.get());
                else event.renderer.sideVertical(hitQuadX1, hitQuadY1, hitQuadZ1, hitQuadX2, hitQuadY2, hitQuadZ2, sideColor.get(), lineColor.get(), shapeMode.get());
            }

            // Render entity
            if (entity != null) {
                double x = (entity.getX() - entity.prevX) * event.tickDelta;
                double y = (entity.getY() - entity.prevY) * event.tickDelta;
                double z = (entity.getZ() - entity.prevZ) * event.tickDelta;

                Box box = entity.getBoundingBox();
                event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
            }
        }
    }
}
