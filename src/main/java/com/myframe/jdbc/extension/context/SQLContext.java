package com.myframe.jdbc.extension.context;

import com.myframe.jdbc.extension.conf.SQLConfig;
import com.myframe.jdbc.extension.exception.NotFoundConfiguration;
import com.myframe.jdbc.extension.parse.AnnotationSQLParser;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author lch
 * @since 2019-03-23
 */
public class SQLContext {


    private AnnotationSQLParser sqlParser;

    private ConcurrentMap<Class,Boolean> initClassCache;

    private ConcurrentMap<String, SQLConfig> sqlConfigCache;

    private SQLContext() {

        this.sqlParser = new AnnotationSQLParser();

        this.initClassCache = new ConcurrentHashMap<>(16);

        this.sqlConfigCache = new ConcurrentHashMap<>(16);
    }


    public void addSQLConfig(String name,SQLConfig config){

        sqlConfigCache.putIfAbsent(name,config);
    }


    public SQLConfig getSQLConfig(String name) {

        return sqlConfigCache.get(name);
    }


    public SQLConfig getSQLConfig(String name,Class clazz){


        SQLConfig sqlConfig = this.sqlConfigCache.get(name);

        if (sqlConfig != null)
        {
            return sqlConfig;
        }


        if (!isInitCompleted(clazz))
        {
            sqlParser.parse(clazz,this);
            initClassCache.put(clazz,true);
            sqlConfig = sqlConfigCache.get(name);
        }


        if (sqlConfig == null)
        {
            throw new NotFoundConfiguration("not found SQL configuration [" + name + "],please check");
        }

        return sqlConfig;

    }

    private boolean isInitCompleted(Class clazz) {

        Boolean isInit = initClassCache.get(clazz);
        return isInit != null ? isInit : false;
    }


    public static SQLContext getInstance(){

        return NewInstance.getInstance();
    }


    private static class NewInstance{

        private static SQLContext sqlContext = new SQLContext();

        private static SQLContext getInstance(){

            return sqlContext;
        }
    }

}
