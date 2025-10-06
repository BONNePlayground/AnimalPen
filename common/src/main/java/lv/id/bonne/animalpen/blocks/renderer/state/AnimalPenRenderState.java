package lv.id.bonne.animalpen.blocks.renderer.state;


import org.apache.commons.lang3.tuple.Pair;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class AnimalPenRenderState extends BlockEntityRenderState
{
    public AnimalPenRenderState()
    {
        this.facing = Direction.NORTH;
        this.dyingEntity = new ArrayList<>();
    }


    /**
     * Needed for creating item model
     */
    public Level level;

    /**
     * Needed for directional rendering
     */
    public Direction facing;

    /**
     * Needed for animal size calculations
     */
    public float animalDisplaySize;

    /**
     * Needed for animal size calculation as well as for displaying amount of animals in front
     */
    public long animalCount;

    /**
     * Needed for display entity rendering
     */
    public EntityRenderState displayEntity;

    /**
     * Needed for displaying dying entity rendering
     */
    public List<EntityRenderState> dyingEntity;

    /**
     * Needed for displaying the cooldown lines above entity
     */
    public List<Pair<ItemStack[], Component>> cooldownLines;
}
