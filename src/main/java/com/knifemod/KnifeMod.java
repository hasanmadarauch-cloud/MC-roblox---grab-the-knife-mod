package com.knifemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class KnifeMod implements ModInitializer {
	public static final String MOD_ID = "knifemod";

	/** /gamerule knifeBlood false  -> kan efektlerini kapatır */
	public static GameRules.Key<GameRules.BooleanRule> BLOOD;
	/** /gamerule knifeGrabPlayers false -> sadece moblar yakalanabilir */
	public static GameRules.Key<GameRules.BooleanRule> GRAB_PLAYERS;

	@Override
	public void onInitialize() {
		BLOOD = GameRuleRegistry.register("knifeBlood", GameRules.Category.MISC,
				GameRuleFactory.createBooleanRule(true));
		GRAB_PLAYERS = GameRuleRegistry.register("knifeGrabPlayers", GameRules.Category.PLAYER,
				GameRuleFactory.createBooleanRule(true));

		ModItems.init();
		ModEntities.init();

		ServerTickEvents.END_SERVER_TICK.register(GrabManager::tick);
	}
}
