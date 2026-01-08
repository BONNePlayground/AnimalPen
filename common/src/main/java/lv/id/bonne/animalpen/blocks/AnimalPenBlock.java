package lv.id.bonne.animalpen.blocks;


import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

import lv.id.bonne.animalpen.blocks.entities.AnimalPenTileEntity;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


public class AnimalPenBlock extends AbstractAnimalContainerBlock<AnimalPenTileEntity>
{
    public AnimalPenBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }


    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec()
    {
        return CODEC;
    }


    @Override
    protected TagKey<Item> getAttackToolTag()
    {
        return AnimalPenTags.ANIMAL_PEN_ATTACK_TOOLS;
    }


    @Override
    protected BlockEntityType<AnimalPenTileEntity> getTileType()
    {
        return AnimalPenTileEntityRegistry.ANIMAL_PEN_TILE_ENTITY.get();
    }


    @Override
    public Item getContainerItem()
    {
        return AnimalPensItemRegistry.ANIMAL_CAGE.get();
    }


// ---------------------------------------------------------------------
// Section: Placement related
// ---------------------------------------------------------------------


    /**
     * Create block state definition
     *
     * @param builder The definition builder.
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }


    /**
     * This method allows to rotate block opposite to player.
     *
     * @param context The placement context.
     * @return The new block state.
     */
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
    {
        return Objects.requireNonNull(super.getStateForPlacement(context)).
            setValue(FACING, context.getHorizontalDirection().getOpposite());
    }


    /**
     * This method returns the shape of current table.
     *
     * @param state The block state.
     * @param level The level where block is located.
     * @param pos The position of the block.
     * @param context The collision content.
     * @return The VoxelShape of current table.
     */
    @Override
    @NotNull
    public VoxelShape getShape(@NotNull BlockState state,
        @NotNull BlockGetter level,
        @NotNull BlockPos pos,
        @NotNull CollisionContext context)
    {
        return SHAPE;
    }


    private final VoxelShape SHAPE = Shapes.or(
        Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
        Block.box(0.0, 4.0, 0.0, 3.0, 8.0, 3.0),
        Block.box(0.0, 4.0, 13.0, 3.0, 8.0, 16.0),
        Block.box(13.0, 4.0, 0.0, 16.0, 8.0, 3.0),
        Block.box(13.0, 4.0, 13.0, 16.0, 8.0, 16.0),
        Block.box(3.0, 5.0, 0.0, 13.0, 7.0, 2.0),
        Block.box(3.0, 5.0, 14.0, 13.0, 7.0, 16.0),
        Block.box(0.0, 5.0, 3.0, 2.0, 7.0, 13.0),
        Block.box(14.0, 5.0, 3.0, 16.0, 7.0, 13.0));


    public static final MapCodec<AnimalPenBlock> CODEC = simpleCodec(AnimalPenBlock::new);
}