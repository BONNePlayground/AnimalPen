package lv.id.bonne.animalpen.platform.forge;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;


public class PlatformHelperImpl
{
    public static SpawnEggItem getSpawnEgg(EntityType<?> entityType)
    {
        return ForgeSpawnEggItem.fromEntityType(entityType);
    }
}