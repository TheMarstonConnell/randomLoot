package dev.marston.randomloot.loot;

import net.minecraft.world.item.Item;

import java.util.HashMap;

public class LootRegistry {

	public static final HashMap<String, Item> Items = new HashMap<String, Item>();

	public static final Item ToolItem = register("tool", new LootItem());
	public static final Item CaseItem = register("case", new LootCase());
	public static final Item ModAdd = register("mod_add", new ModTemplate(true));
	public static final Item ModSub = register("mod_sub", new ModTemplate(false));

	public static Item register(String name, Item item) {
		Items.put(name, item);
		return item;
	}

}
