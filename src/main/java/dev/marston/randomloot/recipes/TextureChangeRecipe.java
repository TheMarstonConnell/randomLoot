package dev.marston.randomloot.recipes;

import dev.marston.randomloot.loot.LootRegistry;
import dev.marston.randomloot.loot.LootUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class TextureChangeRecipe extends CustomRecipe {
	public static SimpleCraftingRecipeSerializer<TextureChangeRecipe> SERIALIZER = null;

	private static final Ingredient CHANGE_TEXTURE_INGREDIENT = Ingredient.of(Items.AMETHYST_SHARD);
	private static final Item ITEM = LootRegistry.ToolItem;

	public TextureChangeRecipe(CraftingBookCategory cat) {
		super(cat);
	}

	@Override
	public boolean matches(CraftingContainer inv, Level level) {
		return !this.getOutput(inv).isEmpty();
	}

	private ItemStack getOutput(Container inv) {
		int size = inv.getContainerSize();

		int modCount = 0;
		ItemStack toolItem = null;

		for (int i = 0; i < size; i++) {
			ItemStack item = inv.getItem(i);
			if (item.isEmpty()) { // skip empty items
				continue;
			}

			if (ITEM == item.getItem()) {
				if (toolItem != null) {
					return ItemStack.EMPTY;
				}
				toolItem = item.copy();
				continue;
			}

			if (CHANGE_TEXTURE_INGREDIENT.test(item)) {
				modCount++;
			}

		}

		if (toolItem == null) {
			return ItemStack.EMPTY;
		}

		if (modCount == 0) {
			return ItemStack.EMPTY;
		}

		LootUtils.addTexture(toolItem, modCount);

		return toolItem;

	}

	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
		return this.getOutput(inv);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return getMySerializer();
	}

	public static RecipeSerializer<?> getMySerializer() {
		if (SERIALIZER == null) {
			SERIALIZER = new SimpleCraftingRecipeSerializer<TextureChangeRecipe>(
					(category) -> new TextureChangeRecipe(category));
		}
		return SERIALIZER;
	}

}