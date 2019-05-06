package com.myframe.jdbc.extension.util;

import com.myframe.jdbc.extension.annotation.Column;
import com.myframe.jdbc.extension.annotation.Transient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
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

    private static final Map<String, String> JDBC_TYPE_CACHE = new HashMap<>(8);

    private static final Map<Class, String> REQUIRE_TYPE_METHOD_NAME_CACHE = new HashMap<>(16);

    private static final ConcurrentMap<Class, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>(8);

    private static final ConcurrentMap<Class, RowMapper> ROW_MAPPER_BEAN_CACHE = new ConcurrentHashMap<>(8);


    @SuppressWarnings("unchecked")
    public static  <T> RowMapper<T> getRowMapper(Class<?> clazz) {

        RowMapper rowMapper = ROW_MAPPER_BEAN_CACHE.get(clazz);
        if (rowMapper == null)
        {
            rowMapper = createRowMapper(clazz);
            ROW_MAPPER_BEAN_CACHE.putIfAbsent(clazz, rowMapper);
        }
        return rowMapper;
    }

    private static  RowMapper createRowMapper(Class clazz) {

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


                return  target;
            } catch (Exception e) {
                log.error("create instance fail ", e);
                throw new RuntimeException(e);
            }
        };
    }

    private static boolean isTransient(Field field) {
        return field.isAnnotationPresent(Transient.class);
    }

    private static void setValue(Member member, Object target, Object value) {

        if (member instanceof  Field)
        {
            final Field field = (Field) member;
            if (!field.isAccessible())
            {
                field.setAccessible(true);
            }

            ReflectionUtils.setField(field, target, value);
        }
        else if (member instanceof Method)
        {
            final Method method = (Method) member;
            if (!method.isAccessible())
            {
                method.setAccessible(true);
            }
            ReflectionUtils.invokeMethod(method, target, value);
        }
    }

    private static Object getValue(Field field, ResultSet resultSet) {

        try
        {
            return getSourceObject(field, resultSet);
        }
        catch (Exception e)
        {
            log.error("an error occurred while getting target object", e);
            throw new RuntimeException("an error occurred while getting target object", e);
        }
    }

    private static Object getSourceObject(Field field, ResultSet resultSet) throws Exception {

        Column column = field.getAnnotation(Column.class);

        try {

            String name = getColumnName(field, column);
            String methodName = getMethodName(field, column);
            if (StringUtils.isBlank(methodName))
            {
                throw new NoSuchMethodException("not found " + methodName);
            }
            Method method = ReflectionUtils.findMethod(resultSet.getClass(), methodName, String.class);
            if (method == null)
            {
                throw new NoSuchMethodException("not found " + methodName);
            }

            return ReflectionUtils.invokeMethod(method, resultSet, name);

        }
        catch (Exception e)
        {
            if (required(column))
            {
                throw e;
            }
            return null;

        }
    }

    private static String getMethodName(Field field, Column column) {

        Class<?> requireType = field.getType();
        if (column != null)
        {
            String jdbcType = column.jdbcType().trim().toLowerCase();
            if (StringUtils.isNotBlank(jdbcType))
            {
                return  JDBC_TYPE_CACHE.get(jdbcType);
            }
        }

        return  REQUIRE_TYPE_METHOD_NAME_CACHE.get(requireType);
    }

    private static String getColumnName(Field field, Column column) {

        return (column != null && StringUtils.isNotBlank(column.name())) ? column.name().trim() : field.getName();
    }

    private static boolean required(Column column) {
        return column != null && column.required();
    }

    private static List<Field> getFields(Class clazz) {

        List<Field> fieldSet =  FIELD_CACHE.get(clazz);
        if (fieldSet != null)
        {
            return fieldSet;
        }

        List<Field> fields = new ArrayList<>();
        FIELD_CACHE.putIfAbsent(clazz, fields);

        ReflectionUtils.doWithFields(clazz, (field) -> {
            if (!Modifier.isStatic(field.getModifiers()))
            {
                fields.add(field);
            }

        });

        return fields;

    }

    static {
        REQUIRE_TYPE_METHOD_NAME_CACHE.put(Long.TYPE, "getLong");
        REQUIRE_TYPE_METHOD_NAME_CACHE.put(Long.class, "getLong");
        REQUIRE_TYPE_METHOD_NAME_CACHE.put(Date.class, "getDate");
        REQUIRE_TYPE_METHOD_NAME_CACHE.put(Time.class, "getTime");
        REQUIRE_TYPE_METHOD_NAME_CACHE.put(Float.TYPE, "getFloat");
        REQUIRE_TYPE_METHOD_NAME_CACHE.put(Float.class, "getFloat");
        REQUIRE_TYPE_METHOD_NAME_CACHE.put(Double.TYPE, "getDate");
        REQUIRE_TYPE_METHOD_NAME_CACHE.put(String.class, "getString");
        REQUIRE_TYPE_METHOD_NAME_CACHE.put(Double.class, "getDate");
        REQUIRE_TYPE_METHOD_NAME_CACHE.put(Boolean.TYPE, "getBoolean");
        REQUIRE_TYPE_METHOD_NAME_CACHE.put(Boolean.class, "getBoolean");
        REQUIRE_TYPE_METHOD_NAME_CACHE.put(Integer.TYPE, "getInt");
        REQUIRE_TYPE_METHOD_NAME_CACHE.put(Integer.class, "getInt");
        REQUIRE_TYPE_METHOD_NAME_CACHE.put(Timestamp.class, "getTimestamp");
        REQUIRE_TYPE_METHOD_NAME_CACHE.put(BigDecimal.class, "getBigDecimal");
        JDBC_TYPE_CACHE.put("date", "getDate");
        JDBC_TYPE_CACHE.put("time", "getTime");
        JDBC_TYPE_CACHE.put("timestamp", "getTimestamp");
        JDBC_TYPE_CACHE.put("dateTime", "getTimestamp");
    }
}