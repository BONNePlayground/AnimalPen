package lv.id.bonne.animalpen.client;


import org.jspecify.annotations.NonNull;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.state.BlockState;


public class WaterTankColor implements BlockTintSource
{
    @Override
    public int color(@NonNull BlockState state)
    {
        int r = 148;   // 0-255
        int g = 190;   // 0-255
        int b = 211;  // 0-255
        int a = 190;  // 0-255, where 255 is fully opaque and 0 is fully transparent
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}