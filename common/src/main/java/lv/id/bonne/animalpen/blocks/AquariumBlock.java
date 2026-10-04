package lv.id.bonne.animalpen.blocks;


import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

import lv.id.bonne.animalpen.blocks.entities.AquariumTileEntity;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
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
            setValue(FILLED, false).
            setValue(DECORATION, Decoration.NONE));
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
    protected boolean propagatesSkylightDown(BlockState blockState)
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
        builder.add(FACING).add(FILLED).add(DECORATION);
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
            setValue(FILLED, false).
            setValue(DECORATION, Decoration.NONE);
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





    public enum Decoration implements StringRepresentable
    {
        NONE("none"),
        CAVE("cave"),
        SHIPWRECK("shipwreck"),
        CORAL("coral");

        Decoration(String name)
        {
            this.name = name;
        }

        @Override
        @NotNull
        public String getSerializedName()
        {
            return this.name;
        }

        private final String name;
    }


    private final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 23.0, 16.0);

    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    public static final EnumProperty<Decoration> DECORATION = EnumProperty.create("decoration", Decoration.class);

    public static final MapCodec<AquariumBlock> CODEC = simpleCodec(AquariumBlock::new);
}