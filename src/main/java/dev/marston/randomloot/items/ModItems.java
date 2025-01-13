package dev.marston.randomloot.items;

import dev.marston.randomloot.RandomLoot;
import dev.marston.randomloot.component.ModDataComponents;
import dev.marston.randomloot.component.ToolModifier;
import dev.marston.randomloot.loot.LootCase;
import dev.marston.randomloot.loot.LootItem;
import dev.marston.randomloot.loot.ModTemplate;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RandomLoot.MODID);


    public static DeferredItem<Item> TOOL = ITEMS.registerItem("tool", p -> new LootItem(p.component(ModDataComponents.TOOL_MODIFIER.get(), new ToolModifier(new HashMap<>()))));
    public static DeferredItem<Item> CASE = ITEMS.registerItem("case", LootCase::new);
    public static DeferredItem<Item> MOD_ADD = ITEMS.registerItem("mod_add", p -> new ModTemplate(p, true));
    public static DeferredItem<Item> MOD_SUB = ITEMS.registerItem("mod_sub", p -> new ModTemplate(p, false));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(TOOL);
            event.accept(CASE);
        }
    }
}


