package dev.marston.randomloot.recipes;

import dev.marston.randomloot.RandomLoot;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class Recipies {
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, RandomLoot.MODID);

	public static final Supplier<TextureChangeRecipe.Serializer> TEXTURE_CHANGE_RECIPE = RECIPE_SERIALIZERS.register("texture_change_recipe", () -> TextureChangeRecipe.SERIALIZER );
//	public static final Supplier<TraitAdditionRecipe.Serializer> TRAIT_ADDITION_RECIPE = RECIPE_SERIALIZERS.register("trait_change", () -> TraitAdditionRecipe.SERIALIZER );

	public static void register(IEventBus eventBus) {
		RECIPE_SERIALIZERS.register(eventBus);
	}

}
