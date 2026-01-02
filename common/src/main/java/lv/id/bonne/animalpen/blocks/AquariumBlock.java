package lv.id.bonne.animalpen.blocks;


import org.jetbrains.annotations.NotNull;
import java.util.Objects;

import lv.id.bonne.animalpen.blocks.entities.AquariumTileEntity;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


public class AquariumBlock extends AbstractAnimalContainerBlock<AquariumTileEntity>
{
    public AquariumBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().
            setValue(FACING, Direction.NORTH).
            setValue(FILLED, false));
    }


    @Override
    protected TagKey<Item> getAttackToolTag()
    {
        return AnimalPenTags.ANIMAL_PEN_ATTACK_TOOLS;
    }


    @Override
    protected BlockEntityType<AquariumTileEntity> getTileType()
    {
        return AnimalPenTileEntityRegistry.AQUARIUM_TILE_ENTITY.get();
    }


    @Override
    public Item getContainerItem()
    {
        return AnimalPensItemRegistry.ANIMAL_CONTAINER.get();
    }


// ---------------------------------------------------------------------
// Section: PP
// ---------------------------------------------------------------------


    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos)
    {
        return true;
    }


    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context)
    {
        return Shapes.empty();
    }


    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos)
    {
        return 1.0F;
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
        builder.add(FACING).add(FILLED);
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
            setValue(FACING, context.getHorizontalDirection().getOpposite()).
            setValue(FILLED, false);
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


    private final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 23.0, 16.0);

    public static final BooleanProperty FILLED = BooleanProperty.create("filled");
}