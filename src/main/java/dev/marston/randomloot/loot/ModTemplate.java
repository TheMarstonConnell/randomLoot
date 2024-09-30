package dev.marston.randomloot.loot;

import dev.marston.randomloot.loot.modifiers.Modifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ModTemplate extends Item {

	final boolean add;

	public ModTemplate(boolean additional) {
		super(new Properties().stacksTo(1));
		this.add = additional;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack template = player.getItemInHand(hand);

		Modifier.TrackEntityParticle(level, player, ParticleTypes.CLOUD);

		if (!level.isClientSide) {

			ItemStack s;

			if (this.add) {
				s = new ItemStack(LootRegistry.ModSub);
			} else {
				s = new ItemStack(LootRegistry.ModAdd);
			}

			player.setItemInHand(hand, s);
		}

		return InteractionResultHolder.sidedSuccess(template, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tipList, TooltipFlag flag) {

		MutableComponent comp = Component.empty();
		comp.append("Right-click to change function");
		comp = comp.withStyle(ChatFormatting.GRAY);

		tipList.add(comp);

	}

}
