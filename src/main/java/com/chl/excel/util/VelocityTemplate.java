package com.chl.excel.util;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.NullLogChute;

import java.io.File;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;


/**
 * velocity template load tool,it provide two way to load template file
 *
 * @author chlu
 * @version 20190305
 */
public enum VelocityTemplate {


    CLASSPATH,

    FILE{

        protected void initial(){
            loadFile();
        }

        @Override
        public StringWriter mergeTemplate(Map<String, Object> paraMap, String templateName, String charset) {

            String path = getPath(templateName);

            String name = getName(templateName);

            setProperty("input.encoding",charset);

            setProperty("output.encoding",charset);

            setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH,path);

            return super.mergeTemplate(paraMap,name, charset);
        }

        private String getPath(String path){

            File file = new File(path);

            if (file.exists() && !file.isDirectory())
            {
               return file.getParent();
            }
            throw new IllegalArgumentException("input parameter has error," +
                    "it must be a file instead of a directory and be exists [" + path + "]");
        }

        private String getName(String path){

            int index = path.lastIndexOf('\\');
            if (index == -1)
            {
                 index = path.lastIndexOf('/');
            }
            return path.substring(index);
        }
    };



    private VelocityEngine velocityEngine;


    VelocityTemplate(){

        this.velocityEngine = new VelocityEngine();
        this.velocityEngine.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogChute.class.getName());
        initial();
    }



    protected void initial() {

        loadClasspath();
    }

    public StringWriter mergeTemplate(Map<String,Object> paraMap,String templateName,String charset){

        StringWriter writer = new StringWriter();

        VelocityContext velocityContext = new VelocityContext();

        InitialVelocityContext(paraMap,velocityContext);

        velocityEngine.mergeTemplate(templateName,charset,velocityContext,writer);

        return writer;
    }


    private  void InitialVelocityContext(Map<String, Object> paraMap, VelocityContext velocityContext) {

        if (paraMap != null && !paraMap.isEmpty())
        {
            Iterator<Map.Entry<String, Object>> it = paraMap.entrySet().iterator();

            for (;it.hasNext();)
            {
                Map.Entry<String, Object> entry = it.next();

                velocityContext.put(entry.getKey(),entry.getValue());
            }
        }

    }

    protected void loadFile(){

        velocityEngine.setProperty(VelocityEngine.RESOURCE_LOADER, "file");

        velocityEngine.setProperty("file.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");

    }


    protected void loadClasspath(){

        velocityEngine.setProperty(VelocityEngine.RESOURCE_LOADER, "class");

        velocityEngine.setProperty("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

    }


    public void setProperty(String key,Object value){

        velocityEngine.setProperty(key,value);
    }

}
