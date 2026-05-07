package lv.id.bonne.animalpen.platform.fabric;


import java.util.List;
import java.util.function.Supplier;

import lv.id.bonne.animalpen.platform.services.IPlatformHelper;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;


public class PlatformHelperImpl implements IPlatformHelper
{
    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntity(BlockEntityType.BlockEntitySupplier<? extends T> blockEntityFactory,
        Block... blocks)
    {
        return FabricBlockEntityTypeBuilder.create((FabricBlockEntityTypeBuilder.Factory<T>) blockEntityFactory::create,
            blocks).build();
    }


    @Override
    public <T extends ItemLike> CreativeModeTab createCreativeTab(Component title,
        Supplier<ItemStack> icon,
        List<T> itemStacks)
    {
        return FabricCreativeModeTab.builder()
            .icon(icon)
            .title(title)
            .displayItems((params, output) ->
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
        return FabricLoader.getInstance().isModLoaded(modId);
    }


    @Override
    public boolean isFake(Player player)
    {
        return player instanceof FakePlayer;
    }
}