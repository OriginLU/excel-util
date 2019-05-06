package com.myframe.common.utils;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

public class JAXBUtils {


    public static String beanToXml(Object obj, String charset) {

        try
        {
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            JAXBContext context = JAXBContext.newInstance(obj.getClass());

            Marshaller marshaller = context.createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            marshaller.setProperty(Marshaller.JAXB_ENCODING, charset);

            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            marshaller.marshal(obj, os);

            return os.toString(charset);
        }
        catch (JAXBException | UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }


    /**
     * If an object factory is defined. You need to specify the context
     * in which the object factory is defined when you convert.
     */
    public static Object XmlToBeanDependObjectFactory(String xml, Class<?> clazz) {

        try
        {
            JAXBContext context = JAXBContext.newInstance(clazz.getPackage().getName(),getClassLoader(clazz));

            Unmarshaller unmarshaller = context.createUnmarshaller();

            return unmarshaller.unmarshal(new StringReader(xml));

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }




    private static ClassLoader getClassLoader(Class<?> clazz){

        try
        {
            ClassLoader classLoader = clazz.getClassLoader();

            if (null == classLoader)
            {
                classLoader = Thread.currentThread().getContextClassLoader();
            }

            if (null == classLoader)
            {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            return classLoader;
        }
        catch (Exception e)
        {
            return ClassLoader.getSystemClassLoader();
        }
    }


    public static Object XmlToBean(String xml, Class<?> clazz) {

        try
        {
            JAXBContext context = JAXBContext.newInstance(clazz);

            Unmarshaller unmarshaller = context.createUnmarshaller();

            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();

            SAXSource saxSource = new SAXSource(xmlReader,new InputSource(new StringReader(xml)));

            return unmarshaller.unmarshal(saxSource);

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
