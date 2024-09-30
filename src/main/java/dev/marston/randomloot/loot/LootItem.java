package dev.marston.randomloot.loot;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.marston.randomloot.Config;
import dev.marston.randomloot.loot.modifiers.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LootItem extends Item {
	public static enum ToolType {
		PICKAXE, SHOVEL, AXE, SWORD, NULL;

		@Override
		public String toString() {
			switch (this) {
			case PICKAXE:
				return "Pickaxe";
			case SHOVEL:
				return "Shovel";
			case AXE:
				return "Axe";
			case SWORD:
				return "Sword";
			default:
				return "Null";
			}
		}
	}

	public LootItem() {
		super(new Properties().stacksTo(1).durability(100));
	}

	public static float getDigSpeed(ItemStack stack, ToolType type) {

		float statMod = 1.0f;

		List<Modifier> mods = LootUtils.getModifiers(stack);

		for (Modifier mod : mods) {
			if (mod instanceof StatsModifier) {
				if (!Config.traitEnabled(mod.tagName())) {
					continue;
				}
				StatsModifier ehm = (StatsModifier) mod;

				statMod *= ehm.getStats(stack);
			}
		}

		if (type.equals(ToolType.SWORD)) {
			return 1.0f;
		}

		float speed = (LootUtils.getStats(stack) / 2.0f) + 6.0f;
		return speed * statMod;
	}

	public static float getAttackSpeed(ItemStack stack, ToolType type) {

		float speed = 0.0f;

		switch (type) {
		case PICKAXE:
			speed = -2.8F;
			break;
		case AXE:
			speed = -3.0F;
			break;
		case SHOVEL:
			speed = -3.0F;
			break;
		case SWORD:
			speed = -2.4F;
			break;
		default:
			break;
		}

		return speed;
	}

	public static float getAttackDamage(ItemStack stack, ToolType type) {

		float damage = (LootUtils.getStats(stack)) + 1.0f;

		switch (type) {
		case PICKAXE:
			damage = damage * 0.5f;
			break;
		case AXE:
			damage = damage * 1.2f;
			break;
		case SHOVEL:
			damage = damage * 0.6f;
			break;
		default:
			break;
		}

		return damage;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState block) {

		ToolType type = LootUtils.getToolType(stack);

		TagKey<Block> blocks = null;

		if (type == ToolType.PICKAXE) {
			blocks = BlockTags.MINEABLE_WITH_PICKAXE;
		} else if (type == ToolType.AXE) {
			blocks = BlockTags.MINEABLE_WITH_AXE;
		} else if (type == ToolType.SHOVEL) {
			blocks = BlockTags.MINEABLE_WITH_SHOVEL;
		} else if (type == ToolType.SWORD) {
			if (block.getBlock() == Blocks.COBWEB) {
				return 15.0f;
			}
			return 1.0f;
		} else {
			return 1.0f;
		}

		return block.is(blocks) ? getDigSpeed(stack, type) : 1.0F;
	}

	@Override
	public boolean isRepairable(ItemStack stack) {
		return true;
	}

	@Override
	public boolean isValidRepairItem(ItemStack tool, ItemStack material) {
		return material.getItem().equals(Items.DIAMOND);
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {

		ToolType type = LootUtils.getToolType(stack);

		TagKey<Block> blocks = null;

		if (type == ToolType.PICKAXE) {
			blocks = BlockTags.MINEABLE_WITH_PICKAXE;
		} else if (type == ToolType.SHOVEL) {
			blocks = BlockTags.MINEABLE_WITH_SHOVEL;
		} else if (type == ToolType.AXE) {
			blocks = BlockTags.MINEABLE_WITH_AXE;
		} else {
			return false;
		}

		return state.is(blocks)
				&& net.neoforged.neoforge.common.TierSortingRegistry.isCorrectTierForDrops(Tiers.DIAMOND, state);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {

		if (!slot.equals(EquipmentSlot.MAINHAND)) {
			return super.getAttributeModifiers(slot, stack);
		}

		ToolType tt = LootUtils.getToolType(stack);

		float damage = getAttackDamage(stack, tt);

		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
				(double) damage, AttributeModifier.Operation.ADDITION));
		builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
				(double) getAttackSpeed(stack, tt), AttributeModifier.Operation.ADDITION));

		return builder.build();
	}

	@Override
	public boolean hurtEnemy(ItemStack itemstack, LivingEntity hurtee, LivingEntity hurter) {

		ToolType type = LootUtils.getToolType(itemstack);

		if (type == ToolType.AXE || type == ToolType.SWORD) {
			LootUtils.addXp(itemstack, hurter, 1);

		}

		List<Modifier> mods = LootUtils.getModifiers(itemstack);

		boolean shouldSkipBreak = false;
		for (Modifier mod : mods) {
			if (mod instanceof EntityHurtModifier) {
				if (!Config.traitEnabled(mod.tagName())) {
					continue;
				}
				EntityHurtModifier ehm = (EntityHurtModifier) mod;

				if (ehm.hurtEnemy(itemstack, hurtee, hurter)) {
					shouldSkipBreak = true;
				}

			}

			if (mod instanceof Unbreaking) {
				if (!Config.traitEnabled(mod.tagName())) {
					continue;
				}
				Unbreaking unbreaking = (Unbreaking) mod;

				if (unbreaking.test(hurtee.level())) {
					shouldSkipBreak = true;
				}

			}
		}
		if (!shouldSkipBreak) {
			itemstack.hurtAndBreak(1, hurter, (p_43296_) -> {
				p_43296_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
			});
		}

		return true;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {

		if (enchantment.category.equals(EnchantmentCategory.BREAKABLE)) { // all these items are breakable so we can
																			// enchant them first!
			return true;
		}

		ToolType type = LootUtils.getToolType(stack);
		if (enchantment.category.equals(EnchantmentCategory.DIGGER)) {
			if (type == ToolType.AXE || type == ToolType.SHOVEL || type == ToolType.PICKAXE) {
				return true;
			}
		}

		if (enchantment.category.equals(EnchantmentCategory.WEAPON)) {
			if (type == ToolType.AXE || type == ToolType.SWORD) {
				return true;
			}
		}

		return enchantment.category.canEnchant(stack.getItem());
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level level, BlockState blockState, BlockPos pos, LivingEntity player) {
		if (!level.isClientSide && blockState.getDestroySpeed(level, pos) != 0.0F) {

			List<Modifier> mods = LootUtils.getModifiers(stack);

			boolean shouldSkipBreak = false;
			for (Modifier mod : mods) {

				if (mod instanceof BlockBreakModifier) {
					BlockBreakModifier bbm = (BlockBreakModifier) mod;

					if (bbm.startBreak(stack, pos, player)) {
						shouldSkipBreak = true;
					}
				}

				if (mod instanceof Unbreaking) {
					if (!Config.traitEnabled(mod.tagName())) {
						continue;
					}
					Unbreaking unbreaking = (Unbreaking) mod;

					if (unbreaking.test(level)) {
						shouldSkipBreak = true;
					}

				}
			}

			if (!shouldSkipBreak) {
				stack.hurtAndBreak(1, player, (p_40992_) -> {
					p_40992_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
				});
			}

			LootUtils.addXp(stack, player, 1);

		}

		return true;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {

		return super.onBlockStartBreak(itemstack, pos, player);
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		float stats = (LootUtils.getStats(stack) + 10.0f) * 80.0f;

		return (int) stats;
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {

		ItemStack itemstack = ctx.getItemInHand();

		List<Modifier> mods = LootUtils.getModifiers(itemstack);

		for (Modifier mod : mods) {

			if (mod instanceof UseModifier) {
				if (!Config.traitEnabled(mod.tagName())) {
					continue;
				}
				UseModifier um = (UseModifier) mod;

				um.use(ctx);
			}

		}

		return InteractionResult.PASS;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack toolItem = player.getItemInHand(hand);

		if (player instanceof ServerPlayer) {
			ServerPlayer sPlayer = (ServerPlayer) player;
			StatType<Item> itemUsed = Stats.ITEM_USED;

			sPlayer.getStats().increment(sPlayer, itemUsed.get(LootRegistry.ToolItem), 1);
		}

		List<Modifier> mods = LootUtils.getModifiers(toolItem);

		for (Modifier mod : mods) {

			if (mod instanceof UseModifier) {
				if (!Config.traitEnabled(mod.tagName())) {
					continue;
				}
				UseModifier um = (UseModifier) mod;

				if (um.useAnywhere()) {
					um.use(level, player, hand);
				}
			}

		}

		player.awardStat(Stats.ITEM_USED.get(this));

		return InteractionResultHolder.sidedSuccess(toolItem, level.isClientSide());
	}

	private MutableComponent makeComp(String text, ChatFormatting color) {
		MutableComponent comp = Component.empty();
		comp.append(text);
		comp = comp.withStyle(color);

		return comp;
	}

	private void newLine(List<Component> tipList) {
		tipList.add(makeComp("", ChatFormatting.GRAY));
	}

	@Override
	public void appendHoverText(ItemStack item, @Nullable Level level, List<Component> tipList, TooltipFlag flag) {

		boolean show = Screen.hasShiftDown();

		boolean showDescription = Screen.hasControlDown();

		ToolType tt = LootUtils.getToolType(item);

		if (show) {
			tipList.add(makeComp(tt.toString(), ChatFormatting.BLUE));
		}

		MutableComponent desc = makeComp(LootUtils.getItemLore(item), ChatFormatting.GRAY);
		tipList.add(desc);

		if (show) {
			newLine(tipList);
			int itemLevel = LootUtils.getLevel(item);
			tipList.add(makeComp("Level: " + itemLevel, ChatFormatting.GRAY));
			tipList.add(makeComp("XP: " + LootUtils.getXP(item) + " / " + LootUtils.getMaxXP(itemLevel),
					ChatFormatting.GRAY));
		}

		newLine(tipList);

		List<Modifier> mods = LootUtils.getModifiers(item);
		Collections.sort(mods, new Comparator<Modifier>() {
			@Override
			public int compare(final Modifier object1, final Modifier object2) {
				return object1.tagName().compareTo(object2.tagName());
			}
		});

		for (Modifier modifier : mods) {
			if (!Config.traitEnabled(modifier.tagName())) {
				continue;
			}
			modifier.writeToLore(tipList, show);
			if (show) {
				Component details = modifier.writeDetailsToLore(level);

				if (details != null) {
					MutableComponent detailComp = makeComp(" - ", ChatFormatting.GRAY);
					detailComp.append(details);
					tipList.add(detailComp);
				}
			}
			if (showDescription) {
				MutableComponent detailComp = makeComp("", ChatFormatting.GRAY);
				detailComp.append(modifier.description());
				tipList.add(detailComp);
			}
		}

		if (show) {
			newLine(tipList);

			float digSpeed = LootItem.getDigSpeed(item, tt);
			tipList.add(makeComp(String.format("Speed: %.2f", digSpeed), ChatFormatting.GRAY));

			float attackDamage = LootItem.getAttackDamage(item, tt);
			tipList.add(makeComp(String.format("Damage: %.2f", attackDamage), ChatFormatting.GRAY));

		}

		if (!show && !showDescription) {
			newLine(tipList);
			MutableComponent comp = Component.empty();
			comp.append("[Shift for more]");
			comp = comp.withStyle(ChatFormatting.GRAY);
			tipList.add(comp);
			MutableComponent descComp = Component.empty();
			descComp.append("[Ctrl for trait info]");
			descComp = descComp.withStyle(ChatFormatting.GRAY);
			tipList.add(descComp);

		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean holding) {

		if (holding) {

			List<Modifier> mods = LootUtils.getModifiers(stack);

			for (Modifier mod : mods) {

				if (mod instanceof HoldModifier) {
					HoldModifier hodlMod = (HoldModifier) mod;

					hodlMod.hold(stack, level, holder);
				}

			}
		}

	}

}
