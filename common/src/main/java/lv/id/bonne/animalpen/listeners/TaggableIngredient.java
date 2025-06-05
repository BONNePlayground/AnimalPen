package lv.id.bonne.animalpen.listeners;


import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.architectury.registry.registries.RegistrarManager;
import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


public class TaggableIngredient implements Predicate<ItemStack>
{
    private TaggableIngredient(Value[] values)
    {
        this.values = values;
    }


    public HolderSet<Item> getItems()
    {
        if (this.items == null)
        {
            this.items = HolderSet.direct(Arrays.stream(this.values).
                flatMap(value -> value.getItems().stream()).
                distinct().
                toList());
        }

        return this.items;
    }


    public ItemStack[] getItemStacks()
    {
        if (this.itemStacks == null)
        {
            this.itemStacks = this.getItems().stream().
                filter(Holder::isBound).
                map(holder -> holder.value().getDefaultInstance()).
                toArray(ItemStack[]::new);
        }

        return this.itemStacks;
    }


    public boolean test(@Nullable ItemStack itemStack)
    {
        if (itemStack == null)
        {
            return false;
        }

        if (this.isEmpty())
        {
            return itemStack.isEmpty();
        }

        return itemStack.is(this.getItems());
    }


    public boolean isEmpty()
    {
        return this.values.length == 0;
    }


    public boolean equals(Object object)
    {
        return object instanceof TaggableIngredient ingredient &&
            Arrays.equals(this.values, ingredient.values);
    }


    interface Value
    {
        Collection<Holder<Item>> getItems();


        Codec<Value> CODEC = Codec.STRING.xmap(
            str ->
            {
                if (str.startsWith("#"))
                {
                    return new TagValue(TagKey.create(Registries.ITEM, ResourceLocation.parse(str.substring(1))));
                }
                else
                {
                    return new ItemValue(RegistrarManager.get(AnimalPen.MOD_ID).get(Registries.ITEM).getHolder(ResourceLocation.parse(str)));
                }
            },
            value ->
            {
                if (value instanceof TagValue(TagKey<Item> tag))
                {
                    return "#" + tag.location();
                }
                else if (value instanceof ItemValue(Holder<Item> item))
                {
                    return item.getRegisteredName();
                }

                throw new UnsupportedOperationException("Unknown value type");
            }
        );
    }


    private static Codec<TaggableIngredient> codec()
    {
        Codec<Value[]> codec = Codec.list(Value.CODEC).comapFlatMap(
            list -> DataResult.success(list.toArray(new Value[0])), List::of
        );

        return codec.xmap(TaggableIngredient::new, ingredient -> ingredient.values);
    }


    record ItemValue(Holder<Item> item) implements Value
    {
        public Collection<Holder<Item>> getItems()
        {
            return Collections.singleton(this.item);
        }
    }


    public record TagValue(TagKey<Item> tag) implements Value
    {
        public Collection<Holder<Item>> getItems()
        {
            List<Holder<Item>> items = new ArrayList<>();

            BuiltInRegistries.ITEM.getTagOrEmpty(this.tag).forEach(items::add);

            return items;
        }
    }


    private final Value[] values;

    @Nullable
    private HolderSet<Item> items;

    @Nullable
    private ItemStack[] itemStacks;

    public static final Codec<TaggableIngredient> CODEC = codec();

    private static TaggableIngredient fromValues(Stream<? extends Value> stream) {
        return new TaggableIngredient(stream.toArray(Value[]::new));
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, TaggableIngredient> CONTENTS_STREAM_CODEC =
        ItemStack.OPTIONAL_LIST_STREAM_CODEC.map(
            list -> fromValues(list.stream().map(ItemStack::getItemHolder).map(ItemValue::new)),
            ingredient -> Arrays.stream(ingredient.values).
                flatMap(value -> value.getItems().stream()).
                map(Holder::value).
                map(Item::getDefaultInstance).
                toList());
}