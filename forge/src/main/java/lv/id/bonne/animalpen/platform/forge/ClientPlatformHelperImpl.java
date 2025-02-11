package lv.id.bonne.animalpen.platform.forge;


import java.util.function.Supplier;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;


public class ClientPlatformHelperImpl
{
    public static <T extends Block> void setRenderLayer(Supplier<T> block, RenderType type)
    {
        ItemBlockRenderTypes.setRenderLayer(block.get(), type);
    }


    public static <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<T> type,
        BlockEntityRendererProvider<T> renderProvider)
    {
        BlockEntityRenderers.register(type, renderProvider);
    }
}
