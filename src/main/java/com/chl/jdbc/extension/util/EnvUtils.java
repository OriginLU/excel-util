package com.chl.jdbc.extension.util;

import java.util.Map;

/**
 * @author lch
 * @since 2019-03-24
 */
public abstract class EnvUtils {


    public static Map<String,Object> getEnv(Object object){

        if (object instanceof Map){

            return (Map<String, Object>) object;
        }

        return null;
    }
}
