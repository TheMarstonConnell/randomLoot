package dev.marston.randomloot.loot.modifiers.users;

import dev.marston.randomloot.loot.LootItem.ToolType;
import dev.marston.randomloot.loot.NameGenerator;
import dev.marston.randomloot.loot.modifiers.Modifier;
import dev.marston.randomloot.loot.modifiers.ModifierRegistry;
import dev.marston.randomloot.loot.modifiers.UseModifier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;
import java.util.List;

public class DirtPlace implements UseModifier {
	private String name;
	private int damage;
	private static final String DAMAGE = "DAMAGE";

	public DirtPlace(String name, int damage) {
		this.name = name;
		this.damage = damage;
	}

	public DirtPlace() {
		this.name = NameGenerator.generateForger(0.5f) + "'s Grace";
		this.damage = 1;
	}

	public Modifier clone() {
		return new DirtPlace();
	}

	@Override
	public CompoundTag toNBT() {

		CompoundTag tag = new CompoundTag();

		tag.putString(NAME, name);
		tag.putInt(DAMAGE, damage);

		return tag;
	}

	@Override
	public Modifier fromNBT(CompoundTag tag) {
		return new DirtPlace(tag.getString(NAME), tag.getInt(DAMAGE));
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String tagName() {
		return "dirt_place";
	}

	@Override
	public String color() {
		return "dark_green";
	}

	private boolean canPlace(BlockPlaceContext p_40611_, BlockState p_40612_) {
		Player player = p_40611_.getPlayer();
		CollisionContext collisioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
		return (p_40612_.canSurvive(p_40611_.getLevel(), p_40611_.getClickedPos()))
				&& p_40611_.getLevel().isUnobstructed(p_40612_, p_40611_.getClickedPos(), collisioncontext);
	}

	private BlockState getPlacementState(BlockPlaceContext p_40613_) {
		BlockState blockstate = Blocks.DIRT.getStateForPlacement(p_40613_);
		return blockstate != null && canPlace(p_40613_, blockstate) ? blockstate : null;
	}

	private boolean placeBlock(BlockPlaceContext p_40578_, BlockState p_40579_) {
		return p_40578_.getLevel().setBlock(p_40578_.getClickedPos(), p_40579_, 11);
	}

	private BlockState updateBlockStateFromTag(BlockPos p_40603_, Level p_40604_, ItemStack p_40605_,
			BlockState p_40606_) {
		BlockState blockstate = p_40606_;
		CompoundTag compoundtag = p_40605_.getTag();
		if (compoundtag != null) {
			CompoundTag compoundtag1 = compoundtag.getCompound("BlockStateTag");
			StateDefinition<Block, BlockState> statedefinition = p_40606_.getBlock().getStateDefinition();

			for (String s : compoundtag1.getAllKeys()) {
				Property<?> property = statedefinition.getProperty(s);
				if (property != null) {
					String s1 = compoundtag1.get(s).getAsString();
					blockstate = updateState(blockstate, property, s1);
				}
			}
		}

		if (blockstate != p_40606_) {
			p_40604_.setBlock(p_40603_, blockstate, 2);
		}

		return blockstate;
	}

	private static <T extends Comparable<T>> BlockState updateState(BlockState p_40594_, Property<T> p_40595_,
			String p_40596_) {
		return p_40595_.getValue(p_40596_).map((p_40592_) -> {
			return p_40594_.setValue(p_40595_, p_40592_);
		}).orElse(p_40594_);
	}

	@Nullable
	private static CompoundTag getBlockEntityData(ItemStack p_186337_) {
		return p_186337_.getTagElement("BlockEntityTag");
	}

	private static boolean updateCustomBlockEntityTag(Level p_40583_, @Nullable Player p_40584_, BlockPos p_40585_,
			ItemStack p_40586_) {
		MinecraftServer minecraftserver = p_40583_.getServer();
		if (minecraftserver == null) {
			return false;
		} else {
			CompoundTag compoundtag = getBlockEntityData(p_40586_);
			if (compoundtag != null) {
				BlockEntity blockentity = p_40583_.getBlockEntity(p_40585_);
				if (blockentity != null) {
					if (!p_40583_.isClientSide && blockentity.onlyOpCanSetNbt()
							&& (p_40584_ == null || !p_40584_.canUseGameMasterBlocks())) {
						return false;
					}

					CompoundTag compoundtag1 = blockentity.saveWithoutMetadata();
					CompoundTag compoundtag2 = compoundtag1.copy();
					compoundtag1.merge(compoundtag);
					if (!compoundtag1.equals(compoundtag2)) {
						blockentity.load(compoundtag1);
						blockentity.setChanged();
						return true;
					}
				}
			}

			return false;
		}
	}

	private boolean updateCustomBlockEntityTag(BlockPos p_40597_, Level p_40598_, @Nullable Player p_40599_,
			ItemStack p_40600_, BlockState p_40601_) {
		return updateCustomBlockEntityTag(p_40598_, p_40599_, p_40597_, p_40600_);
	}

	// Forge: Sensitive version of BlockItem#getPlaceSound
	protected SoundEvent getPlaceSound(BlockState state, Level world, BlockPos pos, Player entity) {
		return state.getSoundType(world, pos, entity).getPlaceSound();
	}

	private InteractionResult place(BlockPlaceContext p_40577_) {
		if (!p_40577_.canPlace()) {
			return InteractionResult.FAIL;
		} else {
			BlockPlaceContext blockplacecontext = p_40577_;
			BlockState blockstate = this.getPlacementState(blockplacecontext);
			if (blockstate == null) {
				return InteractionResult.FAIL;
			} else if (!this.placeBlock(blockplacecontext, blockstate)) {
				return InteractionResult.FAIL;
			} else {
				BlockPos blockpos = blockplacecontext.getClickedPos();
				Level level = blockplacecontext.getLevel();
				Player player = blockplacecontext.getPlayer();
				ItemStack itemstack = blockplacecontext.getItemInHand();
				BlockState blockstate1 = level.getBlockState(blockpos);
				if (blockstate1.is(blockstate.getBlock())) {
					blockstate1 = this.updateBlockStateFromTag(blockpos, level, itemstack, blockstate1);
					this.updateCustomBlockEntityTag(blockpos, level, player, itemstack, blockstate1);
					blockstate1.getBlock().setPlacedBy(level, blockpos, blockstate1, player, itemstack);
					if (player instanceof ServerPlayer) {
						CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, blockpos, itemstack);
					}
				}

				SoundType soundtype = blockstate1.getSoundType(level, blockpos, p_40577_.getPlayer());
				level.playSound(player, blockpos,
						this.getPlaceSound(blockstate1, level, blockpos, p_40577_.getPlayer()), SoundSource.BLOCKS,
						(soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(player, blockstate1));

				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}
	}

	@Override
	public void use(UseOnContext ctx) {

		BlockPlaceContext bctx = new BlockPlaceContext(ctx);

		InteractionResult placed = place(bctx);
		if (placed != InteractionResult.FAIL) {
			ctx.getItemInHand().hurtAndBreak(this.damage, ctx.getPlayer(), (event) -> {
				event.broadcastBreakEvent(EquipmentSlot.MAINHAND);
			});
		}

	}

	@Override
	public String description() {
		return "Right clicking on the top of a block with the tool in hand will place a dirt block and use "
				+ this.damage + " durability points.";
	}

	@Override
	public void writeToLore(List<Component> list, boolean shift) {

		MutableComponent comp = Modifier.makeComp(this.name(), this.color());

		list.add(comp);

	}

	@Override
	public Component writeDetailsToLore() {

		return null;

	}

	@Override
	public boolean compatible(Modifier mod) {
		return !ModifierRegistry.USERS.contains(mod);
	}

	@Override
	public boolean forTool(ToolType type) {
		return true;
	}

	@Override
	public void use(Level level, Player player, InteractionHand hand) {
		return;
	}

	@Override
	public boolean useAnywhere() {
		return false;
	}

	public boolean canLevel() {
		return false;
	}

	public void levelUp() {
		return;
	}
}
