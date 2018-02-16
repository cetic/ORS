/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors;

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author fs
 */
public class ApplicationConstant {

    private static ApplicationConstant instance = null;
    private Properties properties;

    protected ApplicationConstant(String path) throws IOException {

        //InputStream file  = new FileInputStream(path);
        properties = new Properties();
        properties.load(ApplicationConstant.class.getResourceAsStream(path));

    }

    public final static ApplicationConstant getInstance(String path) throws IOException {

        if (ApplicationConstant.instance == null) {

            synchronized (ApplicationConstant.class) {
                if (ApplicationConstant.instance == null) {
                    ApplicationConstant.instance = new ApplicationConstant(path);
                }
            }
        }
        return ApplicationConstant.instance;
    }

    public String getPOJO_DIRECTORY() {
        return properties.getProperty("pojo_directory");
    }
    
//    public String getQUERY_DIRECTORY() {
//        return properties.getProperty("query_directory");
//    }
    
    public String getTARGET_DIRECTORY() {
        return properties.getProperty("target_directory");
    }
    
    public String getTARGET_PACKAGE() {
        return properties.getProperty("target_package");
    }


    public String getONTOLOGY_NAMESPACE(){
        return properties.getProperty("defaultNamespace");
    }
    
    /**
     * Return the corresponding namepace associated with the specified class
     * @param fullNameClass class name with package
     * @param className only the class name without package
     * 
     * @return the namespace associated with the class or the default namespace with the class
     */
    public String getONTOLOGY_NAMESPACE_CLASS(String fullNameClass, String className){
        return properties.getProperty(fullNameClass, properties.getProperty("defaultNamespace")+className);
    }
    
    /**
     * Return the corresponding namepace associated with the specified method
     * @param fullNameClass class name with package
     * @param variableName
     * @return the namespace associated with the class-variable or the default namespace with the variableName
     */
    public String getONTOLOGY_NAMESPACE_VARIABLE(String fullNameClass, String className, String variableName){
        //compute propertyName
        //be.cetic.ors.resource.Resource.Resource-currentLocation_geographicPointAltitude
        String property = fullNameClass+"."+className+"-"+variableName;
        return properties.getProperty(property, properties.getProperty("defaultNamespace")+className+"-"+variableName);
    }

    public String getDATAPROPERTY_URI(String propertyName){
     //   ExtendedIterator<OntProperty> xiter=ontclass.listDeclaredProperties(true);
      //  while (xiter.hasNext()){
       //     OntProperty prop= (OntProperty) xiter.next();
        //    if (prop.getLocalName().equals(propertyName)) return prop.getURI();
        //}
        return this.getONTOLOGY_NAMESPACE()+"/"+propertyName;
    }

    
//    
//    public String getDB_CONNECTION_FUSEKI_SELECT(){
//        return properties.getProperty("fuseki_DataEndpoint");
//    }
    
    
    /**
     * RDFS properties
     * 
     * @return 
     */
    public String getRDFS_LABEL_language(){
        return properties.getProperty("rdfs_label.language");
    }
    
    public String getRDFS_LABEL_value(){
        return properties.getProperty("rdfs_label.value");
    }

}
