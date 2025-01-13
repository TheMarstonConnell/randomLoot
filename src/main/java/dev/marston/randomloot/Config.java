package dev.marston.randomloot;

import dev.marston.randomloot.loot.modifiers.Modifier;
import dev.marston.randomloot.loot.modifiers.ModifierRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@EventBusSubscriber(modid = RandomLootMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

	private static ModConfigSpec.DoubleValue CASE_CHANCE;

	private static ModConfigSpec.DoubleValue MOD_CHANCE;

	private static ModConfigSpec.DoubleValue GOODNESS;

	static final ModConfigSpec SPEC = build();

	public static ModConfigSpec build() {
		init();
		return BUILDER.build();
	}

	public static double CaseChance;
	public static double ModChance;
	public static double Goodness;

	private static Map<String, ModConfigSpec.BooleanValue> MODIFIERS_ENABLED;
	private static Map<String, Boolean> ModsEnabled;

	public static void init() {

		BUILDER.push("Loot Chances");
		CASE_CHANCE = BUILDER.comment("chance to find a case in a chest.").defineInRange("caseChance", 0.25, 0.0, 1.0);
		MOD_CHANCE = BUILDER.comment("chance to find a modifier template in a chest.").defineInRange("modChance", 0.15,
				0.0, 1.0);
		BUILDER.pop();

		BUILDER.push("Modifiers Enabled");
		MODIFIERS_ENABLED = new HashMap<String, ModConfigSpec.BooleanValue>();

		for (Entry<String, Modifier> entry : ModifierRegistry.Modifiers.entrySet()) {
			String key = entry.getKey();
			Modifier mod = entry.getValue();

			MODIFIERS_ENABLED.put(key,
					BUILDER.comment("should the " + mod.name() + " trait be enabled").define(key + "_enabled", true));
		}
		BUILDER.pop();

		BUILDER.push("Misc");
		GOODNESS = BUILDER.comment("rate of tool improvement per player").defineInRange("goodness_rate", 1.0, 0.01,
				10.0);
		BUILDER.pop();
	}

	public static boolean traitEnabled(String tagName) {
		return ModsEnabled.get(tagName);
	}

	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
		CaseChance = CASE_CHANCE.get();
		ModChance = MOD_CHANCE.get();
		Goodness = GOODNESS.get();

		ModsEnabled = new HashMap<String, Boolean>();
		for (Entry<String, BooleanValue> entry : MODIFIERS_ENABLED.entrySet()) {
			String key = entry.getKey();
			BooleanValue val = entry.getValue();

			ModsEnabled.put(key, val.get());
		}

	}
}
