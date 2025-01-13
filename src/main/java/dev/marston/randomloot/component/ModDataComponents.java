package dev.marston.randomloot.component;

import com.mojang.serialization.Codec;
import dev.marston.randomloot.RandomLoot;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.UnaryOperator;


public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, RandomLoot.MODID);
    public static final Codec<List<ToolModifier>> LIST_CODEC = ToolModifier.CODEC.listOf();

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ToolModifier>> TOOL_MODIFIER = register("tool_mod",
            builder -> builder.persistent(ToolModifier.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ToolModifier>>> TOOL_MODIFIERS = register("tool_mods",
            builder -> builder.persistent(LIST_CODEC));

    private static <T>DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name,
                                                                                          UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }


    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
