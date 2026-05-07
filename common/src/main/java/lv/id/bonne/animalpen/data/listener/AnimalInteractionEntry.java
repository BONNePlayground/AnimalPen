package lv.id.bonne.animalpen.data.listener;


import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;


/**
 * This record allows to encode and decode Animal Interactions from `JSON` files.
 * @param entityType - the entity type that is adding interaction.
 * @param requiredMods - the list of required mods for these interactions to load.
 * @param interactions - the list of loaded interactions.
 */
public record AnimalInteractionEntry(Optional<ResourceKey<EntityType<?>>> entityType,
                                     List<String> requiredMods,
                                     List<AnimalInteraction> interactions)
{
    public boolean isActive()
    {
        return this.entityType.isPresent() && !this.interactions.isEmpty();
    }


    public static AnimalInteractionEntry of(ResourceKey<EntityType<?>> entityType, List<AnimalInteraction> animalInteractions)
    {
        return new AnimalInteractionEntry(Optional.of(entityType), Collections.emptyList(), animalInteractions);
    }


    /**
     * Just empty entry for failures.
     */
    private final static AnimalInteractionEntry EMPTY = new AnimalInteractionEntry(Optional.empty(),
        Collections.emptyList(),
        Collections.emptyList());


    public static final Codec<ResourceKey<EntityType<?>>> ENTITY_KEY_CODEC =
        ResourceKey.codec(Registries.ENTITY_TYPE);


    /**
     * Custom decoding to ensure that it parses entries only if required mod is present to avoid
     * errors on loading.
     */
    public static final Codec<AnimalInteractionEntry> CODEC = new Codec<>()
    {
        @Override
        public <T> DataResult<Pair<AnimalInteractionEntry, T>> decode(DynamicOps<T> ops, T input)
        {
            return ops.getMap(input).flatMap(map ->
            {
                // Validate mods
                List<String> requiredMods = map.get("required_mods") != null ?
                    Codec.STRING.listOf().parse(ops, map.get("required_mods")).result().orElse(List.of()) :
                    List.of();

                boolean modsPresent = requiredMods.stream().allMatch(Services.PLATFORM::isModLoaded);

                if (!modsPresent)
                {
                    return DataResult.success(Pair.of(AnimalInteractionEntry.EMPTY, input));
                }

                // Now load everything
                DataResult<ResourceKey<EntityType<?>>> entityResult =
                    ENTITY_KEY_CODEC.parse(ops, map.get("entity"));

                DataResult<List<AnimalInteraction>> interactionsResult = map.get("interactions") != null ?
                    AnimalInteraction.CODEC.listOf().parse(ops, map.get("interactions")) :
                    DataResult.success(List.of());

                return entityResult.flatMap(entity ->
                    interactionsResult.map(interactions ->
                        Pair.of(new AnimalInteractionEntry(Optional.of(entity),
                            requiredMods, interactions),
                            input))
                );
            });
        }


        @Override
        public <T> DataResult<T> encode(AnimalInteractionEntry value,
            DynamicOps<T> ops,
            T prefix)
        {
            RecordBuilder<T> builder = ops.mapBuilder();

            if (value.entityType().isEmpty())
            {
                return DataResult.error(() -> "Cannot encode given value as entity type is missing: " + prefix.toString());
            }

            if (!value.requiredMods().isEmpty())
            {
                builder.add("required_mods", Codec.STRING.listOf().encodeStart(ops, value.requiredMods()));
            }

            builder.add("entity", ENTITY_KEY_CODEC.encodeStart(ops, value.entityType().get()));
            builder.add("interactions", AnimalInteraction.CODEC.listOf().encodeStart(ops, value.interactions()));

            return builder.build(prefix);
        }
    };
}