package com.myframe.excel.util;


import java.io.File;

public  class FileUtils {



    public static String getPath(String path){

        File file = new File(path);

        if (!file.isDirectory())
        {
            throw new IllegalArgumentException("the required parameter is file path,the input parameter is [" + path + "], check please");
        }

        if (!file.exists())
        {
            if (!file.mkdir())
            {
                throw new IllegalStateException("can't create file path [" + path + "]");
            }
        }

        return file.getAbsolutePath() + File.separator;
    }

}
