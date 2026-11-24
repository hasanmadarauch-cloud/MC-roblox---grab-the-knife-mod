package com.knifemod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** Fırlatılan bıçak. Script'teki gibi sınırsız: isabet edince kaybolur, envanterden bıçak eksilmez. */
public class ThrownKnifeEntity extends ThrownItemEntity {
	private static final float DAMAGE = 7.0f;

	public ThrownKnifeEntity(EntityType<? extends ThrownKnifeEntity> type, World world) {
		super(type, world);
	}

	public ThrownKnifeEntity(World world, LivingEntity owner) {
		super(ModEntities.THROWN_KNIFE, owner, world);
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.KNIFE;
	}

	@Override
	protected float getGravity() {
		return 0.015f; // neredeyse dümdüz uçar
	}

	@Override
	protected void onEntityHit(EntityHitResult hit) {
		super.onEntityHit(hit);
		Entity target = hit.getEntity();
		target.damage(getDamageSources().thrown(this, getOwner()), DAMAGE);
		if (getWorld() instanceof ServerWorld sw) {
			Blood.splash(sw, target, 18);
		}
	}

	@Override
	protected void onCollision(HitResult hit) {
		super.onCollision(hit);
		if (!getWorld().isClient) {
			getWorld().playSound(null, getX(), getY(), getZ(),
					hit.getType() == HitResult.Type.ENTITY ? SoundEvents.ENTITY_PLAYER_ATTACK_CRIT
							: SoundEvents.ITEM_TRIDENT_HIT_GROUND,
					SoundCategory.PLAYERS, 1.0f, 1.2f);
			discard();
		}
	}
}
