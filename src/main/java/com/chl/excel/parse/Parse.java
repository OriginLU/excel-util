package com.chl.excel.parse;

/**
 * 解析接口
 * @author LCH
 * @since 2018-06-15
 */
public interface Parse {

    /**
     * the result evaluted by the express and context
     * @param express
     * @param context
     * @param <T>  result
     * @return
     */
    <T> T evalute(String express,Object context);
}
