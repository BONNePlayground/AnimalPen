package lv.id.bonne.animalpen.items;


import lv.id.bonne.animalpen.blocks.entities.AviaryTileEntity;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;


/**
 * This is a main item that allows to pick up flying animals.
 */
public class BirdCatcherItem extends AbstractAnimalStorageItem
{
    public BirdCatcherItem(Properties properties)
    {
        super(properties);
    }


    @Override
    protected String tooltipKeyBase()
    {
        return "item.animal_pen.bird_catcher";
    }


    @Override
    protected TagKey<EntityType<?>> pickableTag()
    {
        return AnimalPenTags.BIRD_CATCHER_PICKABLE;
    }


    @Override
    protected BlockEntityHandler blockEntityHandler()
    {
        return (be, player, hand) ->
            be instanceof AviaryTileEntity birdCage &&
                birdCage.processContainer(player, hand);
    }
}
