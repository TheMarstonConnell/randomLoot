package dev.marston.randomloot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.marston.randomloot.loot.modifiers.Modifier;
import dev.marston.randomloot.loot.modifiers.ModifierRegistry;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GenWiki {

    private static void write(String s, FileWriter f) throws IOException {
        f.write(s + "\n");
    }

    private static String stripItemName(String name) {
        ResourceLocation loc = ResourceLocation.tryParse(name);
        return loc.getPath();
    }

    private static void writeMod(Modifier m, FileWriter f) throws IOException {
        String tag = m.tagName();
        String recipe = "n/a";
        try {
            recipe = readRecipe(tag);
        } catch (Exception e) {
            RandomLoot.LOGGER.warn("failed to find recipe for " + tag + ".");
        }
        write("### " + m.name(), f);

        write("**id:** `" + tag + "` | **crafting:** `" + recipe + "` ![" + stripItemName(recipe)
                + "](https://raw.githubusercontent.com/anish-shanbhag/minecraft-api/master/public/images/items/"
                + stripItemName(recipe) + ".png)", f);
        write("", f);
        write("**Decription:** " + m.description(), f);
    }

    private static void writeMods(Set<Modifier> mods, FileWriter f) throws IOException {
        List<Modifier> sortedList = new ArrayList<>(mods);
        sortedList.sort((o1, o2) -> {
            return o1.tagName().compareTo(o2.name());
        });

        for (Iterator<Modifier> iterator = sortedList.iterator(); iterator.hasNext();) {
            Modifier modifier = iterator.next();
            writeMod(modifier, f);
        }
    }

    private static void writeModifiers(FileWriter f) throws IOException {

        write("# Modifiers", f);
        write("This is a full list of modifiers in the game and a description of what they do.", f);

        write("## Breakers", f);
        write("These effects are applied when breaking blocks.", f);
        writeMods(ModifierRegistry.BREAKERS, f);

        write("## Holders", f);
        write("These effects are applied when holding the tool.", f);
        writeMods(ModifierRegistry.HOLDERS, f);

        write("## Users", f);
        write("These effects are applied when right clicking.", f);
        writeMods(ModifierRegistry.USERS, f);

        write("## Hurters", f);
        write("These effects are applied when hurting enemies.", f);
        writeMods(ModifierRegistry.HURTERS, f);

        write("## Stats", f);
        write("These effects are used to calculate stats for tools.", f);
        writeMods(ModifierRegistry.STATS, f);

        write("## Misc.", f);
        write("These effects are general and don't fit into any other categories.", f);
        writeMods(ModifierRegistry.MISC, f);
    }

    public static void genWiki() {

        String isProdEnv = System.getenv("RL_PROD");
        if (isProdEnv == null) {
            return;
        }

        String isProd = isProdEnv.strip();

        if (isProd.contains("false")) {
            RandomLoot.LOGGER.info("Creating wiki...");

            try {
                FileWriter wikiWriter = new FileWriter("../MODIFIERS.md");
                writeModifiers(wikiWriter);
                wikiWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static String readRecipe(String trait) throws FileNotFoundException {
        FileReader reader = new FileReader(
                "../src/main/resources/data/randomloot/recipe/trait_" + trait + ".json");
        Gson gson = new Gson();
        BufferedReader bufferedReader = new BufferedReader(reader);

        JsonObject obj;
        obj = gson.fromJson(bufferedReader, JsonObject.class);

        JsonElement item = obj.get("item");

        JsonObject itemObj = item.getAsJsonObject();

        String itemName = itemObj.get("id").getAsString();

        return itemName;

    }

}