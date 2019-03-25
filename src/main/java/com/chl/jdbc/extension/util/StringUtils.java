package com.chl.jdbc.extension.util;

/**
 * @author lch
 * @since 2019-03-22
 */
public abstract class StringUtils {


    public static boolean isBlank(CharSequence sequence){

        if (sequence != null){

            for (int i = 0; i < sequence.length(); i++)
            {
                if (!Character.isWhitespace(sequence.charAt(i)))
                {
                    return false;
                }
            }
        }
        return true;
    }



    public static boolean isNotBlank(CharSequence sequence){

        return !isBlank(sequence);
    }
}
