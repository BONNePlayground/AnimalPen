package lv.id.bonne.animalpens.fabric;

import lv.id.bonne.animalpens.AnimalPens;
import net.fabricmc.api.ModInitializer;

public final class AnimalPensFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        AnimalPens.init();
    }
}
