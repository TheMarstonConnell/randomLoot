package dev.marston.randomloot.loot;

import com.google.gson.JsonObject;
import dev.marston.randomloot.Config;
import dev.marston.randomloot.loot.LootItem.ToolType;
import dev.marston.randomloot.loot.modifiers.Modifier;
import dev.marston.randomloot.loot.modifiers.ModifierRegistry;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class LootUtils {

	private static int PICKAXE_COUNT = 18;
	private static int AXE_COUNT = 12;
	private static int SHOVEL_COUNT = 9;
	private static int SWORD_COUNT = 47;

	public static void addLoreLine(ListTag lore, String text, String color) {
		JsonObject value = new JsonObject();

		value.addProperty("text", text);
		value.addProperty("color", color);
		value.addProperty("italic", false);

		StringTag nbtName = StringTag.valueOf(value.toString());

		lore.add(nbtName);
	}

	public static void setItemName(ItemStack stack, String name, String color) {
		JsonObject value = new JsonObject();

		value.addProperty("text", name);
		value.addProperty("color", color);
		value.addProperty("italic", false);

		StringTag nbtName = StringTag.valueOf(value.toString());

		CompoundTag display = stack.getOrCreateTagElement("display");

		display.put("Name", nbtName);

		stack.addTagElement("display", display);
	}

	public static void setItemLore(ItemStack stack, String lore) {
		CompoundTag tag = stack.getOrCreateTag();

		tag.putString("itemLore", lore);

		stack.setTag(tag);
	}

	public static String getItemLore(ItemStack stack) {
		CompoundTag tag = stack.getOrCreateTag();
		String loreString = tag.getString("itemLore");
		return loreString;
	}

	public static int getMaxXP(int level) {
		int starting = 500;

		int xp = (int) (starting * Math.pow(2, level));
		return xp;
	}

	public static ItemStack levelUp(ItemStack item, LivingEntity holder) {

		float stats = getStats(item);
		stats = stats * 1.1f;
		setStats(item, stats);

		holder.level().playSound(null, holder.getX(), holder.getY(), holder.getZ(), SoundEvents.PLAYER_LEVELUP,
				holder.getSoundSource(), 1.0f, 1.0f);

		return item;
	}

	public static ItemStack addXp(ItemStack item, LivingEntity holder, int amount) {

		CompoundTag tag = item.getOrCreateTagElement("XP");

		int level = tag.getInt("level");

		int xp = tag.getInt("xp");

		xp += amount;

		int max = getMaxXP(level);

		while (xp >= max) {
			xp = xp - max;
			level++;
			item = levelUp(item, holder);
		}

		tag.putInt("level", level);
		tag.putInt("xp", xp);

		item.addTagElement("XP", tag);

		return item;
	}

	public static int getLevel(ItemStack item) {

		CompoundTag tag = item.getOrCreateTagElement("XP");

		int level = tag.getInt("level");

		return level;
	}

	public static int getXP(ItemStack item) {

		CompoundTag tag = item.getOrCreateTagElement("XP");

		int xp = tag.getInt("xp");

		return xp;
	}

	public static ItemStack setLevelAndXP(ItemStack item, int level, int xp) {

		CompoundTag tag = item.getOrCreateTagElement("XP");

		tag.putInt("level", level);
		tag.putInt("xp", xp);

		item.addTagElement("XP", tag);

		return item;
	}

	public static List<Modifier> getModifiers(ItemStack item) {

		ArrayList<Modifier> tags = new ArrayList<Modifier>();

		CompoundTag modifiers = item.getOrCreateTagElement(Modifier.MODTAG);
		if (modifiers == null) {
			return tags;
		}

		Set<String> mods = modifiers.getAllKeys();

		for (Iterator<String> iterator = mods.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();

			CompoundTag modTag = modifiers.getCompound(string);

			Modifier finalModifier = ModifierRegistry.loadModifier(string, modTag);
			if (finalModifier == null) {
				continue;
			}
			if (!Config.traitEnabled(finalModifier.tagName())) {
				continue;
			}

			tags.add(finalModifier);

		}

		return tags;
	}

	public static ItemStack addModifier(ItemStack item, Modifier mod) {

		CompoundTag modifiers = item.getOrCreateTagElement(Modifier.MODTAG);
		CompoundTag newTag = mod.toNBT();

		boolean oldModFound = modifiers.contains(mod.tagName()); // does that tag already exist on the tool?

		if (!oldModFound) { // tag doesn't exist so we're adding a new trait.

			modifiers.put(mod.tagName(), newTag);
			item.removeTagKey(Modifier.MODTAG);
			item.addTagElement(Modifier.MODTAG, modifiers);
			return item;

		}

		CompoundTag oldmod = modifiers.getCompound(mod.tagName());
		Modifier oldModifier = mod.fromNBT(oldmod);

		if (!oldModifier.canLevel()) {
			return item;
		}

		oldModifier.levelUp();
		modifiers.put(oldModifier.tagName(), oldModifier.toNBT());

		item.removeTagKey(Modifier.MODTAG);
		item.addTagElement(Modifier.MODTAG, modifiers);

		return item;
	}

	public static ItemStack removeModifier(ItemStack item, Modifier mod) {

		CompoundTag modifiers = item.getOrCreateTagElement(Modifier.MODTAG);

		modifiers.remove(mod.tagName());

		item.removeTagKey(Modifier.MODTAG);
		item.addTagElement(Modifier.MODTAG, modifiers);

		return item;
	}

	public static void setStats(ItemStack stack, float goodness) {
		CompoundTag statTag = stack.getOrCreateTagElement("itemStats");

		statTag.putFloat("goodness", goodness);

		stack.addTagElement("itemStats", statTag);
	}

	public static float getStats(ItemStack stack) {
		CompoundTag statTag = stack.getOrCreateTagElement("itemStats");

		float goodness = statTag.getFloat("goodness");

		return goodness;
	}

	public static void setTexture(ItemStack stack, int texture) {
		CompoundTag cosmeticTag = stack.getOrCreateTagElement("cosmetics");

		cosmeticTag.putInt("texture", texture);

		stack.addTagElement("cosmetics", cosmeticTag);
	}

	public static float getTexture(ItemStack stack) {
		CompoundTag cosmeticTag = stack.getOrCreateTagElement("cosmetics");

		int texture = cosmeticTag.getInt("texture");

		float index = ((float) texture) / 10000.0f;

		ToolType type = getToolType(stack);

		switch (type) {

		case PICKAXE:
			index += 0.1f;
			break;
		case SHOVEL:
			index += 0.2f;
			break;
		case AXE:
			index += 0.3f;
			break;
		case SWORD:
			index += 0.4f;
			break;
		case NULL:
		default:
			break;

		}

		return index;
	}

	public static int getTextureIndex(ItemStack stack) {
		CompoundTag cosmeticTag = stack.getOrCreateTagElement("cosmetics");

		int texture = cosmeticTag.getInt("texture");

		return texture;
	}

	private static void generateLore(ItemStack lootItem, Level level, Player player) {
		String nameColor = "#c0c7d1";
		float temp = 0.7f;

		if (player != null) {
			Holder<Biome> biome = level.getBiome(player.blockPosition());

			Biome b = biome.value();

			temp = b.getBaseTemperature();

			nameColor = String.format("#%06X", (0xFFFFFF & b.getFoliageColor()));
			if (level.dimension().equals(Level.NETHER)) {
				nameColor = "#FF8C19";
			} else if (level.dimension().equals(Level.END)) {
				nameColor = "#C419FF";
			}
		}

		LootUtils.setItemName(lootItem, NameGenerator.generateNameWPrefix(temp, level.isRaining()), nameColor);

		String forger = NameGenerator.generateForger(temp);

		String name = "a machine";
		if (player != null) {
			name = player.getDisplayName().getString();
		}

		String loreText = "Discovered by " + name + ", forged by " + forger + ".";

		LootUtils.setItemLore(lootItem, loreText);

	}

	public static ToolType getToolType(ItemStack item) {
		CompoundTag toolType = item.getOrCreateTagElement("info");
		String type = toolType.getString("type");
		if (type == "") {
			return ToolType.NULL;
		}
		return ToolType.valueOf(type);
	}

	public static ItemStack setToolType(ItemStack item, ToolType type) {
		CompoundTag toolInfo = item.getOrCreateTagElement("info");
		toolInfo.putString("type", type.name());
		item.addTagElement("info", toolInfo);
		return item;
	}

	public static void generateNewTrait(ItemStack stack, ToolType type) {

		List<Modifier> mods = getModifiers(stack);

		ArrayList<Modifier> allowedMods = new ArrayList<Modifier>();

		for (Entry<String, Modifier> entry : ModifierRegistry.Modifiers.entrySet()) {
			Modifier newMod = entry.getValue();

			if (!Config.traitEnabled(newMod.tagName())) {
				continue;
			}

			if (!newMod.forTool(type)) {
				continue;
			}

			boolean compatible = true;

			for (Modifier modifier : mods) {

				if (modifier.tagName() == newMod.tagName()) {
					if (!modifier.canLevel()) {
						compatible = false;
						break;
					}
				}

				if (!modifier.compatible(newMod)) {
					compatible = false;
					break;
				}

			}

			if (compatible) {
				allowedMods.add(newMod);
			}

		}

		int size = allowedMods.size();

		if (size == 0) {
			return;
		}

		int choice = (int) (Math.random() * size);

		Modifier m = allowedMods.get(choice);

		addModifier(stack, m);

	}

	public static void generateInitialTraits(ItemStack stack, ToolType type, int count) {
		for (int i = 0; i < count; i++) {
			generateNewTrait(stack, getToolType(stack));
		}
	}

	public static int getToolMaxTextures(ItemStack stack) {
		ToolType m = getToolType(stack);

		return switch (m) {
		case PICKAXE: {
			yield PICKAXE_COUNT;
		}
		case AXE: {
			yield AXE_COUNT;
		}
		case SHOVEL: {
			yield SHOVEL_COUNT;
		}
		case SWORD: {
			yield SWORD_COUNT;
		}
		default:
			yield 0;
		};

	}

	public static int addToolTextures(ItemStack stack, int count) {
		int max = getToolMaxTextures(stack);

		int current = getTextureIndex(stack);

		int newTexture = (current + count) % max;

		return newTexture;

	}

	public static void addTexture(ItemStack stack, int count) {
		setTexture(stack, addToolTextures(stack, count));
	}

	public static ItemStack genTool(Player player, Level level) {
		ItemStack lootItem = new ItemStack(LootRegistry.ToolItem);

		/**
		 * We want to be able to make the loot get better over time for players. This is
		 * pretty trivial with the statistics of a player since we can just check how
		 * many cases they've opened. The more cases they've opened, the better their
		 * tools will be. We do this on a SQRT curve to ensure that tools get better
		 * over time without getting out of hand. Since the tools level pretty closely
		 * to this curve we can expect that users won't constantly feel like they should
		 * use the same tool since it's already leveled up to be better than what these
		 * new tools start at. But at the same time, players won't feel sad abandoning
		 * their old tools in favor of these new ones since they're comparable AND these
		 * new ones have a clean XP slate so they can level faster again.
		 */
		int count = 0;
		if (level.isClientSide) {
			return ItemStack.EMPTY;
		}

		if (player != null) {
			ServerPlayer sPlayer = (ServerPlayer) player;
			StatType<Item> itemUsed = Stats.ITEM_USED;
			count = sPlayer.getStats().getValue(itemUsed.get(LootRegistry.CaseItem));
		}

		float goodness = (float) (Math.sqrt(count + 1) * Config.Goodness); // keeping track of items stats through a
																			// "goodness" curve

		int traits = (int) (Math.floor(goodness / 2.0f)); // how many traits the tool should be created with

		LootUtils.setStats(lootItem, goodness);

		int toolType = (int) (Math.random() * 4);
		ToolType m = switch (toolType) {
		case 0: {
			yield ToolType.PICKAXE;
		}
		case 1: {
			yield ToolType.AXE;
		}
		case 2: {
			yield ToolType.SHOVEL;
		}
		case 3: {
			yield ToolType.SWORD;
		}
		default: {
			yield ToolType.PICKAXE;
		}
		};

		int textureCount = switch (m) {
		case PICKAXE: {
			yield PICKAXE_COUNT;
		}
		case AXE: {
			yield AXE_COUNT;
		}
		case SHOVEL: {
			yield SHOVEL_COUNT;
		}
		case SWORD: {
			yield SWORD_COUNT;
		}
		default:
			yield 0;
		};

		lootItem = setToolType(lootItem, m);

		generateInitialTraits(lootItem, m, traits);

		generateLore(lootItem, level, player);

		LootUtils.setTexture(lootItem, (int) (Math.random() * textureCount));

		return lootItem;
	}

	public static boolean generateTool(ServerPlayer player, Level level) {

		ItemStack lootItem = genTool(player, level);

		boolean added = player.getInventory().add(lootItem);
		if (!added) {
			ItemEntity dropItem = new ItemEntity(EntityType.ITEM, level);
			dropItem.setItem(lootItem);
			dropItem.setPos(player.position());

			level.addFreshEntity(dropItem);
		}
		return true;
	}

	public static String roman(int input) {
		if (input < 1 || input > 3999)
			return "Invalid Roman Number Value";
		String s = "";
		while (input >= 1000) {
			s += "M";
			input -= 1000;
		}
		while (input >= 900) {
			s += "CM";
			input -= 900;
		}
		while (input >= 500) {
			s += "D";
			input -= 500;
		}
		while (input >= 400) {
			s += "CD";
			input -= 400;
		}
		while (input >= 100) {
			s += "C";
			input -= 100;
		}
		while (input >= 90) {
			s += "XC";
			input -= 90;
		}
		while (input >= 50) {
			s += "L";
			input -= 50;
		}
		while (input >= 40) {
			s += "XL";
			input -= 40;
		}
		while (input >= 10) {
			s += "X";
			input -= 10;
		}
		while (input >= 9) {
			s += "IX";
			input -= 9;
		}
		while (input >= 5) {
			s += "V";
			input -= 5;
		}
		while (input >= 4) {
			s += "IV";
			input -= 4;
		}
		while (input >= 1) {
			s += "I";
			input -= 1;
		}
		return s;

	}

}
