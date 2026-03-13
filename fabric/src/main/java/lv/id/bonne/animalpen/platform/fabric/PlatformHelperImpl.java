package lv.id.bonne.animalpen.platform.fabric;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;


public class PlatformHelperImpl
{
    public static SpawnEggItem getSpawnEgg(EntityType<?> entityType)
    {
        return SpawnEggItem.byId(entityType);
    }
}