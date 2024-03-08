package meteordevelopment.meteorclient.systems.modules.own;

import meteordevelopment.meteorclient.events.packets.InventoryEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.ChestBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class Sorting extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

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

    List<ItemStack> playerInv = new ArrayList<>();

    @EventHandler
    private void onInventory(InventoryEvent event) {
        List<ItemStack> chestInv = event.packet.getContents();

        for (ItemStack itemStack : playerInv) {
            if (chestInv.contains(itemStack)) {
                mc.interactionManager.clickSlot(event.packet.getSyncId(), chestInv.indexOf(itemStack), playerInv.indexOf(itemStack), SlotActionType.SWAP, mc.player);
            }
        }
        // Hallo Theo, kommentiere diese Zeile aus, um das Öffnen der Kiste zu sehen. GaLiGrü
        mc.player.closeHandledScreen();
    }

    @EventHandler
    private void onPacketSent(PacketEvent.Send event) {
        System.out.println(event.packet.toString());
    }

    public void onActivate() {
        if (mc.player == null || mc.world == null) return;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            playerInv.add(mc.player.getInventory().getStack(i));
        }

        BlockPos playerPos = mc.player.getBlockPos();
        findChestsAroundPlayer(playerPos);
    }


    private void findChestsAroundPlayer(BlockPos playerPos) {
        double doubleValue = range.get();
        int ran = (int) Math.round(doubleValue);
        BlockPos.streamOutwards(playerPos, ran, ran, ran)
            .forEach(pos -> {
                if (mc.world.getBlockState(pos).getBlock() instanceof ChestBlock) processChest(pos);
            });
    }

    private void processChest(BlockPos pos) {
        pos = BlockPos.ofFloored(pos.getX(), pos.getY(), pos.getZ());
        Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos));
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, true));
    }

    private void checkItemsAreSame() {

    }
}

