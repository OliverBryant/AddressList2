package com.example.addresslist.encrypt;

import static com.example.addresslist.encrypt.SHA256withRSA.generateKeyBytes;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GeneKeyPair {

    public static boolean propertiesKeyIsExist(Properties prop, String key){
        boolean success = false;
        String item = "";
        // verify the properties file is null
        if (prop == null) {
            success = false;
            return success;
        }
        // verify the key is null
        if ("".equals(key) || key == null) {

            success = false;
            return success;
        }

        // get keys from properties
        Enumeration<?> enu = prop.propertyNames();

        // verify the key is contains in properties or not
        while (enu.hasMoreElements()) {
            item = (String)enu.nextElement();

            if (item.equals(key)) {
                success = true;
            }
        }

        return success;
    }

    public static void generateKeyPair(File file) {
        String PUBLIC_KEY_AS = null;
        String PRIVATE_KEY = null;
        Map<String, String> map = new HashMap<>();
        InputStream in = null;

        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Properties properties = new Properties();
            try {
                properties.load(in);
                if (!(propertiesKeyIsExist(properties, "PRIVATE_KEY") || propertiesKeyIsExist(properties, "PUBLIC_KEY_AS"))) {
                    map = generateKeyBytes();
                    PUBLIC_KEY_AS = map.get("PUBLIC_KEY");
                    PRIVATE_KEY = map.get("PRIVATE_KEY");
                    properties.setProperty("PUBLIC_KEY_AS", PUBLIC_KEY_AS);
                    properties.setProperty("PRIVATE_KEY", PRIVATE_KEY);
                    properties.store(new FileOutputStream(file),null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null)
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

            }
        }

    public static void writeKey(File file,String key,String value) {
        String PUBLIC_KEY_IDEA = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Properties properties = new Properties();
        try {

            properties.load(in);
            properties.setProperty(key, value);
            properties.store(new FileOutputStream(file),null);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }
    }

    public static String readKey(File file,String key){
        String value = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Properties properties = new Properties();
        try {
            properties.load(in);
            if (propertiesKeyIsExist(properties, key)) {
               value = properties.getProperty(key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }
        return value;
    }

    public static Integer keyCount(File file) {
        int count = 0;
        String value = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Enumeration<?> enu = properties.propertyNames();
        while (enu.hasMoreElements()) {
            enu.nextElement();
            count++;
        }
        if (in!=null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return count;

    }

}
