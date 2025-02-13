//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.config.util;


import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import lv.id.bonne.animalpen.config.annotations.JsonComment;


/**
 * This util class handles object serialization into string and adding comments from annotation.
 */
public class CommentGeneration
{
    /**
     * This method generates JSON text from given object but adds comments from @JsonComment annotation.
     */
    public static String writeWithComments(Gson gson, Object obj)
        throws IllegalAccessException
    {
        StringBuilder jsonWithComments = new StringBuilder();
        jsonWithComments.append("{\n");

        // Recursive method to process objects and their nested fields
        serializeObjectWithComments(obj, jsonWithComments, gson, 1);

        jsonWithComments.append("\n}");

        return jsonWithComments.toString();
    }


    /**
     * Main method that serializes object and adds comments.
     */
    private static void serializeObjectWithComments(Object obj,
        StringBuilder jsonWithComments,
        Gson gson,
        int indentLevel) throws IllegalAccessException
    {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        String indent = "  ".repeat(indentLevel);
        boolean first = true;

        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];

            if (!field.trySetAccessible())
            {
                continue;
            }

            // Skip fields marked with @Expose(serialize = false) or without @Expose when requireExpose is true
            Expose expose = field.getAnnotation(Expose.class);
            if (expose != null && !expose.serialize())
            {
                continue;
            }

            if (!first)
            {
                jsonWithComments.append(",");
                jsonWithComments.append("\n");
            }

            first = false;

            String jsonPropertyName = field.getName();

            SerializedName serializedName = field.getAnnotation(SerializedName.class);
            if (serializedName != null)
            {
                // Use the value specified in @SerializedName
                jsonPropertyName = serializedName.value();
            }

            JsonComment[] comment = field.getDeclaredAnnotationsByType(JsonComment.class);

            if (comment != null)
            {
                for (JsonComment jsonComment : comment)
                {
                    jsonWithComments.append(indent).append("// ").append(jsonComment.value()).append("\n");
                }
            }

            jsonWithComments.append(indent).append("\"").append(jsonPropertyName).append("\": ");

            Object fieldValue = field.get(obj);

            if (fieldValue != null)
            {
                if (fieldValue instanceof Collection<?> collection)
                {
                    // Handle Collection (List, Set)
                    jsonWithComments.append("[\n");
                    serializeCollection(collection, jsonWithComments, gson, indentLevel + 1);
                    jsonWithComments.append(indent).append("]");
                }
                else if (fieldValue instanceof Map<?, ?> map)
                {
                    // Handle Map
                    jsonWithComments.append("{\n");
                    serializeMap(map, jsonWithComments, gson, indentLevel + 1);
                    jsonWithComments.append(indent).append("}");
                }
                else if (hasTypeAdapter(gson, field))
                {
                    jsonWithComments.append(gson.toJson(fieldValue));
                }
                else if (isNotPrimitive(fieldValue))
                {
                    // Serialize object as recursive object.
                    jsonWithComments.append("{\n");
                    serializeObjectWithComments(fieldValue, jsonWithComments, gson, indentLevel + 1);
                    jsonWithComments.append("\n");
                    jsonWithComments.append(indent).append("}");
                }
                else
                {
                    // If the field is a primitive or String, serialize directly
                    jsonWithComments.append(gson.toJson(fieldValue));
                }
            }
            else
            {
                jsonWithComments.append("null");
            }
        }
    }


    private static boolean hasTypeAdapter(Gson gson, Field field)
    {
        // Get the TypeToken for the field type
        Type fieldType = field.getGenericType();

        try
        {
            // Try to get the adapter factory for this type
            // If no adapter exists, this will throw IllegalArgumentException
            gson.getAdapter(TypeToken.get(fieldType));
            return true;
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }



    /**
     * This method serializes collections into json text with comments.
     */
    private static void serializeCollection(Collection<?> collection,
        StringBuilder jsonWithComments,
        Gson gson,
        int indentLevel)
        throws IllegalAccessException
    {
        String indent = "  ".repeat(indentLevel);
        int count = 0;
        for (Object item : collection)
        {
            jsonWithComments.append(indent);

            if (item instanceof Collection<?> collection2)
            {
                // Handle Collection (List, Set)
                jsonWithComments.append("[\n");
                serializeCollection(collection2, jsonWithComments, gson, indentLevel + 1);
                jsonWithComments.append(indent).append("]");
            }
            else if (item instanceof Map<?, ?> map)
            {
                // Handle Map
                jsonWithComments.append("{\n");
                serializeMap(map, jsonWithComments, gson, indentLevel + 1);
                jsonWithComments.append(indent).append("}");
            }
            else if (isNotPrimitive(item))
            {
                // Serialize object as recursive object.
                jsonWithComments.append("{\n");
                serializeObjectWithComments(item, jsonWithComments, gson, indentLevel + 1);
                jsonWithComments.append("\n");
                jsonWithComments.append(indent).append("}");
            }
            else
            {
                // If the field is a primitive or String, serialize directly
                jsonWithComments.append(gson.toJson(item));
            }

            if (++count < collection.size())
            {
                jsonWithComments.append(",");
            }

            jsonWithComments.append("\n");
        }
    }


    /**
     * This method serializes map entries into json text with comments.
     */
    private static void serializeMap(Map<?, ?> map,
        StringBuilder jsonWithComments,
        Gson gson,
        int indentLevel)
        throws IllegalAccessException
    {
        String indent = "  ".repeat(indentLevel);
        int count = 0;

        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            jsonWithComments.append(indent).append("\"").append(entry.getKey()).append("\": ");
            Object value = entry.getValue();

            if (value instanceof Collection<?> collection)
            {
                // Handle Collection (List, Set)
                jsonWithComments.append("[\n");
                serializeCollection(collection, jsonWithComments, gson, indentLevel + 1);
                jsonWithComments.append(indent).append("]");
            }
            else if (value instanceof Map<?, ?> map2)
            {
                // Handle Map
                jsonWithComments.append("{\n");
                serializeMap(map2, jsonWithComments, gson, indentLevel + 1);
                jsonWithComments.append(indent).append("}");
            }
            else if (isNotPrimitive(value))
            {
                // Serialize object as recursive object.
                jsonWithComments.append("{\n");
                serializeObjectWithComments(value, jsonWithComments, gson, indentLevel + 1);
                jsonWithComments.append("\n");
                jsonWithComments.append(indent).append("}");
            }
            else
            {
                // If the field is a primitive or String, serialize directly
                jsonWithComments.append(gson.toJson(value));
            }

            if (++count < map.size())
            {
                jsonWithComments.append(",");
            }

            jsonWithComments.append("\n");
        }
    }


    /**
     * This method returns if an object is not primitive.
     */
    private static boolean isNotPrimitive(Object obj)
    {
        return !(obj instanceof String) &&
            !obj.getClass().isPrimitive() &&
            !(obj instanceof Number) &&
            !(obj instanceof Boolean);
    }
}