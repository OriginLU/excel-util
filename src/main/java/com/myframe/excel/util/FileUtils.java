package com.myframe.excel.util;


import java.io.File;

public  class FileUtils {



    public static String getPath(String path){

        File file = new File(path);

        if (file.isFile())
        {
            throw new IllegalArgumentException("input parameter error,require parameter is file path,check please");
        }

        if (!file.exists())
        {
            if (!file.mkdir())
            {
                throw new IllegalStateException("can't create direction [" + path + "]");
            }
        }

        return file.getAbsolutePath() + File.separator;
    }

}
