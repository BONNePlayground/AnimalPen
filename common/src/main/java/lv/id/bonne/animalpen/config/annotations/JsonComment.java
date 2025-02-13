package lv.id.bonne.animalpen.config.annotations;


import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * The annotation that allows to add single line comment for JSON files.
 */
@Repeatable(JsonComments.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonComment
{
    String value();
}
