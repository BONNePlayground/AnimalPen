package lv.id.bonne.animalpens.neoforge;

import lv.id.bonne.animalpens.AnimalPens;
import net.neoforged.fml.common.Mod;

@Mod(AnimalPens.MOD_ID)
public final class AnimalPensNeoForge {
    public AnimalPensNeoForge() {
        // Run our common setup.
        AnimalPens.init();
    }
}
