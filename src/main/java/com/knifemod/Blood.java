package com.knifemod;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import org.joml.Vector3f;

/** Kan efekti: kırmızı blok parçacıkları + koyu kırmızı toz. Sadece görsel, hiçbir şeye zarar vermez. */
public final class Blood {
	private static final BlockStateParticleEffect CHUNKS =
			new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.getDefaultState());
	private static final DustParticleEffect MIST =
			new DustParticleEffect(new Vector3f(0.55f, 0.0f, 0.0f), 1.3f);

	public static void splash(ServerWorld world, Entity target, int amount) {
		if (!world.getGameRules().getBoolean(KnifeMod.BLOOD)) return;
		double x = target.getX();
		double y = target.getBodyY(0.6);
		double z = target.getZ();
		world.spawnParticles(CHUNKS, x, y, z, amount, 0.25, 0.3, 0.25, 0.15);
		world.spawnParticles(MIST, x, y, z, Math.max(1, amount / 2), 0.3, 0.3, 0.3, 0.0);
	}

	private Blood() {}
}
