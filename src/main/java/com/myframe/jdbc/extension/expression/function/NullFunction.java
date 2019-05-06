package com.myframe.jdbc.extension.expression.function;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;




/**
 * @author lch
 * @since 2019-03-18
 */
public class NullFunction extends BaseFunction {




    @Override
    public String getName() {
        return "$";
    }



    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {

        Object object = FunctionUtils.getJavaObject(arg1, env);
        if (object == null)
        {
            return AviatorBoolean.valueOf(false);
        }
        return AviatorBoolean.valueOf(true);
    }



}
