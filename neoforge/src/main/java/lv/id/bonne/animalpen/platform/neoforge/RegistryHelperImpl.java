package lv.id.bonne.animalpen.platform.neoforge;


import java.util.List;
import java.util.function.Supplier;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.platform.services.IRegistryHelper;
import lv.id.bonne.animalpen.processing.function.wrapper.EntityFunctionEntry;
import lv.id.bonne.animalpen.registries.AnimalPenRegistry;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;


public class RegistryHelperImpl implements IRegistryHelper
{
    @Override
    public <T extends Block> Supplier<T> registerBlock(Identifier name, Supplier<T> supplier)
    {
        return BLOCKS.register(name.getPath(), supplier);
    }


    @Override
    public <T extends Item> Supplier<T> registerItem(Identifier name, Supplier<T> supplier)
    {
        return ITEMS.register(name.getPath(), supplier);
    }


    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntityType(Identifier name,
        Supplier<BlockEntityType<T>> supplier)
    {
        return BLOCK_ENTITY_TYPES.register(name.getPath(), supplier);
    }


    @Override
    public <T extends EntityFunctionEntry> Supplier<T> registerFunction(Identifier name, Supplier<T> supplier)
    {
        return ENTITY_FUNCTIONS.register(name.getPath(), supplier);
    }


    @Override
    public <T extends DataComponentType<?>> Supplier<T> registerDataComponent(Identifier name, Supplier<T> supplier)
    {
        return DATA_COMPONENTS.register(name.getPath(), supplier);
    }


    @Override
    public <T extends CriterionTrigger<?>> Supplier<T> registerTrigger(Identifier name, Supplier<T> supplier)
    {
        return CRITERION_TRIGGERS.register(name.getPath(), supplier);
    }


    @Override
    public <T extends CreativeModeTab> Supplier<T> registerCreativeTab(Identifier name, Supplier<T> supplier)
    {
        return CREATIVE_MODE_TABS.register(name.getPath(), supplier);
    }


    @Override
    public EntityFunctionEntry getFunctionValue(Identifier id)
    {
        return AnimalPenRegistry.ENTITY_FUNCTIONS.getValue(id);
    }


    @Override
    public Identifier getFunctionKey(EntityFunctionEntry entry)
    {
        return AnimalPenRegistry.ENTITY_FUNCTIONS.getKey(entry);
    }


    @Override
    public List<Item> getRegisterItems()
    {
        return (List<Item>) ITEMS.getEntries().stream().
            map(DeferredHolder::get).
            toList();
    }


    @Override
    public <T> Registry<T> createRegistry(ResourceKey<Registry<T>> registryKey)
    {
        return new RegistryBuilder<>(registryKey).
            sync(true).
            maxId(256).
            create();
    }


    public static void init(IEventBus bus)
    {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        DATA_COMPONENTS.register(bus);
        CRITERION_TRIGGERS.register(bus);
        CREATIVE_MODE_TABS.register(bus);
        ENTITY_FUNCTIONS.register(bus);
    }

    public static final DeferredRegister<EntityFunctionEntry> ENTITY_FUNCTIONS =
        DeferredRegister.create(AnimalPenRegistry.ENTITY_FUNCTIONS, AnimalPen.MOD_ID);

    private static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(Registries.BLOCK, AnimalPen.MOD_ID);

    private static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(Registries.ITEM, AnimalPen.MOD_ID);

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AnimalPen.MOD_ID);

    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, AnimalPen.MOD_ID);

    private static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS =
        DeferredRegister.create(Registries.TRIGGER_TYPE, AnimalPen.MOD_ID);

    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AnimalPen.MOD_ID);
}