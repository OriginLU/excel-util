package com.chl.excel.parse.impl;

import com.chl.excel.parse.Parse;
import com.googlecode.aviator.AviatorEvaluator;

/**
 * @author LCH
 * @since 2018-06-27
 */
public class OperaParse implements Parse {


    public <T> T evalute(String express, Object context) {
        return (T) AviatorEvaluator.exec(express,context);
    }
}
