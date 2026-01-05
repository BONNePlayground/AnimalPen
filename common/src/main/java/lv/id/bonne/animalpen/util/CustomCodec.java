//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.util;


import com.mojang.serialization.*;
import java.util.List;
import java.util.stream.Stream;

import lv.id.bonne.animalpen.AnimalPen;


/**
 * This class contains custom generic codecs.
 */
public class CustomCodec
{
    /**
     * This codec does similar function to Codec.listOf().optionalFiled(), however, it requires
     * that if object exists it should be valid, without errors, unlike default one that maps to empty list on failure.
     */
    public static <T> MapCodec<List<T>> strictOptionalListField(String fieldName, Codec<T> elementCodec) {
        return new MapCodec<>()
        {
            @Override
            public <T1> RecordBuilder<T1> encode(List<T> input, DynamicOps<T1> ops, RecordBuilder<T1> prefix)
            {
                return input.isEmpty() ?
                    prefix : prefix.add(fieldName, elementCodec.listOf().encodeStart(ops, input));
            }


            @Override
            public <T1> DataResult<List<T>> decode(DynamicOps<T1> ops, MapLike<T1> input)
            {
                T1 value = input.get(fieldName);

                if (value == null)
                {
                    return DataResult.success(List.of());
                }

                DataResult<List<T>> result = elementCodec.listOf().parse(ops, value);

                return result.error().isPresent() ?
                    DataResult.error(() -> "Error in " + fieldName + ": " + result.error().get().message()) : result;
            }


            @Override
            public <T1> Stream<T1> keys(DynamicOps<T1> ops)
            {
                return Stream.of(ops.createString(fieldName));
            }
        };
    }
}
