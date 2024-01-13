package dev.marston.randomloot.loot.modifiers.holders;

import java.util.List;

import javax.annotation.Nullable;

import dev.marston.randomloot.loot.LootItem.ToolType;
import dev.marston.randomloot.loot.modifiers.HoldModifier;
import dev.marston.randomloot.loot.modifiers.Modifier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Effect implements HoldModifier {

	private String name;
	private float power;
	private String tagname;
	private final static String POWER = "power";
	private MobEffect effect;
	private int duration;

	public Effect(String name, String tagname, int duration, MobEffect effect) {
		this.name = name;
		this.effect = effect;
		this.power = 4.0f;
		this.tagname = tagname;
		this.duration = duration;
	}

	public Modifier clone() {
		return new Effect(this.name, this.tagname, this.duration, this.effect);
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
		return new Effect(tag.getString(NAME), this.tagname, this.duration, this.effect);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String tagName() {
		return tagname;
	}

	@Override
	public String color() {
		int color = effect.getColor();
		ChatFormatting format = ChatFormatting.getById(color);
		if (format == null) {
			return ChatFormatting.LIGHT_PURPLE.getName();
		}
		return format.getName();
	}

	@Override
	public String description() {
		return "While holding the tool, get the " + I18n.get(effect.getDisplayName().getString()).toLowerCase()
				+ " effect.";
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

	@Override
	public void hold(ItemStack stack, Level level, Entity holder) {
		MobEffectInstance eff = new MobEffectInstance(effect, duration * 20, 0, false, false);

		if (!(holder instanceof LivingEntity)) {
			return;
		}

		LivingEntity livingHolder = (LivingEntity) holder;
		boolean alreadyHasEffect = livingHolder.hasEffect(effect);
		if (!alreadyHasEffect) {
			livingHolder.addEffect(eff);
		}

	}

	public boolean canLevel() {
		return false;
	}

	public void levelUp() {
		return;
	}
}
