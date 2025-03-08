//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.entities;



import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;


/**
 * This interface is used on both Animal Pen and Aquarium to have common methods.
 * @param <T> Instance of LivingEntity that is common for both: animal and water animal
 */
public interface AnimalPenBlockInterface<T extends LivingEntity>
{
    /**
     * This method returns stored animal for block entity.
     *
     * @return Animal instance stored in block entity.
     */
    T getStoredAnimal();

    /**
     * This method returns animal display size.
     * @return The display size of animal.
     */
    long getAnimalDisplaySize();

    /**
     * This method changes animal display size.
     * @param size The animal display size.
     */
    void setAnimalDisplaySize(long size);

    /**
     * This method returns the count of animals in pen.
     * @return The animal count in pen.
     */
    long getAnimalCount();

    /**
     * This method returns if animal can grow by config option.
     * @return {@code true} if entity can grow in block.
     */
    boolean canGrowEntity();

    /**
     * Returns the list of entity variants stored in cage.
     * @return List of entity variants in cage.
     */
    ListTag getEntityVariants();

    /**
     * This method sets new animal variant from given CompoundTag tag.
     * @param animalVariant a new animal variant
     */
    void updateAnimalVariant(CompoundTag animalVariant);

    /**
     * This method removes animal pen variant with given index.
     * @param index the variant index to be removed
     */
    void removeAnimalVariant(int index);
}
