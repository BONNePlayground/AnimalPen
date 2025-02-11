package lv.id.bonne.animalpen.platform.fabric;


import java.util.function.Supplier;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;


public class ClientPlatformHelperImpl
{
    public static <T extends Block> void setRenderLayer(Supplier<T> block, RenderType type)
    {
        BlockRenderLayerMap.INSTANCE.putBlock(block.get(), type);
    }


    public static <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<T> type,
        BlockEntityRendererProvider<T> renderProvider)
    {
        BlockEntityRendererRegistry.register(type, renderProvider);
    }
}
