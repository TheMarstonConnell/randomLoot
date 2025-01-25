package dev.marston.randomloot.recipes;

import dev.marston.randomloot.RandomLoot;
import dev.marston.randomloot.items.ModItems;
import dev.marston.randomloot.loot.LootItem;
import dev.marston.randomloot.loot.LootUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class TextureChangeRecipe extends CustomRecipe {
	private static final Item ingredient = Items.AMETHYST_SHARD;
	private static final List<Predicate<ItemStack>> ITEM_PREDICATES = List.of(
			stack -> stack.getItem() instanceof LootItem,
			stack -> stack.getItem().equals(ingredient)
	);
	private static final Ingredient CHANGE_TEXTURE_INGREDIENT = Ingredient.of(ingredient);



	public TextureChangeRecipe(CraftingBookCategory cat) {
		super(cat);
		RandomLoot.LOGGER.info("CREATING TEXTURE CHANGE");
	}

	@Override
	public boolean matches(CraftingInput container, Level level) {
		RandomLoot.LOGGER.info("CHECKING TEXTURE CHANGE");

		if (container.ingredientCount() < 2) {
			return false;
		}


		boolean hasTool = false;
		List<ItemStack> items = container.items();

		RandomLoot.LOGGER.info(items.toString());

		for (ItemStack item: items) {
			if (item.isEmpty()) {
				continue;
			}

			if (item.getItem() instanceof LootItem) {
				if (hasTool) {
					return false;
				}
				hasTool = true;
				continue;
			}

			if (!CHANGE_TEXTURE_INGREDIENT.test(item)) {
				return false;
			}
		}


		return true;
	}

	@Override
	public @NotNull ItemStack assemble(@NotNull CraftingInput craftingInput, HolderLookup.Provider provider) {
		RandomLoot.LOGGER.info("ASSEMBLING TEXTURE CHANGE");

		int modCount = 0;
		List<ItemStack> stacks = craftingInput.items();

		ItemStack result = ItemStack.EMPTY;
        for (ItemStack item : stacks) {
			if (item.isEmpty()) {
				continue;
			}

            if (item.getItem() instanceof LootItem) {
                result = LootUtils.CloneItem(item);
				continue;
            }


			if (CHANGE_TEXTURE_INGREDIENT.test(item)) {
				modCount++;
			}

        }

		if (result.isEmpty()) {
			RandomLoot.LOGGER.info("Item is empty!!!");
			return ItemStack.EMPTY;
		}

		LootUtils.addTexture(result, modCount);

		return result;
	}

	@Override
	public RecipeSerializer<TextureChangeRecipe> getSerializer() {
		return Recipies.TEXTURE_CHANGE_SHAPELESS.get();
	}


}