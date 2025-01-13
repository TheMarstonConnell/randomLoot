package dev.marston.randomloot.loot.modifiers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface EntityHurtModifier extends Modifier {
	public boolean hurtEnemy(ItemStack itemstack, LivingEntity hurtee, LivingEntity hurter);
}
