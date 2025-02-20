package lv.id.bonne.animalpen.platform;


import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;


public class ClientPlatformHelper
{
    @ExpectPlatform
    public static <T extends BlockEntity> BlockEntityType<T> create(
        BlockEntityType.BlockEntitySupplier<? extends T> blockEntityFactory, Block... blocks)
    {
        throw new AssertionError();
    }
}
