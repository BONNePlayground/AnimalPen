package lv.id.bonne.animalpen.platform.neoforge;


import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;


public class ClientPlatformHelperImpl
{
    public static <T extends BlockEntity> BlockEntityType<T> create(
        BlockEntityType.BlockEntitySupplier<? extends T> blockEntityFactory, Block... blocks)
    {
        return new BlockEntityType<>(blockEntityFactory, blocks);
    }
}