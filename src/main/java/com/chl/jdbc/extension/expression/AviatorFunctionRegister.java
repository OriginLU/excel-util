package com.chl.jdbc.extension.expression;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.type.AviatorFunction;

/**
 * @author lch
 * @since 2019-03-23
 */
public class AviatorFunctionRegister {



    public static void register(AviatorFunction function){

        AviatorEvaluator.addFunction(function);

    }
}
