package com.myframe.jdbc.extension.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lch
 * @since 2019-03-24
 */
public abstract class ThreadContext {

    private static ThreadLocal<Map<Object, Object>> THREAD_CONTEXT = ThreadLocal.withInitial(HashMap::new);


    public static Object getValue(Object key) {
        return THREAD_CONTEXT.get().get(key);
    }


    public static void put(Object key, Object value) {
        THREAD_CONTEXT.get().put(key, value);
    }


    public static void remove(Object key) {
        THREAD_CONTEXT.get().remove(key);
    }

    public static void clear() {
        THREAD_CONTEXT.get().clear();
    }


}
