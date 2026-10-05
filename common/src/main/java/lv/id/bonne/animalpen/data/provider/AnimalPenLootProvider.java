//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.data.provider;


import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import lv.id.bonne.animalpen.registries.AnimalPenLootTables;
import lv.id.bonne.animalpen.util.AnimalPenItemHelper;
import net.minecraft.advancements.predicates.NbtPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.entity.EntityTypePredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.frog.FrogVariants;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;


public class AnimalPenLootProvider
{
    public static void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output,
        HolderLookup.Provider registries)
    {
        output.accept(AnimalPenLootTables.MUSHROOM_STEW,
            LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1)).
                add(LootItem.lootTableItem(Items.MUSHROOM_STEW))));

        HolderGetter<EntityType<?>> entityLookup = registries.lookupOrThrow(Registries.ENTITY_TYPE);
        LootPool.Builder poolBuilder = LootPool.lootPool();
        poolBuilder.setRolls(ContextIntProviders.exactly(1));

        // Blocks are a static registry, so iterate it directly instead of via the HolderLookup.Provider.
        BuiltInRegistries.BLOCK.stream().
            filter(block -> block instanceof FlowerBlock).
            map(block -> ((FlowerBlock) block)).
            map(FlowerBlock::getSuspiciousEffects).
            forEach(effect ->
            {
                CompoundTag tag = new CompoundTag();
                tag.put("stew_effects", SuspiciousStewEffects.CODEC.encodeStart(NbtOps.INSTANCE, effect).getOrThrow());

                poolBuilder.add(LootItem.lootTableItem(Items.SUSPICIOUS_STEW).
                    when(LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.entity().
                            entityType(EntityTypePredicate.of(entityLookup, EntityTypes.MOOSHROOM)).
                            components(DataComponentExactPredicate.expect(DataComponents.MOOSHROOM_VARIANT, MushroomCow.Variant.BROWN)).
                            nbt(new NbtPredicate(tag))
                    )).
                    apply(SetComponentsFunction.setComponent(DataComponents.SUSPICIOUS_STEW_EFFECTS, effect)));
            });

        output.accept(AnimalPenLootTables.SUSPICIOUS_STEW, LootTable.lootTable().withPool(poolBuilder));

        output.accept(AnimalPenLootTables.ARMADILLO_SCUTE,
            LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1)).
                add(LootItem.lootTableItem(Items.ARMADILLO_SCUTE))));

        output.accept(AnimalPenLootTables.MILK_BUCKET,
            LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1)).
                add(LootItem.lootTableItem(Items.MILK_BUCKET))));
        output.accept(AnimalPenLootTables.SNIFFER_EGG,
            LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1)).
                add(LootItem.lootTableItem(Items.SNIFFER_EGG))));
        output.accept(AnimalPenLootTables.TURTLE_EGG,
            LootTable.lootTable().withPool(LootPool.lootPool().
                // UniformGenerator now lives in ...providers.number.ints
                    setRolls(ContextIntProviders.between(1, 4)).
                add(LootItem.lootTableItem(Items.TURTLE_EGG))));

        output.accept(AnimalPenLootTables.HONEY_BOTTLE,
            LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1)).
                add(LootItem.lootTableItem(Items.HONEY_BOTTLE))));

        HolderGetter<FrogVariant> frogVariants = registries.lookupOrThrow(Registries.FROG_VARIANT);
        output.accept(AnimalPenLootTables.FROGLIGHT,
            LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1)).
                add(AlternativesEntry.alternatives(
                    LootItem.lootTableItem(Items.OCHRE_FROGLIGHT).
                        when(LootItemEntityPropertyCondition.hasProperties(
                            LootContext.EntityTarget.THIS,
                            EntityPredicate.Builder.entity().
                                components(DataComponentExactPredicate.expect(DataComponents.FROG_VARIANT,
                                    frogVariants.getOrThrow(FrogVariants.TEMPERATE))))),
                    LootItem.lootTableItem(Items.PEARLESCENT_FROGLIGHT).
                        when(LootItemEntityPropertyCondition.hasProperties(
                            LootContext.EntityTarget.THIS,
                            EntityPredicate.Builder.entity().
                                components(DataComponentExactPredicate.expect(DataComponents.FROG_VARIANT,
                                    frogVariants.getOrThrow(FrogVariants.WARM))))),
                    LootItem.lootTableItem(Items.VERDANT_FROGLIGHT).
                        when(LootItemEntityPropertyCondition.hasProperties(
                            LootContext.EntityTarget.THIS,
                            EntityPredicate.Builder.entity().
                                components(DataComponentExactPredicate.expect(DataComponents.FROG_VARIANT,
                                    frogVariants.getOrThrow(FrogVariants.COLD))))))
                )
            )
        );

        output.accept(AnimalPenLootTables.HONEYCOMB,
            LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(3)).
                add(LootItem.lootTableItem(Items.HONEYCOMB))));


        // LootPoolSingletonContainer.Builder -> UniformContainerBase.Builder
        List<UniformContainerBase.Builder<?>> lootEntries = new ArrayList<>();

        for (DyeColor value : DyeColor.values())
        {
            lootEntries.add(LootItem.lootTableItem(AnimalPenItemHelper.ITEM_BY_DYE.get(value)).
                when(LootItemEntityPropertyCondition.hasProperties(
                    LootContext.EntityTarget.THIS,
                    EntityPredicate.Builder.entity().
                        components(DataComponentExactPredicate.expect(DataComponents.SHEEP_COLOR,
                            value)))));
        }

        output.accept(AnimalPenLootTables.WOOL,
            LootTable.lootTable().withPool(
                LootPool.lootPool().
                    setRolls(ContextIntProviders.between(1, 3)).
                    add(AlternativesEntry.alternatives(lootEntries.toArray(new UniformContainerBase.Builder[0])))
            )
        );
    }
}

