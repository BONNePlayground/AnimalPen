package lv.id.bonne.animalpen.platform.services;


import java.util.List;
import java.util.function.Supplier;

import lv.id.bonne.animalpen.processing.function.wrapper.EntityFunctionEntry;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;


public interface IRegistryHelper
{
    /**
     * Registers a Block using the given identifier and supplier.
     *
     * @param name The identifier for the block registration.
     * @param supplier A supplier that provides the actual {@link Block} instance.
     * @param <T> The type extending {@link Block}.
     * @return A {@link Supplier} providing the registered block.
     */
    <T extends Block> Supplier<T> registerBlock(Identifier name, Supplier<T> supplier);

    /**
     * Registers an Item using the given identifier and supplier.
     *
     * @param name The identifier for the item registration.
     * @param supplier A supplier that provides the actual {@link Item} instance.
     * @param <T> The type extending {@link Item}.
     * @return A {@link Supplier} providing the registered item.
     */
    <T extends Item> Supplier<T> registerItem(Identifier name, Supplier<T> supplier);

    /**
     * Registers a BlockEntityType using the given identifier and supplier.
     *
     * @param name The identifier for the block entity type registration.
     * @param supplier A supplier that provides the actual {@link BlockEntityType} instance.
     * @param <T> The type extending {@link BlockEntity}.
     * @return A {@link Supplier} providing the registered block entity type.
     */
    <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntityType(
        Identifier name,
        Supplier<BlockEntityType<T>> supplier
    );

    /**
     * Registers an EntityFunctionEntry using the given identifier and supplier.
     *
     * @param name The identifier for the function registration.
     * @param supplier A supplier that provides the actual {@link EntityFunctionEntry} instance.
     * @param <T> The type extending {@link EntityFunctionEntry}.
     * @return A {@link Supplier} providing the registered function entry.
     */
    <T extends EntityFunctionEntry> Supplier<T> registerFunction(Identifier name, Supplier<T> supplier);

    /**
     * Registers a DataComponentType using the given identifier and supplier.
     *
     * @param name The identifier for the data component type registration.
     * @param supplier A supplier that provides the actual {@link DataComponentType} instance.
     * @param <T> The type extending {@link DataComponentType}.
     * @return A {@link Supplier} providing the registered data component.
     */
    <T extends DataComponentType<?>> Supplier<T> registerDataComponent(Identifier name, Supplier<T> supplier);

    /**
     * Registers a CriterionTrigger using the given identifier and supplier.
     *
     * @param name The identifier for the trigger registration.
     * @param supplier A supplier that provides the actual {@link CriterionTrigger} instance.
     * @param <T> The type extending {@link CriterionTrigger}.
     * @return A {@link Supplier} providing the registered trigger.
     */
    <T extends CriterionTrigger<?>> Supplier<T> registerTrigger(Identifier name, Supplier<T> supplier);

    /**
     * Registers a CreativeModeTab using the given identifier and supplier.
     *
     * @param name The identifier for the creative mode tab registration.
     * @param supplier A supplier that provides the actual {@link CreativeModeTab} instance.
     * @param <T> The type extending {@link CreativeModeTab}.
     * @return A {@link Supplier} providing the registered creative tab.
     */
    <T extends CreativeModeTab> Supplier<T> registerCreativeTab(Identifier name, Supplier<T> supplier);

    /**
     * Retrieves an {@link EntityFunctionEntry} by its unique identifier.
     *
     * @param id The identifier of the function to retrieve.
     * @return The associated {@link EntityFunctionEntry}.
     */
    EntityFunctionEntry getFunctionValue(Identifier id);

    /**
     * Retrieves the unique identifier key associated with a given {@link EntityFunctionEntry}.
     *
     * @param entry The function entry whose key is needed.
     * @return The {@link Identifier} key for the entry.
     */
    Identifier getFunctionKey(EntityFunctionEntry entry);

    /**
     * Retrieves a list of all items that have been registered through this helper.
     *
     * @return A {@link List} of registered {@link Item} instances.
     */
    List<Item> getRegisterItems();

    /**
     * Creates and retrieves a Minecraft {@link Registry} instance of the specified type,
     * ensuring it is correctly initialized with the given key.
     *
     * @param registryKey The {@link ResourceKey} defining the registry type and location.
     * @param <T> The type of the registry contents.
     * @return A fully functional {@link Registry<T>} instance.
     */
    <T> Registry<T> createRegistry(ResourceKey<Registry<T>> registryKey);
}