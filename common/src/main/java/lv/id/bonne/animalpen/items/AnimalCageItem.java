package lv.id.bonne.animalpen.items;


import lv.id.bonne.animalpen.blocks.entities.AnimalPenTileEntity;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;


/**
 * This is a main item that allows to pick up animals.
 */
public class AnimalCageItem extends AbstractAnimalStorageItem
{
    public AnimalCageItem(Properties properties)
    {
        super(properties);
    }

    @Override
    protected String tooltipKeyBase()
    {
        return "item.animal_pen.animal_cage";
    }

    @Override
    protected TagKey<EntityType<?>> pickableTag()
    {
        return AnimalPenTags.ANIMAL_CAGE_PICKABLE;
    }


    @Override
    protected BlockEntityHandler blockEntityHandler()
    {
        return (be, player, hand) ->
            be instanceof AnimalPenTileEntity pen && pen.processContainer(player, hand);
    }
}
