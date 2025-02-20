package lv.id.bonne.animalpen.platform.fabric;


import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;


public class ClientPlatformHelperImpl
{
    public static <T extends BlockEntity> BlockEntityType<T> create(
        BlockEntityType.BlockEntitySupplier<? extends T> blockEntityFactory, Block... blocks)
    {
        return FabricBlockEntityTypeBuilder.create((FabricBlockEntityTypeBuilder.Factory<T>) blockEntityFactory::create, blocks).build();
    }
}
