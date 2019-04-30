package com.chl.excel.converter;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.TypeConverter;
import org.springframework.format.support.DefaultFormattingConversionService;

/**
 * @author lch
 * @since 2019-04-28
 */
public class DefaultFormatterConverter implements TypeConverter {



    private static ConversionService defaultConversionService;


    private ConversionService conversionService;


    public DefaultFormatterConverter() {


        if (null == defaultConversionService)
        {
            synchronized (DefaultFormatterConverter.class)
            {
                if (null == defaultConversionService)
                {
                    defaultConversionService = new DefaultFormattingConversionService();
                }
            }
        }
        this.conversionService = defaultConversionService;
    }

    @Override
    public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return conversionService.canConvert(sourceType,targetType);
    }

    @Override
    public Object convertValue(Object value, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return conversionService.convert(value,sourceType,targetType);
    }
}
