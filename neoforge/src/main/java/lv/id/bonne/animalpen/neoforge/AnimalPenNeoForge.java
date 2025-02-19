package lv.id.bonne.animalpen.neoforge;


import lv.id.bonne.animalpen.AnimalPen;
import net.neoforged.fml.common.Mod;


@Mod(AnimalPen.MOD_ID)
public final class AnimalPenNeoForge
{
    public AnimalPenNeoForge()
    {
        // Run our common setup.
        AnimalPen.init();
    }
}
