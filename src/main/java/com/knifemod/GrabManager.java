package com.knifemod;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Kimin kimi yakaladığını tutar ve yakalanan hedefi her tik oyuncunun önünde tutar. Sadece sunucu tarafı. */
public final class GrabManager {
	private static final Map<UUID, LivingEntity> HELD = new HashMap<>();
	private static final float STAB_DAMAGE = 8.0f;
	private static final double FLING_SPEED = 2.2;

	public static boolean isHolding(PlayerEntity p) {
		return HELD.containsKey(p.getUuid());
	}

	private static boolean isHeldByAnyone(LivingEntity e) {
		return HELD.containsValue(e);
	}

	public static boolean grab(ServerPlayerEntity holder, LivingEntity target) {
		if (target == holder || !target.isAlive() || isHolding(holder)) return false;
		if (isHeldByAnyone(target)) return false;
		if (target instanceof PlayerEntity tp) {
			if (!holder.getServerWorld().getGameRules().getBoolean(KnifeMod.GRAB_PLAYERS)) return false;
			if (tp.isSpectator() || tp.isCreative()) return false;
			if (isHolding(tp)) return false; // karşılıklı yakalama yok
		}
		if (holder.squaredDistanceTo(target) > 9.0) return false;

		HELD.put(holder.getUuid(), target);
		holder.getMainHandStack().getOrCreateNbt().putBoolean(KnifeItem.GRAB_TAG, true);
		holder.getWorld().playSound(null, holder.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
				SoundCategory.PLAYERS, 1.0f, 0.8f);
		holder.sendMessage(Text.translatable("knifemod.grab.start", target.getDisplayName()), true);
		if (target instanceof ServerPlayerEntity tp) {
			tp.sendMessage(Text.translatable("knifemod.grab.victim", holder.getDisplayName()), true);
		}
		return true;
	}

	public static void stab(ServerPlayerEntity holder) {
		LivingEntity target = HELD.get(holder.getUuid());
		if (target == null) return;
		ServerWorld world = holder.getServerWorld();

		target.timeUntilRegen = 0; // arka arkaya bıçaklamalar sayılsın
		target.damage(holder.getDamageSources().playerAttack(holder), STAB_DAMAGE);
		Blood.splash(world, target, 25);
		world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
				SoundCategory.PLAYERS, 1.0f, 0.7f);
		holder.getMainHandStack().damage(1, holder, p -> p.sendToolBreakStatus(p.getActiveHand()));
		holder.swingHand(net.minecraft.util.Hand.MAIN_HAND, true);

		if (!target.isAlive()) {
			Blood.splash(world, target, 60);
			release(holder);
		}
	}

	public static void fling(ServerPlayerEntity holder) {
		LivingEntity target = HELD.get(holder.getUuid());
		if (target == null) return;
		release(holder);
		Vec3d v = holder.getRotationVec(1.0f).multiply(FLING_SPEED).add(0, 0.35, 0);
		target.setVelocity(v);
		target.velocityModified = true;
		holder.getWorld().playSound(null, holder.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
				SoundCategory.PLAYERS, 1.0f, 0.6f);
	}

	public static void release(ServerPlayerEntity holder) {
		HELD.remove(holder.getUuid());
		clearTags(holder);
	}

	private static void clearTags(PlayerEntity p) {
		for (ItemStack s : p.getInventory().main) if (s.isOf(ModItems.KNIFE)) s.removeSubNbt(KnifeItem.GRAB_TAG);
		for (ItemStack s : p.getInventory().offHand) if (s.isOf(ModItems.KNIFE)) s.removeSubNbt(KnifeItem.GRAB_TAG);
	}

	public static void tick(MinecraftServer server) {
		if (HELD.isEmpty()) return;
		for (Map.Entry<UUID, LivingEntity> e : new ArrayList<>(HELD.entrySet())) {
			ServerPlayerEntity holder = server.getPlayerManager().getPlayer(e.getKey());
			LivingEntity target = e.getValue();

			if (holder == null) { HELD.remove(e.getKey()); continue; }
			boolean invalid = !holder.isAlive()
					|| !target.isAlive() || target.isRemoved()
					|| target.getWorld() != holder.getWorld()
					|| !holder.getMainHandStack().isOf(ModItems.KNIFE)
					|| (target instanceof PlayerEntity tp && (tp.isSpectator() || tp.isCreative()));
			if (invalid) { release(holder); continue; }

			Vec3d look = holder.getRotationVec(1.0f);
			Vec3d pos = holder.getEyePos()
					.add(look.multiply(1.3 + target.getWidth() * 0.5))
					.subtract(0, target.getHeight() * 0.65, 0);

			target.fallDistance = 0;
			target.setVelocity(Vec3d.ZERO);
			if (target instanceof ServerPlayerEntity tp) {
				tp.networkHandler.requestTeleport(pos.x, pos.y, pos.z, tp.getYaw(), tp.getPitch());
			} else {
				target.setPosition(pos.x, pos.y, pos.z);
				target.velocityModified = true;
				if (target instanceof MobEntity mob) mob.getNavigation().stop();
			}
		}
	}

	private GrabManager() {}
}
