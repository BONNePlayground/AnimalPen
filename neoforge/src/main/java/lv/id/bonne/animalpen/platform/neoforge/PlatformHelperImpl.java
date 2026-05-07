package lv.id.bonne.animalpen.platform.neoforge;


import java.util.List;
import java.util.function.Supplier;

import lv.id.bonne.animalpen.platform.services.IPlatformHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.fml.ModList;


public class PlatformHelperImpl implements IPlatformHelper
{

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntity(BlockEntityType.BlockEntitySupplier<? extends T> blockEntityFactory,
        Block... blocks)
    {
        return new BlockEntityType<>(blockEntityFactory, blocks);
    }


    @Override
    public <T extends ItemLike> CreativeModeTab createCreativeTab(Component title,
        Supplier<ItemStack> icon,
        List<T> itemStacks)
    {
        return CreativeModeTab.builder()
            .title(title)
            .icon(icon)
            .displayItems((par, output) ->
            {
                for (ItemLike itemStack : itemStacks)
                {
                    output.accept(itemStack);
                }
            })
            .build();
    }


    @Override
    public boolean isModLoaded(String modId)
    {

        return ModList.get().isLoaded(modId);
    }


    @Override
    public boolean isFake(Player player)
    {
        return player instanceof ServerPlayer && player.getClass() != ServerPlayer.class;
    }
}