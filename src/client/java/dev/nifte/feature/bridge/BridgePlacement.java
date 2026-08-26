package dev.nifte.feature.bridge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

record BridgePlacement(BlockHitResult hit, BlockPos target, double distanceSq) {
}
