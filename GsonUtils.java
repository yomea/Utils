package com.booway;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * json帮助类
 * @author wuzhenhong
 */
public class GsonUtils
{
    private static final Gson GSON;

    static
    {
        GSON = new GsonBuilder()
                // .serializeNulls()//不序列化null值
                .setDateFormat("yyyy-MM-dd")
                .disableInnerClassSerialization()//禁止序列化内部类
                .create();
    }
    
    private GsonUtils(){}

    public static String toJson(Object obj)
    {
        return GSON.toJson(obj);
    }

    public static <T> T toObject(String jsonStr, Class<T> classOfT)
    {
        return GSON.fromJson(jsonStr, classOfT);
    }

    public static <T> List<T> toList(String jsonStr, Class<T> classOfT)
    {
        return GSON.fromJson(jsonStr, new GenericType(List.class, new Type[]
        { classOfT }));
    }

    public static <T, K> Map<T, K> toMap(String jsonStr, Class<T> classOfT, Class<K> classOfK)
    {
        return GSON.fromJson(jsonStr, new GenericType(Map.class, new Type[]
        { classOfT, classOfK }));
    }

    public static <T, K> Map<T, List<K>> toMapList(String jsonStr, Class<T> classOfT, Class<K> classOfK)
    {
        return GSON.fromJson(jsonStr, new GenericType(Map.class, new Type[]
        { classOfT, new GenericType(List.class, new Type[]
                { classOfK }) }));
    }

    /**
     * 仅仅查找第一个符合路径的json，如果为找到返回null
     * @param jsonStr json字符串
     * @param xpath 层级路径
     * @param delims 分割符
     * @return json串
     */
    public static String getJsonNode(String jsonStr, String xpath, String delims)
    {
        JsonElement jsonElement = GSON.toJsonTree(GSON.fromJson(jsonStr, Object.class));
        String[] paths = xpath.split(delims);
        for (String path : paths)
        {
            jsonElement = searJsonTree(jsonElement, path);
            if (jsonElement == null)
            {
                return null;
            }
        }
        return jsonElement.toString();
    }

    private static JsonElement searJsonTree(JsonElement jsonElement, String path)
    {
        if (jsonElement.isJsonArray())
        {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++)
            {
                jsonElement = searJsonTree(jsonArray.get(i), path);
                if (jsonElement != null)
                {
                    return jsonElement;
                }
            }
        } else if (jsonElement.isJsonObject())
        {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            jsonElement = jsonObject.get(path);
            if (jsonElement != null)
            {
                return jsonElement;
            }
        }
        return null;
    }

    private static class GenericType implements ParameterizedType
    {

        private final Class<?> rawType;

        private final Type[] types;

        private GenericType(Class<?> rawType, Type[] types)
        {
            this.rawType = rawType;
            this.types = types;
        }

        @Override
        public Type[] getActualTypeArguments()
        {
            return this.types;
        }

        @Override
        public Type getRawType()
        {
            return this.rawType;
        }

        @Override
        public Type getOwnerType()
        {
            return null;
        }

    }
}
