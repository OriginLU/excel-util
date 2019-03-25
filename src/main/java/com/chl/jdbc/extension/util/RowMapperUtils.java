package com.chl.jdbc.extension.util;

import com.chl.jdbc.extension.annotation.Column;
import com.chl.jdbc.extension.annotation.Transient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class RowMapperUtils {

    private static final Logger log = LoggerFactory.getLogger(RowMapperUtils.class);

    private static final Map<String, String> jdbcTypeCache = new HashMap(8);

    private static final Map<Class, String> requireTypeMethodNameCache = new HashMap(16);

    private static final ConcurrentMap<Class, List<Field>> fieldCache = new ConcurrentHashMap(8);

    private static final ConcurrentMap<Class, RowMapper> rowMappersBeanCache = new ConcurrentHashMap(8);


    public static RowMapper getRowMapper(Class clazz) {

        RowMapper rowMapper = (RowMapper) rowMappersBeanCache.get(clazz);
        if (rowMapper != null)
        {
            return rowMapper;
        }
        else
        {
            rowMapper = createRowMapper(clazz);
            rowMappersBeanCache.putIfAbsent(clazz, rowMapper);
            return rowMapper;
        }
    }

    private static RowMapper createRowMapper(Class clazz) {

        return (result, num) -> {
            try {
                Object target = clazz.newInstance();
                List<Field> fields = getFields(clazz);

                for (Field field : fields)
                {
                    if (!isTransient(field))
                    {
                        Object value = getValue(field, result);
                        setValue(field, target, value);
                    }
                }


                return target;
            } catch (Exception e) {
                log.error("create instance fail ", e);
                throw new RuntimeException(e);
            }
        };
    }

    private static boolean isTransient(Field field) {
        return field.isAnnotationPresent(Transient.class);
    }

    private static void setValue(Field field, Object target, Object value) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }

        ReflectionUtils.setField(field, target, value);
    }

    private static Object getValue(Field field, ResultSet resultSet) {
        try {
            return getSourceObject(field, resultSet);
        } catch (Exception var3) {
            log.error("an error occurred while getting target object", var3);
            throw new RuntimeException("an error occurred while getting target object", var3);
        }
    }

    private static Object getSourceObject(Field field, ResultSet resultSet) throws Exception {

        Column column = (Column) field.getAnnotation(Column.class);

        try {
            String name = getColumnName(field, column);
            String methodName = getMethodName(field, column);
            if (StringUtils.isBlank(methodName)) {
                throw new NoSuchMethodException("not found " + methodName);
            } else {
                Method method = ReflectionUtils.findMethod(resultSet.getClass(), methodName, String.class);
                if (method == null) {
                    throw new NoSuchMethodException("not found " + methodName);
                } else {
                    return ReflectionUtils.invokeMethod(method, resultSet, name);
                }
            }
        } catch (Exception e) {
            if (required(column)) {
                throw e;
            } else {
                return null;
            }
        }
    }

    private static String getMethodName(Field field, Column column) {
        Class<?> requireType = field.getType();
        if (column != null) {
            String jdbcType = column.jdbcType().trim().toLowerCase();
            if (StringUtils.isNotBlank(jdbcType)) {
                return (String) jdbcTypeCache.get(jdbcType);
            }
        }

        return (String) requireTypeMethodNameCache.get(requireType);
    }

    private static String getColumnName(Field field, Column column) {
        return column != null && StringUtils.isNotBlank(column.name()) ? column.name().trim() : field.getName();
    }

    private static boolean required(Column column) {
        return column != null ? column.required() : false;
    }

    private static List<Field> getFields(Class clazz) {
        List<Field> fieldSet = (List) fieldCache.get(clazz);
        if (fieldSet != null) {
            return fieldSet;
        } else {
            List<Field> fields = new ArrayList();
            ReflectionUtils.doWithFields(clazz, (field) -> {
                if (!Modifier.isStatic(field.getModifiers())) {
                    fields.add(field);
                }

            });
            fieldCache.putIfAbsent(clazz, fields);
            return fields;
        }
    }

    static {
        requireTypeMethodNameCache.put(Long.TYPE, "getLong");
        requireTypeMethodNameCache.put(Long.class, "getLong");
        requireTypeMethodNameCache.put(Date.class, "getDate");
        requireTypeMethodNameCache.put(Time.class, "getTime");
        requireTypeMethodNameCache.put(Float.TYPE, "getFloat");
        requireTypeMethodNameCache.put(Float.class, "getFloat");
        requireTypeMethodNameCache.put(Double.TYPE, "getDate");
        requireTypeMethodNameCache.put(String.class, "getString");
        requireTypeMethodNameCache.put(Double.class, "getDate");
        requireTypeMethodNameCache.put(Boolean.TYPE, "getBoolean");
        requireTypeMethodNameCache.put(Boolean.class, "getBoolean");
        requireTypeMethodNameCache.put(Integer.TYPE, "getInt");
        requireTypeMethodNameCache.put(Integer.class, "getInt");
        requireTypeMethodNameCache.put(Timestamp.class, "getTimestamp");
        requireTypeMethodNameCache.put(BigDecimal.class, "getBigDecimal");
        jdbcTypeCache.put("date", "getDate");
        jdbcTypeCache.put("time", "getTime");
        jdbcTypeCache.put("timestamp", "getTimestamp");
        jdbcTypeCache.put("dateTime", "getTimestamp");
    }
}