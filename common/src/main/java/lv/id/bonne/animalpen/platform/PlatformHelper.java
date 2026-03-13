package lv.id.bonne.animalpen.platform;


import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;


public class PlatformHelper
{
    @ExpectPlatform
    public static SpawnEggItem getSpawnEgg(EntityType<?> entityType)
    {
        throw new AssertionError();
    }
}