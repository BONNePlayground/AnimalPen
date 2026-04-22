//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.util;


import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.data.DataProvider;


public class DataOrderInjection
{
    public static void injectCustomOrder()
    {
        Object2IntOpenHashMap<String> map =
            (Object2IntOpenHashMap<String>) DataProvider.FIXED_ORDER_FIELDS;

        map.put("id", 2);
        map.put("items", 3);
        map.put("conditions", 4);
        map.put("even_entity_count", 5);
        map.put("consumer", 6);
        map.put("loot", 7);
        map.put("cooldown", 8);
        map.put("run_functions", 9);
        map.put("finish_functions", 10);
        map.put("text_lines", 11);
        map.put("sound", 12);
        map.put("redstone_signal", 13);
        map.put("entity", 14);
        map.put("required_mods", 15);
        map.put("interactions", 16);
        map.put("key", 17);
        map.put("operator", 18);
        map.put("value", 19);
        map.put("drop_limit", 20);
        map.put("per_entity", 21);
        map.put("min", 22);
        map.put("max", 23);
        map.put("base", 24);
        map.put("delta", 25);
        map.put("limit", 26);
        map.put("short_text", 27);
        map.put("long_text", 28);
        map.put("main_item", 29);
        map.put("result_item", 30);
        map.put("visibility", 31);
        map.put("parameters", 32);
    }


    public static void removeCustomOrder()
    {
        Object2IntOpenHashMap<String> map =
            (Object2IntOpenHashMap<String>) DataProvider.FIXED_ORDER_FIELDS;

        map.remove("id", 2);
        map.remove("items", 3);
        map.remove("conditions", 4);
        map.remove("even_entity_count", 5);
        map.remove("consumer", 6);
        map.remove("loot", 7);
        map.remove("cooldown", 8);
        map.remove("run_functions", 9);
        map.remove("finish_functions", 10);
        map.remove("text_lines", 11);
        map.remove("sound", 12);
        map.remove("redstone_signal", 13);
        map.remove("entity", 14);
        map.remove("required_mods", 15);
        map.remove("interactions", 16);
        map.remove("key", 17);
        map.remove("operator", 18);
        map.remove("value", 19);
        map.remove("drop_limit", 20);
        map.remove("per_entity", 21);
        map.remove("min", 22);
        map.remove("max", 23);
        map.remove("base", 24);
        map.remove("delta", 25);
        map.remove("limit", 26);
        map.remove("short_text", 27);
        map.remove("long_text", 28);
        map.remove("main_item", 29);
        map.remove("result_item", 30);
        map.remove("visibility", 31);
        map.remove("parameters", 32);
    }
}
