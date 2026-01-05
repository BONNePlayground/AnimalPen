package lv.id.bonne.animalpen.data.helper;


/**
 * This interface allows to link tags with add.
 * @param <T>
 */
public interface SimpleTagAppender<T>
{
    SimpleTagAppender<T> add(T value);


    default SimpleTagAppender<T> add(T... values)
    {
        for (T value : values)
        {
            this.add(value);
        }

        return this;
    }
}