//mit bot alle kisten in feststellbaren bereich ablaufen und checken ob item in inventar = item in chest -> item in chest legen
package meteordevelopment.meteorclient.systems.modules.own;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Sorting extends Module {


    public Sorting() {
        super(Categories.Own, "Sorting", "Scans nearby chests and tries to stack inventory items into these chests.");
    }

    @EventHandler
    public void onTickPre(TickEvent.Pre event) {
        if (this.mc.player == null) return;
        int x = (int) Math.round(mc.player.getX());
        int y = (int) Math.round(mc.player.getY());
        int z = (int) Math.round(mc.player.getZ());
        BlockPos playerPos = new BlockPos(x,y,z);
        findChestsAroundPlayer(mc.world, playerPos, 5);
    }
    public static void findChestsAroundPlayer(World world, BlockPos playerPos, int radius) {
        // Loop through blocks within the specified radius around the player
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    // Check if the block is a chest
                    if (state.getBlock() == Blocks.CHEST) {
                        // Found a chest
                        BlockEntity blockEntity = world.getBlockEntity(pos);
                        if (blockEntity instanceof ChestBlockEntity) {
                            ChestBlockEntity chest = (ChestBlockEntity) blockEntity;

                            System.out.println("Chest at " + pos + " contains:");
                            for (int slot = 0; slot < chest.size(); slot++) {
                                ItemStack stack = chest.getStack(slot);
                                System.out.println(stack.getName().getString() + " x " + stack.getCount());
                            }
                        }
                    }
                }
            }
        }
    }
}


