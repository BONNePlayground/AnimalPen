package lv.id.bonne.animalpen.platform.services;


import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;


public interface IPlatformHelper
{
    /**
     * Creates a {@link BlockEntityType} capable of handling multiple {@link Block} instances.
     * This is useful for registering block entity types that correspond to various block placements.
     *
     * @param blockEntityFactory The factory responsible for creating the specific type of {@link BlockEntity}.
     * @param blocks A variable list of {@link Block} instances that this block entity type can represent.
     * @param <T> The type of {@link BlockEntity} managed by this factory.
     * @return A fully configured {@link BlockEntityType<T>}.
     */
    <T extends BlockEntity> BlockEntityType<T> createBlockEntity(
        BlockEntityType.BlockEntitySupplier<? extends T> blockEntityFactory, Block... blocks);


    /**
     * Creates a {@link CreativeModeTab} used to group related items within the creative inventory.
     *
     * @param translatable The translation key for the tab's name.
     * @param icon A supplier providing the icon stack for the tab.
     * @param itemStacks A list of {@link ItemLike} instances that should be displayed on this tab.
     * @param <T> The type extending {@link ItemLike}.
     * @return A configured {@link CreativeModeTab}.
     */
    <T extends ItemLike> CreativeModeTab createCreativeTab(Component translatable,
        Supplier<ItemStack> icon,
        List<T> itemStacks);


    /**
     * Checks if a specific mod with the given ID is currently loaded in the game environment.
     *
     * @param modId The unique identifier of the mod to check.
     * @return true if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);


    /**
     * Determines if the player passed into the method is a fake/non-client-side entity (e.g., in specific testing or
     * server simulation contexts).
     *
     * @param player The {@link Player} to check.
     * @return true if the player is considered 'fake' according to the platform logic, false otherwise.
     */
    boolean isFake(Player player);
}