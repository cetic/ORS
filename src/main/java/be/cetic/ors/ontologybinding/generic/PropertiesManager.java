/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.ontologybinding.generic;

import java.io.*;
import java.util.Properties;

/**
 *
 * @author fs
 */
public class PropertiesManager {

    private static PropertiesManager instance = null;
    private Properties properties;

    protected PropertiesManager() throws IOException {

        InputStream file
                = getClass().getClassLoader().getResourceAsStream("config.properties");
        properties = new Properties();
        properties.load(file);

    }

    public final static PropertiesManager getInstance() throws IOException {

        if (PropertiesManager.instance == null) {

            synchronized (PropertiesManager.class) {
                if (PropertiesManager.instance == null) {
                    PropertiesManager.instance = new PropertiesManager();
                }
            }
        }
        return PropertiesManager.instance;
    }

    public String getValue(String key) {
        return properties.getProperty(key);
    }

}
