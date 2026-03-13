package lv.id.bonne.animalpen.processing.function.api;


/**
 * This interface is used to change sheared visual state for mobs.
 * It must be implemented into animal for it to work.
 */
public interface ShearStateAccessor {
    void setShearedState(boolean sheared);
}