package meteordevelopment.meteorclient.systems.modules.own;

import meteordevelopment.meteorclient.events.meteor.ActiveModulesChangedEvent;
import meteordevelopment.meteorclient.events.packets.InventoryEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Sorting extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> autoRotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates server-side to the block being mined.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The break range.")
        .defaultValue(4)
        .min(0)
        .build()
    );

    public Sorting() {
        super(Categories.Own, "Sorting", "Scans nearby chests and tries to stack inventory items into these chests.");
    }

    @EventHandler
    private void onPacketReceive(InventoryEvent event) {
        System.out.println(event.packet.getContents());
    }

    @Override
    public void onActivate() {
        super.onActivate();

        if (mc.player == null || mc.world == null) return;
        BlockPos playerPos = mc.player.getBlockPos();
        findChestsAroundPlayer(playerPos);
    }


    private void findChestsAroundPlayer(BlockPos playerPos) {
        double doubleValue = range.get();
        int ran = (int) Math.round(doubleValue);
        BlockPos.streamOutwards(playerPos, ran, ran, ran)
            .forEach(pos -> {
                if (mc.world.getBlockEntity(pos) instanceof ChestBlockEntity) processChest(pos);
            });
    }

    private void processChest(BlockPos pos) {
        if (autoRotate.get()) {
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, true));
        } else {
            Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), () -> {
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, true));
            });

        }
    }

    private void checkItemsAreSame() {

    }
}

