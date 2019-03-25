package com.chl.jdbc.extension.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lch
 * @since 2019-03-24
 */
public class ThreadContext {

    private static ThreadLocal<Map<String,Object>> THREAD_CONTEXT = new ThreadLocal<Map<String, Object>>(){

        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<>();
        }
    };


    public static Object getValue(String key) {

        Map<String, Object> objectMap = THREAD_CONTEXT.get();

        return objectMap.get(key);

    }


    public static void put(String key,Object value){

        Map<String, Object> objectMap = THREAD_CONTEXT.get();

        objectMap.put(key,value);
    }



    public static void remove(String key){

        Map<String, Object> objectMap = THREAD_CONTEXT.get();

        objectMap.remove(key);
    }

    public static void clear(){

        Map<String, Object> objectMap = THREAD_CONTEXT.get();

        objectMap.clear();
    }


}
