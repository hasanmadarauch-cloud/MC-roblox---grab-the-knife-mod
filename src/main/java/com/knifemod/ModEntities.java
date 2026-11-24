package com.knifemod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEntities {
	public static final EntityType<ThrownKnifeEntity> THROWN_KNIFE = Registry.register(
			Registries.ENTITY_TYPE,
			new Identifier(KnifeMod.MOD_ID, "thrown_knife"),
			EntityType.Builder.<ThrownKnifeEntity>create(ThrownKnifeEntity::new, SpawnGroup.MISC)
					.setDimensions(0.25f, 0.25f)
					.maxTrackingRange(4)
					.trackingTickInterval(10)
					.build("thrown_knife"));

	public static void init() {}

	private ModEntities() {}
}
