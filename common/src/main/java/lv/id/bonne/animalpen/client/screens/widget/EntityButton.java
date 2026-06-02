//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.client.screens.widget;


import lv.id.bonne.animalpen.mixin.accessors.EntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.WaterAnimal;


public class EntityButton extends Button
{
    private LivingEntity cachedEntity;


    public EntityButton(int x, int y, int width, int height, CompoundTag entityTag, Button.OnPress onPress)
    {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);

        // Deserialize and cache the entity strictly on the client side
        if (Minecraft.getInstance().level != null && entityTag != null)
        {
            Entity entity = EntityType.loadEntityRecursive(entityTag, Minecraft.getInstance().level, (ent) -> ent);
            if (entity instanceof LivingEntity livingEntity)
            {
                this.cachedEntity = livingEntity;

                if (livingEntity instanceof WaterAnimal animal)
                {
                    animal.setPose(Pose.SWIMMING);
                    animal.setSwimming(true);
                    ((EntityAccessor) animal).setWasTouchingWater(true);
                }
            }
        }
    }


    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        if (!this.visible) return;

        // Render background slot container
        int backgroundColor = this.isHoveredOrFocused() ? 0xFF444444 : 0xFF222222;
        int borderColor = 0xFF8B8B8B;
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, borderColor);
        graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, backgroundColor);

        if (this.cachedEntity != null)
        {
            float bbWidth = this.cachedEntity.getBbWidth();
            float bbHeight = this.cachedEntity.getBbHeight();

            // Find the absolute largest dimension (handles exceptionally wide or tall mobs uniformly)
            float maxDimension = Math.max(bbWidth, bbHeight);
            if (maxDimension <= 0) maxDimension = 1.0F;

            // Run scaler into 24px
            float targetSize = 24.0F;
            float scale = targetSize / maxDimension;

            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics,
                this.getX() + 2,
                this.getY() + 2,
                this.getX() + 44,
                this.getY() + 44,
                (int) scale,
                0f,
                this.getX() + 100,
                this.getY() + 22,
                this.cachedEntity);
        }
    }
}