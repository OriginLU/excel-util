package com.chl.jdbc.extension.util;

import com.chl.excel.util.ReflectUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lch
 * @since 2019-03-24
 */
public abstract class EnvUtils {


    public static Map<String,Object> getEnv(Object object){

        if (object instanceof Map)
        {
            return (Map<String, Object>) object;
        }
        else if (object instanceof Collection)
        {
            return null;
        }
        else if (object instanceof Object[])
        {
            Map<String, Object> env = new HashMap<>();
            Object[] objects = (Object[]) object;
            for (Object o : objects)
            {
                Map<String, Object> env1 = getEnv(o);
                if (env1 != null)
                {
                    env.putAll(env1);
                }
            }
            return env;
        }
        else
        {
            Map<String, Object> env = new HashMap<>();
            List<Field> fields = ReflectUtils.getFields(object.getClass());
            for (Field field : fields)
            {
                Object value = ReflectUtils.getFieldValue(object, field);
                env.put(field.getName(),value);
            }
            return env;
        }
    }
}
