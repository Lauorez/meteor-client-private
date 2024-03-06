package meteordevelopment.meteorclient.systems.modules.own;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Sorting extends Module {
    private static final int rad = 5;

    public Sorting() {
        super(Categories.Own, "Sorting", "Scans nearby chests and tries to stack inventory items into these chests.");
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        findChestsAroundPlayer(mc.world, playerPos);
    }

    private void findChestsAroundPlayer(World world, BlockPos playerPos) {
        BlockPos.streamOutwards(playerPos, rad, rad, rad)
            .map(pos -> world.getBlockEntity(pos))
            .filter(entity -> entity instanceof ChestBlockEntity)
            .map(entity -> (ChestBlockEntity) entity)
            .forEach(this::processChest);
    }

    private void processChest(ChestBlockEntity chest) {
        System.out.println("Chest at " + chest.getPos() + " contains:");
        for (int slot = 0; slot < chest.size(); slot++) {
            ItemStack stack = chest.getStack(slot);
                System.out.println(stack.getName().getString() + " x " + stack.getCount() + " at slot " + slot);
        }
    }
}
