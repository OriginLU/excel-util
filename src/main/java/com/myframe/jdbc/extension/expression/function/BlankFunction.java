package com.myframe.jdbc.extension.expression.function;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

import static com.myframe.jdbc.extension.util.StringUtils.isBlank;

/**
 * @author lch
 * @since 2019-03-18
 */
public class BlankFunction extends BaseFunction {


    @Override
    public String getName() {
        return "isBlank";
    }


    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {

        String object = FunctionUtils.getStringValue(arg1, env);
        if (isBlank(object))
        {
            return AviatorBoolean.valueOf(true);
        }
        return AviatorBoolean.valueOf(false);
    }






}
