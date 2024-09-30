package dev.marston.randomloot.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.marston.randomloot.loot.LootRegistry;
import dev.marston.randomloot.loot.LootUtils;
import dev.marston.randomloot.loot.modifiers.Modifier;
import dev.marston.randomloot.loot.modifiers.ModifierRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;

import java.nio.charset.Charset;

public class TraitAdditionRecipe implements SmithingRecipe {
	public static Serializer SERIALIZER = null;

	final Ingredient item;
	final String trait;

	public TraitAdditionRecipe(Ingredient item, String trait) {
		this.item = item;
		this.trait = trait;
	}

	public boolean matches(Container inv, Level p_266781_) {
		ItemStack template = inv.getItem(0);
		if (!isTemplateIngredient(template)) { // we want an empty template
			return false;
		}

		ItemStack base = inv.getItem(1);
		if (!isBaseIngredient(base)) {
			return false;
		}

		ItemStack addition = inv.getItem(2);
		if (!isAdditionIngredient(addition)) {
			return false;
		}

		return true;

	}

	public ItemStack assemble(Container inv, RegistryAccess p_266699_) {

		ItemStack template = inv.getItem(0);

		boolean shouldAdd = false;
		if (template.getItem() == LootRegistry.ModAdd) {
			shouldAdd = true;
		} else if (template.getItem() == LootRegistry.ModSub) {
			shouldAdd = false;
		} else {
			return ItemStack.EMPTY;
		}

		ItemStack s = inv.getItem(1);

		ItemStack itemstack = s.copy();

		Modifier mod = ModifierRegistry.Modifiers.get(trait);

		if (mod == null) {
			return ItemStack.EMPTY;
		}

		if (shouldAdd) {
			itemstack = LootUtils.addModifier(itemstack, mod.clone());
		} else {
			itemstack = LootUtils.removeModifier(itemstack, mod.clone());
		}

		return itemstack;
	}

	public boolean isTemplateIngredient(ItemStack stack) {
		return stack.getItem() == LootRegistry.ModAdd || stack.getItem() == LootRegistry.ModSub;
	}

	public boolean isBaseIngredient(ItemStack stack) {
		return stack.getItem() == LootRegistry.ToolItem;
	}

	public boolean isAdditionIngredient(ItemStack stack) {
		return item.test(stack);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return getMySerializer();
	}

	public static RecipeSerializer<?> getMySerializer() {
		if (SERIALIZER == null) {
			SERIALIZER = new Serializer();
		}
		return SERIALIZER;
	}

	public static class Serializer implements RecipeSerializer<TraitAdditionRecipe> {

		private static final Codec<TraitAdditionRecipe> CODEC = RecordCodecBuilder.create((p_297394_) -> {
			return p_297394_.group(Ingredient.CODEC.fieldOf("item").forGetter((p_298441_) -> {
				return p_298441_.item;
			}), Codec.STRING.fieldOf("trait").forGetter((p_299309_) -> {
				return p_299309_.trait;
			})).apply(p_297394_, TraitAdditionRecipe::new);
		});

		public Codec<TraitAdditionRecipe> codec() {
			return CODEC;
		}

		public TraitAdditionRecipe fromNetwork(FriendlyByteBuf buffer) {

			Ingredient ingredient = Ingredient.fromNetwork(buffer);
			int l = buffer.readInt(); // getting length
			String trait = buffer.readCharSequence(l, Charset.forName("utf-8")).toString();

			TraitAdditionRecipe t = new TraitAdditionRecipe(ingredient, trait);

			return t;
		}

		public void toNetwork(FriendlyByteBuf buffer, TraitAdditionRecipe recipe) {
			recipe.item.toNetwork(buffer);

			CharSequence s = recipe.trait;
			buffer.writeInt(s.length()); // length of string to track
			buffer.writeCharSequence(s, Charset.forName("utf-8"));

		}
	}

	@Override
	public ItemStack getResultItem(RegistryAccess p_267052_) {
		return ItemStack.EMPTY;
	}
}
