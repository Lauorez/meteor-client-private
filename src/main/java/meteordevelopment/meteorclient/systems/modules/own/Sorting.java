//mit bot alle kisten in feststellbaren bereich ablaufen und checken ob item in inventar = item in chest -> item in chest legen
package meteordevelopment.meteorclient.systems.modules.own;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class Sorting extends Module {
    private World world = mc.world;

    public Sorting() {
        super(Categories.Own, "Sorting", "Scans nearby chests and tries to stack inventory items into these chests.");
    }

    @EventHandler
    public void onTickPre(TickEvent.Pre event) {
        if (world == null) {
            return;
        }
        // Define the range within which to search for chests
        int range = 5; // Adjust this as needed
        System.out.println("HUANSEN");
        // Get the player's current position
        BlockPos playerPos = mc.player.getBlockPos();

        // Iterate over all nearby chests
        for (BlockPos chestPos : findChestsInRange(playerPos, range)) {
            // Iterate over each slot in the player's inventory
            for (int slot = 0; slot < mc.player.getInventory().size(); slot++) {
                // Get the item stack in the current slot
                ItemStack itemStack = mc.player.getInventory().getStack(slot);
                // Check if the item stack is not empty
                if (!itemStack.isEmpty()) {
                    // Check if the item can be inserted into the chest
                    if (canInsertIntoChest(itemStack, chestPos)) {
                        // Insert the item into the chest
                        insertIntoChest(itemStack, chestPos);
                        // Remove the item from the player's inventory
                        mc.player.getInventory().removeStack(slot);
                        break; // Move to the next chest
                    }
                }
            }
        }
    }

    private boolean canInsertIntoChest(ItemStack itemStack, BlockPos chestPos) {
        // Check if the block at chestPos is a chest
        if (world.getBlockState(chestPos).getBlock() == Blocks.CHEST) {
            // Implement your logic to check if the item can be inserted into the chest
            // For example, you can check if the chest has available space:
             return world.getBlockEntity(chestPos) instanceof ChestBlockEntity;
        }

        return false; // Default to false if the block is not a chest
    }

    private void insertIntoChest(ItemStack itemStack, BlockPos chestPos) {
        // Check if the block at chestPos is a chest
        System.out.println("Test");

        if (world.getBlockState(chestPos).getBlock() == Blocks.CHEST) {
            // Implement your logic to insert the item into the chest
            // For example, you can add the item to the chest's inventory:
            // ((ChestBlockEntity)world.getBlockEntity(chestPos)).getInventory().insertStack(itemStack);
        }
    }

    private Iterable<BlockPos> findChestsInRange(BlockPos playerPos, int range) {
        // Create a list to store nearby chest positions
        List<BlockPos> nearbyChests = new ArrayList<>();

        // Iterate through all block positions within the specified range
        for (int x = playerPos.getX() - range; x <= playerPos.getX() + range; x++) {
            for (int y = playerPos.getY() - range; y <= playerPos.getY() + range; y++) {
                for (int z = playerPos.getZ() - range; z <= playerPos.getZ() + range; z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);

                    // Check if the block at the current position is a chest
                    if (world.getBlockState(blockPos).getBlock() == Blocks.CHEST) {
                        nearbyChests.add(blockPos);
                    }
                }
            }
        }

        return nearbyChests;
    }
}


