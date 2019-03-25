package com.chl.jdbc.extension.util;

import java.util.Collection;

/**
 * @author lch
 * @since 2019-03-23
 */
public class CollectionUtils {


    public static boolean isEmpty(Collection collection) {

        return !isNotEmpty(collection);
    }

    public static boolean isNotEmpty(Collection collection) {

        return (collection != null && collection.size() > 0);
    }


    public static boolean isEmpty(Object[] array) {

        return !(array != null && array.length > 0);
    }
}
