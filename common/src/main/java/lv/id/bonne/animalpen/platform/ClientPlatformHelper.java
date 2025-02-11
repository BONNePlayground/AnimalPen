package lv.id.bonne.animalpen.platform;


import java.util.function.Supplier;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;


public class ClientPlatformHelper
{
    @ExpectPlatform
    public static <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<T> tileEntity,
        BlockEntityRendererProvider<T> blockEntityRenderer)
    {
        throw new AssertionError();
    }


    @ExpectPlatform
    public static <T extends Block> void setRenderLayer(Supplier<T> block, RenderType type)
    {
        throw new AssertionError();
    }
}
