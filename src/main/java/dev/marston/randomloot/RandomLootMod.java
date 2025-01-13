package dev.marston.randomloot;

import com.mojang.logging.LogUtils;
import dev.marston.randomloot.loot.LootCase;
import dev.marston.randomloot.loot.LootRegistry;
import dev.marston.randomloot.loot.LootUtils;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

import java.util.Map;

@Mod(RandomLootMod.MODID)
public class RandomLootMod {
	public static final String MODID = "randomloot";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
			.create(Registries.CREATIVE_MODE_TAB, MODID);

	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
			.create(Registries.RECIPE_SERIALIZER, MODID);

	static RandomLootMod INSTANCE;

	public RandomLootMod(IEventBus bus) {

		if (INSTANCE != null) {
			throw new IllegalStateException();
		}
		INSTANCE = this;

		bus.addListener((RegisterEvent event) -> {
			if (!event.getRegistryKey().equals(Registries.BLOCK)) {
				return;
			}
//			Recipies.init(BuiltInRegistries.RECIPE_SERIALIZER);
		});

		bus.addListener(this::commonSetup);
		bus.addListener(this::addCreative);

		NeoForge.EVENT_BUS.register(this);

//		ModLootModifiers.register(bus);

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

		GenWiki.genWiki();

	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		LOGGER.info("RandomLoot Common Setup");

	}

	private void addCreative(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
			event.accept(LootRegistry.CaseItem);
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		LOGGER.info("Starting server with RandomLoot installed!");

		Globals.Seed = event.getServer().getWorldData().worldGenOptions().seed();
	}

	@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			LOGGER.info("Client is starting with RandomLoot installed!");

			event.enqueueWork(() -> {
				ItemProperties.register(LootRegistry.ToolItem, new ResourceLocation(RandomLootMod.MODID, "cosmetic"),
						(stack, level, living, id) -> {
							return LootUtils.getTexture(stack);
						});
			});
		}
	}

	@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {

		@SubscribeEvent
		public static void registerItems(RegisterEvent event) {

			event.register(Registries.ITEM, helper -> {
				for (Map.Entry<String, Item> entry : LootRegistry.Items.entrySet()) {
					String key = entry.getKey();
					Item val = entry.getValue();

					helper.register(new ResourceLocation(MODID, key), val);
				}

			});
			LootCase.initDispenser();

		}

	}
}
