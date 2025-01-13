package dev.marston.randomloot.loot.modifiers;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DummyContainer implements Container {

	ItemStack item;

	public DummyContainer(ItemStack stack) {
		item = stack;
	}

	public DummyContainer() {
		this(ItemStack.EMPTY);
	}

	@Override
	public void clearContent() {
		item = ItemStack.EMPTY;

	}

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return item.isEmpty();
	}

	@Override
	public ItemStack getItem(int _index) {
		return item;
	}

	@Override
	public ItemStack removeItem(int _x, int _y) {
		item = ItemStack.EMPTY;
		return item;
	}

	@Override
	public ItemStack removeItemNoUpdate(int _index) {
		item = ItemStack.EMPTY;
		return item;
	}

	@Override
	public void setItem(int _index, ItemStack stack) {
		item = stack;
	}

	@Override
	public void setChanged() {

	}

	@Override
	public boolean stillValid(Player p_18946_) {

		return true;
	}

}
