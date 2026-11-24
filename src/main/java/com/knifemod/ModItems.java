package com.knifemod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
	public static final Item KNIFE = Registry.register(Registries.ITEM,
			new Identifier(KnifeMod.MOD_ID, "knife"),
			new KnifeItem(new Item.Settings()));

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> entries.add(KNIFE));
	}

	private ModItems() {}
}
