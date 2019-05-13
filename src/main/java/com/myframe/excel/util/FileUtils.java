package com.myframe.excel.util;


import java.io.File;

public  class FileUtils {



    public static String getPath(String path){

        File file = new File(path);

        if (!file.exists())
        {
            if (!file.mkdir())
            {
                throw new IllegalStateException("can't create file path [" + path + "],please check if the permissions are sufficient");
            }
        }
        else if (file.isFile())
        {
            throw new IllegalArgumentException("the required parameter is file path,the input parameter is [" + path + "], check please");
        }

        return file.getAbsolutePath() + File.separator;
    }

}
