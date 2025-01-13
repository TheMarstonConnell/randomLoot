package dev.marston.randomloot.recipes;

import dev.marston.randomloot.RandomLoot;
import dev.marston.randomloot.items.ModItems;
import dev.marston.randomloot.loot.LootItem;
import dev.marston.randomloot.loot.LootUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class TextureChangeRecipe extends ShapelessRecipe {
	public static ShapelessRecipe.Serializer SERIALIZER = new ShapelessRecipe.Serializer();

	private static final Item ingredient = Items.AMETHYST_SHARD;
	private static final List<Predicate<ItemStack>> ITEM_PREDICATES = List.of(
			stack -> stack.getItem() instanceof LootItem,
			stack -> stack.getItem().equals(ingredient)
	);
	private static final Ingredient CHANGE_TEXTURE_INGREDIENT = Ingredient.of(ingredient);
	private static final Item ITEM = ModItems.TOOL.get();

	public TextureChangeRecipe(CraftingBookCategory cat) {
		super("", cat, new ItemStack(ModItems.TOOL.get()), NonNullList.of(Ingredient.of(Items.AMETHYST_SHARD), Ingredient.of(ModItems.TOOL.get())));
	}

	@Override
	public boolean matches(CraftingInput container, Level level) {
		return ModCraftingHelper.allPresent(container, ITEM_PREDICATES);
	}

	@Override
	public @NotNull ItemStack assemble(@NotNull CraftingInput craftingInput, HolderLookup.Provider provider) {

		int modCount = 0;
		List<ItemStack> stacks = ModCraftingHelper.findItems(craftingInput, ITEM_PREDICATES);
		ItemStack tool = stacks.get(0).copyWithCount(1);
		ItemStack mod = stacks.get(1);
		if (tool.isEmpty() || mod.isEmpty()) {
			return ItemStack.EMPTY;
		}

		if (CHANGE_TEXTURE_INGREDIENT.test(mod)) {
			modCount++;
		}

		if (modCount == 0) {
			return ItemStack.EMPTY;
		}

		LootUtils.addTexture(tool, modCount);

		return tool;
	}

	@Override
	public RecipeSerializer<ShapelessRecipe> getSerializer() {
		return getMySerializer();
	}

	public static RecipeSerializer<ShapelessRecipe> getMySerializer() {
		return SERIALIZER;
	}



}