package lv.id.bonne.animalpen.client;


import org.jetbrains.annotations.Nullable;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;


public class WaterTankColor implements BlockColor
{
    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tintIndex) {
        int r = 148;   // 0-255
        int g = 190;   // 0-255
        int b = 211;  // 0-255
        int a = 50;  // 0-255, where 255 is fully opaque and 0 is fully transparent
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}