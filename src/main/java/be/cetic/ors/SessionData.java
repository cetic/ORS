/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author fs
 */
public class SessionData {
      private static SessionData instance = null;
    private String pathProperties;

    protected SessionData() throws IOException {

       

    }

    public final static SessionData getInstance() throws IOException {

        if (SessionData.instance == null) {

            synchronized (SessionData.class) {
                if (SessionData.instance == null) {
                    SessionData.instance = new SessionData();
                }
            }
        }
        return SessionData.instance;
    }

    public String getPathProperties() {
        return pathProperties;
    }

    public void setPathProperties(String pathProperties) {
        this.pathProperties = pathProperties;
    }

    
    
}
