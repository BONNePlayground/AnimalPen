package lv.id.bonne.animalpen.platform.fabric;


import java.util.List;
import java.util.function.Supplier;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.platform.services.IRegistryHelper;
import lv.id.bonne.animalpen.processing.function.wrapper.EntityFunctionEntry;
import lv.id.bonne.animalpen.registries.AnimalPenRegistry;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;


public class RegistryHelperImpl implements IRegistryHelper
{
    @Override
    public <T extends Block> Supplier<T> registerBlock(Identifier name, Supplier<T> supplier)
    {
        T register = Registry.register(BuiltInRegistries.BLOCK, name, supplier.get());
        return () -> register;
    }


    @Override
    public <T extends Item> Supplier<T> registerItem(Identifier name, Supplier<T> supplier)
    {
        T register = Registry.register(BuiltInRegistries.ITEM, name, supplier.get());
        return () -> register;
    }


    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntityType(Identifier name,
        Supplier<BlockEntityType<T>> supplier)
    {
        BlockEntityType<T> register = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, name, supplier.get());
        return () -> register;
    }


    @Override
    public <T extends EntityFunctionEntry> Supplier<T> registerFunction(Identifier name, Supplier<T> supplier)
    {
        T register = Registry.register(AnimalPenRegistry.ENTITY_FUNCTIONS, name, supplier.get());
        return () -> register;
    }


    @Override
    public <T extends DataComponentType<?>> Supplier<T> registerDataComponent(Identifier name, Supplier<T> supplier)
    {
        T register = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, name, supplier.get());
        return () -> register;
    }


    @Override
    public <T extends CriterionTrigger<?>> Supplier<T> registerTrigger(Identifier name, Supplier<T> supplier)
    {
        T register = Registry.register(BuiltInRegistries.TRIGGER_TYPES, name, supplier.get());
        return () -> register;
    }


    @Override
    public <T extends CreativeModeTab> Supplier<T> registerCreativeTab(Identifier name, Supplier<T> supplier)
    {
        T register = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, name, supplier.get());
        return () -> register;
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
        return BuiltInRegistries.ITEM.stream().
            filter(item -> item.builtInRegistryHolder().key().identifier().getNamespace().equals(AnimalPen.MOD_ID)).
            toList();
    }


    @Override
    public <T> Registry<T> createRegistry(ResourceKey<Registry<T>> registryKey)
    {
        return FabricRegistryBuilder.
            create(registryKey).
            attribute(RegistryAttribute.SYNCED).
            buildAndRegister();
    }
}