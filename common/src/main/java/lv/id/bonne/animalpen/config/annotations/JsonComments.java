package lv.id.bonne.animalpen.config.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * The annotation holder that allows to store multiple JSON comments.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonComments
{
    JsonComment[] value();
}
