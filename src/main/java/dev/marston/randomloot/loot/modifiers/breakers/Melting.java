package dev.marston.randomloot.loot.modifiers.breakers;

import dev.marston.randomloot.RandomLootMod;
import dev.marston.randomloot.loot.LootItem.ToolType;
import dev.marston.randomloot.loot.modifiers.BlockBreakModifier;
import dev.marston.randomloot.loot.modifiers.DummyContainer;
import dev.marston.randomloot.loot.modifiers.Modifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

public class Melting implements BlockBreakModifier {

	private String name;
	private float power;
	private final static String POWER = "power";

	public Melting(String name, float power) {
		this.name = name;
		this.power = power;
	}

	public Melting() {
		this.name = "Melting";
		this.power = 1.0f;
	}

	public Modifier clone() {
		return new Melting();
	}

	@Override
	public boolean startBreak(ItemStack itemstack, BlockPos pos, LivingEntity player) {

		Level l = player.level();

		AABB box = new AABB(pos.east().south().below().getCenter(), pos.west().north().above().getCenter());

		RegistryAccess access = l.registryAccess();
		RecipeManager manager = l.getRecipeManager();

		Thread thread = new Thread() {
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				List<Entity> items = l.getEntities(null, box);

				for (Entity entity : items) {

					if (entity.getType() == EntityType.ITEM) {
						ItemEntity i = (ItemEntity) entity;
						if (i.getAge() > 10) {
							continue;
						}

						RandomLootMod.LOGGER.info(i.getItem().getDisplayName().getString());

						ItemStack stack = i.getItem();

						DummyContainer mc = new DummyContainer(stack);

						List<RecipeHolder<SmeltingRecipe>> recipes = manager.getRecipesFor(RecipeType.SMELTING, mc, l);

						for (RecipeHolder<SmeltingRecipe> recipe : recipes) {

							ItemStack result = recipe.value().getResultItem(access).copy();

							if (result.isEmpty()) {
								continue;
							}

							ItemEntity k = i.copy();
							k.setItem(result);

							i.setPos(i.position().x, -1, i.position().z);
							i.kill();

							l.addFreshEntity(k);

							break;
						}

					}
				}

			}
		};

		thread.start();
		return false;

	}

	@Override
	public CompoundTag toNBT() {

		CompoundTag tag = new CompoundTag();

		tag.putFloat(POWER, power);

		tag.putString(NAME, name);

		return tag;
	}

	@Override
	public Modifier fromNBT(CompoundTag tag) {
		return new Melting(tag.getString(NAME), tag.getFloat(POWER));
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String tagName() {
		return "melting";
	}

	@Override
	public String color() {
		return "red";
	}

	@Override
	public String description() {
		return "Items dropped by blocks broken with this tool will be smelted.";
	}

	@Override
	public void writeToLore(List<Component> list, boolean shift) {

		MutableComponent comp = Modifier.makeComp(this.name(), this.color());

		list.add(comp);
	}

	@Override
	public Component writeDetailsToLore(@Nullable Level level) {

		return null;
	}

	@Override
	public boolean compatible(Modifier mod) {
		return true;
	}

	@Override
	public boolean forTool(ToolType type) {
		return type.equals(ToolType.PICKAXE) || type.equals(ToolType.AXE) || type.equals(ToolType.SHOVEL);
	}

	public boolean canLevel() {
		return false;
	}

	public void levelUp() {
		return;
	}
}
