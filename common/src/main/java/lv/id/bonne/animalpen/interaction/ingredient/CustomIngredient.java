package lv.id.bonne.animalpen.interaction.ingredient;


import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
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
                return Arrays.stream(this.itemStacks).anyMatch(checkStack -> isSameIgnoreDurability(checkStack, itemStack));
            }
        }
    }


    public static boolean isSameIgnoreDurability(ItemStack stack1, ItemStack stack2)
    {
        if (stack1 == stack2) return true;
        if (stack1.isEmpty() || stack2.isEmpty()) return false;
        if (!stack1.is(stack2.getItem())) return false;
        if (!stack1.hasTag()) return true;
        if (!stack1.hasTag() && !stack2.hasTag()) return true;

        // Copy tags so we don't accidentally repair the item in-game!
        CompoundTag nbt1 = stack1.getTag() != null ? stack1.getTag().copy() : null;
        CompoundTag nbt2 = stack2.getTag() != null ? stack2.getTag().copy() : null;

        // Explicitly strip away ONLY the durability tag
        if (nbt1 != null) nbt1.remove(ItemStack.TAG_DAMAGE);
        if (nbt2 != null) nbt2.remove(ItemStack.TAG_DAMAGE);

        return nbt1 != null && nbt1.isEmpty() || Objects.equals(nbt1, nbt2);
    }


    public boolean isEmpty()
    {
        return this.values.length == 0 && (this.itemStacks == null || this.itemStacks.length == 0);
    }


    interface Value
    {
        Collection<ItemStack> getItems();

        String TAG_PREFIX = "#";

        Codec<Value> CODEC = Codec.either(
            Codec.STRING.comapFlatMap(
                str -> {
                    if (str.startsWith(TAG_PREFIX))
                    {
                        return DataResult.success(new TagValue(TagKey.create(Registries.ITEM,
                            ResourceLocation.tryParse(str.substring(TAG_PREFIX.length())))));
                    }
                    else
                    {
                        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.tryParse(str)).
                            map(item -> DataResult.success((Value) new ItemValue(item.getDefaultInstance()))).
                            orElseGet(() -> DataResult.error(() -> "Unknown item: " + str + " - was it spelled correctly?"));
                    }
                },
                value -> {
                    if (value instanceof TagValue tag)
                    {
                        return TAG_PREFIX + tag.tag.location();
                    }

                    if (value instanceof ItemValue item)
                    {
                        return BuiltInRegistries.ITEM.getKey(item.item.getItem()).toString();
                    }

                    throw new UnsupportedOperationException("Unknown Value type: " + value.getClass());
                }
            ),
            ItemStack.CODEC.xmap(ItemValue::new, v -> v.item)
        ).xmap(
            either -> either.map(l -> l, r -> r),
            value -> {
                if (value instanceof TagValue)
                {
                    return Either.left(value);
                }

                ItemValue item = (ItemValue) value;

                if (item.item.getCount() == 1 && !item.item.hasTag())
                {
                    return Either.left(value);
                }

                CompoundTag tag = item.item.getTag();

                if (tag != null)
                {
                    tag.remove(ItemStack.TAG_DAMAGE);

                    if (tag.isEmpty())
                    {
                        // Vanilla items adds damage by default. If tag is empty do not save it.
                        return Either.left(value);
                    }
                }

                return Either.right(item);
            }
        );

        Codec<Value> STREAM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("is_tag").forGetter(v -> v instanceof TagValue),
            ResourceLocation.CODEC.optionalFieldOf("tag").forGetter(v -> {
                if (v instanceof TagValue tag)
                {
                    return Optional.of(tag.tag.location());
                }

                return Optional.empty();
            }),
            ItemStack.CODEC.optionalFieldOf("stack").forGetter(v -> {
                if (v instanceof ItemValue item)
                {
                    return Optional.of(item.item);
                }

                return Optional.empty();
            })
        ).apply(instance, (isTag, tag, stack) -> {
            if (isTag)
            {
                return new TagValue(TagKey.create(Registries.ITEM, tag.orElseThrow()));
            }

            return new ItemValue(stack.orElse(ItemStack.EMPTY));
        }));
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


    private static Codec<CustomIngredient> codec(Codec<Value> valueCodec)
    {
        return Codec.list(valueCodec).comapFlatMap(
            list -> DataResult.success(list.toArray(new Value[0])),
            List::of
        ).xmap(CustomIngredient::new, i -> i.values);
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


    public static final Codec<CustomIngredient> CODEC = codec(Value.CODEC);

    public static final Codec<CustomIngredient> STREAM_CODEC = codec(Value.STREAM_CODEC);
}
