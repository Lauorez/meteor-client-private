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
import net.minecraft.item.Items;
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
    private final Setting<Boolean> includeHotbar = sgGeneral.add(new BoolSetting.Builder()
        .name("include-hotbar")
        .description("Whether to sort away hotbar or not.")
        .defaultValue(true)
        .build()
    );

    public Sorting() {
        super(Categories.Own, "Sorting", "Scans nearby chests and tries to stack inventory items into these chests.");
    }


    @EventHandler
    private void onInventory(InventoryEvent event) {
        List<ItemStack> inv = event.packet.getContents();

        int hotbar = 0;
        if (!includeHotbar.get()) hotbar = 9;

        // Das For-Schleifen-Gedöns könnte man noch optimieren,
        // und zwar wie man feststellt, ob die items in der Kiste bereits vorkommen oder nicht
        // zwei for schleifen ist da eher ineffizient i guess

        for (int i = inv.size() - (36 - hotbar); i < inv.size(); i++) {
            ItemStack playerItemStack = inv.get(i);
            if (playerItemStack.getItem().equals(Items.AIR)) continue;
            for (int j = 0; j < inv.size() - 36; j++) {
                ItemStack chestItemStack = inv.get(j);
                if (chestItemStack.getItem().equals(Items.AIR)) continue;
                if (playerItemStack.getItem().equals(chestItemStack.getItem())) {
                    mc.interactionManager.clickSlot(event.packet.getSyncId(), i, 0, SlotActionType.QUICK_MOVE, mc.player);
                }
            }
        }
        mc.player.closeHandledScreen();
    }

    public void onActivate() {
        if (mc.player == null || mc.world == null) return;

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

