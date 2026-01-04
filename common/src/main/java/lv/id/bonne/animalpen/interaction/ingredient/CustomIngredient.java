package lv.id.bonne.animalpen.interaction.ingredient;


import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;


/**
 * This class is used to define ingredients either as Item, ItemStack or Tag<Item>. It has merge option that allows to
 * merge multiple ingredients into one object, which is missing in native Ingredient class. It also simplifies item and
 * tag definition using same principles that is adopted in native versions after 1.20.6
 */
public final class CustomIngredient implements Predicate<ItemStack>
{
    private CustomIngredient(Stream<? extends Value> stream)
    {
        this.values = stream.toArray(Value[]::new);
    }


    private CustomIngredient(Value[] values)
    {
        this.values = values;
    }


    public ItemStack[] getItems()
    {
        this.dissolve();
        return this.itemStacks;
    }


    private void dissolve()
    {
        if (this.itemStacks == null)
        {
            this.itemStacks = Arrays.stream(this.values).
                flatMap((value) -> value.getItems().stream()).
                distinct().
                toArray(ItemStack[]::new);
        }
    }


    public boolean test(@Nullable ItemStack itemStack)
    {
        if (itemStack == null)
        {
            return false;
        }
        else
        {
            this.dissolve();

            if (this.itemStacks.length == 0)
            {
                return itemStack.isEmpty();
            }
            else
            {
                return Arrays.stream(this.itemStacks).anyMatch(checkStack -> checkStack.is(itemStack.getItem()));
            }
        }
    }


    public boolean isEmpty()
    {
        return this.values.length == 0 && (this.itemStacks == null || this.itemStacks.length == 0);
    }


    interface Value
    {
        Collection<ItemStack> getItems();


        Codec<Value> CODEC = Codec.STRING.xmap(
            str ->
            {
                if (str.startsWith("#"))
                {
                    return new TagValue(TagKey.create(Registries.ITEM,
                        ResourceLocation.tryParse(str.substring(1))));
                }
                else
                {
                    return new ItemValue(BuiltInRegistries.ITEM.getOptional(ResourceLocation.tryParse(str)).
                        map(Item::getDefaultInstance).
                        orElse(ItemStack.EMPTY));
                }
            },
            value ->
            {
                if (value instanceof TagValue tag)
                {
                    return "#" + tag.tag.location();
                }
                else if (value instanceof ItemValue val)
                {
                    return BuiltInRegistries.ITEM.getKey(val.item.getItem()).toString();
                }

                throw new UnsupportedOperationException("Unknown value type");
            }
        );
    }


    private static CustomIngredient fromValues(Stream<? extends Value> stream)
    {
        CustomIngredient ingredient = new CustomIngredient(stream);
        return ingredient.values.length == 0 ? EMPTY : ingredient;
    }


    public static CustomIngredient of()
    {
        return EMPTY;
    }


    public static CustomIngredient of(ItemLike... itemLikes)
    {
        return of(Arrays.stream(itemLikes).map(ItemStack::new));
    }


    public static CustomIngredient of(ItemStack... itemStacks)
    {
        return of(Arrays.stream(itemStacks));
    }


    public static CustomIngredient of(Stream<ItemStack> stream)
    {
        return fromValues(stream.filter((itemStack) -> !itemStack.isEmpty()).map(ItemValue::new));
    }


    public static CustomIngredient of(TagKey<Item> tagKey)
    {
        return fromValues(Stream.of(new TagValue(tagKey)));
    }


    public static CustomIngredient merge(CustomIngredient... ingredients)
    {
        return fromValues(
            Arrays.stream(ingredients).
                filter(i -> i != null && i != EMPTY).
                flatMap(i -> Arrays.stream(i.values)));
    }


    private static Codec<CustomIngredient> codec()
    {
        Codec<Value[]> codec = Codec.list(Value.CODEC).comapFlatMap(
            list -> DataResult.success(list.toArray(new Value[0])), List::of);

        return codec.xmap(CustomIngredient::new, i -> i.values);
    }


    static class ItemValue implements Value
    {
        ItemValue(ItemStack itemStack)
        {
            this.item = itemStack;
        }


        public Collection<ItemStack> getItems()
        {
            return Collections.singleton(this.item);
        }


        private final ItemStack item;
    }


    static class TagValue implements Value
    {
        TagValue(TagKey<Item> tagKey)
        {
            this.tag = tagKey;
        }


        public Collection<ItemStack> getItems()
        {
            List<ItemStack> list = Lists.newArrayList();

            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag))
            {
                list.add(new ItemStack(holder));
            }

            return list;
        }


        private final TagKey<Item> tag;
    }


    private final Value[] values;

    @Nullable
    private ItemStack[] itemStacks;


    public static final CustomIngredient EMPTY = new CustomIngredient(Stream.empty());


    public static final Codec<CustomIngredient> CODEC = codec();
}
