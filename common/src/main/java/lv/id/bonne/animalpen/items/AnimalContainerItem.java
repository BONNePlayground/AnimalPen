package lv.id.bonne.animalpen.items;


import lv.id.bonne.animalpen.blocks.entities.AquariumTileEntity;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;


/**
 * This is a main item that allows to pick up water animals.
 */
public class AnimalContainerItem extends AbstractAnimalStorageItem
{
    public AnimalContainerItem(Properties properties)
    {
        super(properties);
    }


    @Override
    protected String tooltipKeyBase()
    {
        return "item.animal_pen.water_animal_container";
    }


    @Override
    protected TagKey<EntityType<?>> pickableTag()
    {
        return AnimalPenTags.WATER_MOB_CONTAINER_PICKABLE;
    }


    @Override
    protected BlockEntityHandler blockEntityHandler()
    {
        return (be, player, hand) ->
            be instanceof AquariumTileEntity aquarium &&
                aquarium.processContainer(player, hand);
    }
}
