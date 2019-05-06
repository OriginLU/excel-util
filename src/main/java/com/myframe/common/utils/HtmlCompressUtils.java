package com.myframe.common.utils;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

import java.io.*;
import java.nio.charset.Charset;


public abstract class HtmlCompressUtils {

    public static String htmlFileCompress(String path,String charset){

        String html = readFile(path,charset);

        HtmlCompressor compressor = getHtmlCompressor();

        return compressor.compress(html);
    }


    public static String htmlTextCompress(String text){

        HtmlCompressor compressor = getHtmlCompressor();

        return compressor.compress(text);

    }


    private static HtmlCompressor getHtmlCompressor(){


        HtmlCompressor compressor = new HtmlCompressor();

        compressor.setEnabled(true);
        compressor.setCompressCss(true);
        compressor.setYuiJsPreserveAllSemiColons(true);
        compressor.setYuiJsLineBreak(1);
        compressor.setPreserveLineBreaks(false);
        compressor.setRemoveIntertagSpaces(true);
        compressor.setRemoveComments(true);
        compressor.setRemoveMultiSpaces(true);

        return compressor;
    }

    public static String readFile(String path, String charset) {


        BufferedReader br = null;
        try
        {
            String line;
            StringBuilder sb = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), Charset.forName(charset)));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            close(br);
        }
    }

    public static void writeToLocal(String text,String path,String charset){

        BufferedWriter bw = null;
        try
        {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path)), Charset.forName(charset)));
            bw.write(text);
            bw.flush();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            close(bw);
        }
    }


    private static void close(Closeable stream){

        try
        {
            if (stream != null){
                stream.close();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


}
