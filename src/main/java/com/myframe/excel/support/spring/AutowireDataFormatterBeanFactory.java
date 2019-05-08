package com.myframe.excel.support.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class AutowireDataFormatterBeanFactory implements ApplicationContextAware {



    private  static AutowireCapableBeanFactory BEAN_FACTORY;


    public static Object autowireBean(Object object){

        if (object != null && BEAN_FACTORY != null)
        {
            BEAN_FACTORY.autowireBean(object);
        }
        return object;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        if (applicationContext != null)
        {
            BEAN_FACTORY = applicationContext.getAutowireCapableBeanFactory();
        }
    }
}
