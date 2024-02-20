/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.Own;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.BedBlock;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import meteordevelopment.orbit.EventHandler;

import java.util.*;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;


public class BedFucker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final List<BlockPos> blocks = new ArrayList<>();
    private int timer;


    // General

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder().name("range").description("The range in blocks to search for beds.").defaultValue(4) // Set your default range value here
        .min(1).sliderRange(1, 6) // Adjust the range of the slider according to your preference
        .build());


    private final Setting<BedFucker.Mode> mode = sgGeneral.add(new EnumSetting.Builder<BedFucker.Mode>().name("mode").description("The way the blocks are broken.").defaultValue(BedFucker.Mode.Smash).build());


    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder().name("delay").description("Delay in ticks between breaking blocks.").defaultValue(0).build());

    private final Setting<Integer> maxBlocksPerTick = sgGeneral.add(new IntSetting.Builder().name("max-blocks-per-tick").description("Maximum blocks to try to break per tick. Useful when insta mining.").defaultValue(1).min(1).sliderRange(1, 6).build());

    private final Setting<Boolean> swingHand = sgGeneral.add(new BoolSetting.Builder().name("swing-hand").description("Swing hand client side.").defaultValue(true).build());


    private final Setting<Boolean> enableRenderBreaking = sgRender.add(new BoolSetting.Builder().name("broken-blocks").description("Enable rendering bounding box for Cube and Uniform Cube.").defaultValue(true).build());

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color of the target block rendering.").defaultValue(new SettingColor(255, 0, 0, 80)).visible(enableRenderBreaking::get).build());
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color of the target block rendering.").defaultValue(new SettingColor(255, 0, 0, 255)).visible(enableRenderBreaking::get).build());

    private final Setting<Boolean> enableRenderBounding = sgRender.add(new BoolSetting.Builder().name("bounding-box").description("Enable rendering bounding box for Cube and Uniform Cube.").defaultValue(true).build());

    private final Setting<ShapeMode> shapeModeBox = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("nuke-box-mode").description("How the shape for the bounding box is rendered.").defaultValue(ShapeMode.Both).build());

    private final Setting<SettingColor> sideColorBox = sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color of the bounding box.").defaultValue(new SettingColor(16, 106, 144, 100)).build());

    private final Setting<SettingColor> lineColorBox = sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color of the bounding box.").defaultValue(new SettingColor(16, 106, 144, 255)).build());

    private final Setting<ShapeMode> shapeModeBreak = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("nuke-block-mode").description("How the shapes for broken blocks are rendered.").defaultValue(ShapeMode.Both).visible(enableRenderBreaking::get).build());
    private BlockPos.Mutable pos1 = new BlockPos.Mutable(); // Rendering for cubes
    private BlockPos.Mutable pos2 = new BlockPos.Mutable();
    private Box box;
    int maxh = 0;
    int maxv = 0;


    private final BlockPos.Mutable lastBedPos = new BlockPos.Mutable();

    public BedFucker() {
        super(Categories.Own, "BedFucker", "Breaks bed blocks around you.");
    }

    @Override
    public void onActivate() {
        timer = 0;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (enableRenderBounding.get()) {
            // Render bounding box if cube and should break stuff
            box = new Box(pos1.toCenterPos(), pos2.toCenterPos());
            event.renderer.box(box, sideColorBox.get(), lineColorBox.get(), shapeModeBox.get(), 0);
        }
    }

    private final List<BlockPos.Mutable> bedsToBreak = new ArrayList<>();

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {

        double doupX = mc.player.getX();
        double doupY = mc.player.getY();
        double doupZ = mc.player.getZ();
        int pX = (int) Math.round(doupX);
        int pY = (int) Math.round(doupY);
        int pZ = (int) Math.round(doupZ);


        // Update timer
        if (timer > 0) {
            timer--;
            return;
        }


        // Calculate player's position
        BlockPos playerPos = new BlockPos(pX, pY, pZ);


        // Find beds to break

        for (BlockPos blockPos : BlockPos.iterateOutwards(playerPos, range.get(), range.get(), range.get())) {
            if (mc.world.getBlockState(blockPos).getBlock() instanceof BedBlock) {
                bedsToBreak.add(blockPos.mutableCopy());
            }
        }


        // Sort bed positions based on distance to the player
        bedsToBreak.sort(Comparator.comparingDouble(pos -> pos.getSquaredDistance(playerPos)));

        if (!bedsToBreak.isEmpty()) {
            // Mine beds
            int brokenThisTick = 0;
            while (!bedsToBreak.isEmpty() && brokenThisTick < maxBlocksPerTick.get()) {
                // Get the closest bed to the player
                BlockPos bedPos = bedsToBreak.remove(0);

                // Break the bed
                breakBed(bedPos);

                lastBedPos.set(bedPos);
                brokenThisTick++;
                if (delay.get() > 0) {
                    timer = delay.get();
                }
                blocks.clear();
                BlockPos.iterate(pos1, pos2).forEach(blocks::add);
            }
        }

    }


    private final Set<BlockPos> bossList = new ObjectOpenHashSet<>();

    private void breakBed(BlockPos pos) {

        RenderUtils.renderTickingBlock(pos, sideColor.get(), lineColor.get(), shapeModeBreak.get(), 0, 2, true, false);
        if (mode.get() == Mode.PacketMine) {

            // Send packet to server to destroy the bed
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
        } else if (mode.get() == Mode.Smash) {
            // Actually break the bed
            Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos));
            BlockUtils.breakBlock(pos, swingHand.get());
        } else if (mode.get() == Mode.LookMine) {

            Vec3d direction = new Vec3d(0, 0, 0.1).rotateX(-(float) Math.toRadians(mc.player.getPitch())).rotateY(-(float) Math.toRadians(mc.player.getYaw()));

            bossList.clear();

            for (int i = 1; i < mc.interactionManager.getReachDistance() * 10; i++) {
                BlockPos boss = BlockPos.ofFloored(mc.player.getCameraPosVec(mc.getTickDelta()).add(direction.multiply(i)));

                if (bossList.contains(boss)) continue;
                bossList.add(boss);

                if (mc.world.getBlockState(boss).hasBlockEntity() && mc.mouse.wasLeftButtonClicked()) {
                    System.out.println(mc.mouse.wasLeftButtonClicked());
                    /*mc.interactionManager.breakBlock(boss);
                     * LAURENZ ICH KANN DAS ALLES NICHT MEHR WARUM FICKT DAS BREAKBLOCK SO REIN UUURGH
                     */
                    mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
                    mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
                    return;
                }
            }
        }


    }

    private enum Mode {
        Smash, PacketMine, LookMine
    }

}
