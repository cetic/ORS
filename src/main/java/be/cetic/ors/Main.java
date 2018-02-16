/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors;

import be.cetic.ors.analyzer.AnalyzerResult;
import be.cetic.ors.generator.RDFConvertor;
import be.cetic.ors.analyzer.PojosAnalyzer;
import be.cetic.ors.analyzer.Tree;
import be.cetic.ors.generator.GeneratorException;
import be.cetic.ors.generator.StorageManagerEnumGenerator;
import be.cetic.ors.generator.StorageManagerGenerator;
import be.cetic.ors.generator.WSGenerator;
import be.cetic.ors.generator.SerializerConvertor;
import be.cetic.ors.generator.SerializerManagerBuilder;
import com.github.javaparser.ParseException;

import com.helger.jcodemodel.JClassAlreadyExistsException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;

/**
 * This is the main class Step 1. Run xjc to produce java class files from Serialized form.
 * Step 2. This program reads the configuration file and parses the java
 * classes, so the model is generated (using the analyser). Step 3. The
 * generators are being used in order to produce RDF or the serialized format.
 *
 * @author fs
 */
public class Main {

    ApplicationConstant applicationConstant;
    //String generatedClassPath;

    //This hashmap holds the model. It is a hashmap of classnames and class attributes objects (AbstractSyntaxTree).
    HashMap<String, AnalyzerResult> analyzerResults = new HashMap<>();

    //This tree contains the inheritence hierarchy of the model classes
    Tree<String> modelClassHierarchy;

    public static Logger logger = Logger.getLogger(Main.class.getName());

    public static ClassLoader classLoader = null;

    public Main() throws IOException, FileNotFoundException, ParseException, JClassAlreadyExistsException, ClassNotFoundException {
        //This is mainly the conversion of namespaces (classes and attributes see resources directory) - mentioned earlier as the configuration
        applicationConstant = ApplicationConstant.getInstance(SessionData.getInstance().getPathProperties());
        logger.log(Level.INFO, "Config file loaded: {0}", SessionData.getInstance().getPathProperties());
        String path = applicationConstant.getPOJO_DIRECTORY();
        List<File> javafiles = getJavaClasses(path);
        classLoader = initClassLoader(path);
        PojosAnalyzer pojosAnalyser = new PojosAnalyzer(javafiles);
        analyzerResults = pojosAnalyser.getAnalyzerResults();
        modelClassHierarchy = pojosAnalyser.getRootHierarchy();
    }

    //Calls RDFConvertor for each of the classes in the analyzer hashmap.
    public void rdfConvertor() {
        analyzerResults.forEach((k, v) -> {
            try {
                new RDFConvertor(applicationConstant, v).buildRDFClass();
            } catch (JClassAlreadyExistsException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    //Converts RDF to Model (to java classes)
    public void modelConvertor() throws GeneratorException, JClassAlreadyExistsException, IOException {
        SerializerManagerBuilder xMLManagerBuilder = new SerializerManagerBuilder(applicationConstant);
        analyzerResults.forEach((k, v) -> {
            try {
                new SerializerConvertor(applicationConstant, v).buildSerializerClass();
                xMLManagerBuilder.addClass(v);
            } catch (JClassAlreadyExistsException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GeneratorException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        xMLManagerBuilder.close();

    }

    //Generate WS classes for CRUD operations
    public void WebServicesGenerator() throws GeneratorException, JClassAlreadyExistsException, IOException {
        StorageManagerGenerator smg = new StorageManagerGenerator(applicationConstant);
        for (Tree<String> superClass : modelClassHierarchy.getModelClasses(modelClassHierarchy, null)) {// get all classes
            logger.log(Level.INFO, "Generating WS for this class {0}", superClass.getData());
            WSGenerator wsg = new WSGenerator(applicationConstant, analyzerResults, superClass.getData());
            AnalyzerResult analyzerResult = wsg.buildClass(superClass.getData());// generate rest WS
            // We have to create one enum for each superClass
            StorageManagerEnumGenerator smeg = new StorageManagerEnumGenerator(applicationConstant,superClass.getData());
            // We have to create one method to store data comming from this superClass
            StorageManagerGenerator.RDFStore rdfStore = smg.createRDFstore(analyzerResult, smeg.getClassName());
            // We have to create one method to retrieve all data (instances) from (linked to ) this superclass
            smg.createRDFretrieveAll(analyzerResult);
            logger.log(Level.INFO, "Getting subclasses that are going to use the same WS");
            for (String subClasse: superClass.getAllChildData()){
                logger.log(Level.INFO, "Subclass found: {0} of the main class: {1}", new Object[]{subClasse,superClass.getData()});
                //adding enum
                String enumConst = smeg.addSubClass(applicationConstant.getONTOLOGY_NAMESPACE_CLASS(subClasse, analyzerResult.getAnalysedClassName()),
                        subClasse,
                        subClasse.replaceFirst(".*.[.*]", ""));
                //adding manager files
                smg.addCase(rdfStore, smeg.getJc(),enumConst,subClasse);
            }
            smeg.close();
           
        }
        smg.close();
    }

    /**
     * Loads all java classes in the specified directory. These should only be
     * the model classes. Also loads all .class files into memory.
     *
     * @return
     * @throws IOException
     */
    private List<File> getJavaClasses(String path) throws IOException {

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        ArrayList<File> javaFiles = new ArrayList<File>();
        String fileName;
        for (int i = 0; listOfFiles != null && i < listOfFiles.length; i++) {

            if (listOfFiles[i].isFile()) {
                fileName = listOfFiles[i].getName();
                if (fileName.endsWith(".java")) {
                    logger.log(Level.INFO, "This file will be added: {0}", fileName);
                    javaFiles.add(listOfFiles[i]);
                }
            }
        }
        return javaFiles;
    }

    //I am using the URLClassLoader to dynamically load classes from the pojos directory.
    private URLClassLoader initClassLoader(String path) throws IOException {
        URL tmpurl = null;
        URLClassLoader ucl = null;
        try {
            File dir = new File(path);
            tmpurl = (dir.toURI()).toURL();
            logger.log(Level.INFO, "Loading from url: {0}", tmpurl);
            URL[] tmpurls = new URL[]{tmpurl};
            ucl = new URLClassLoader(tmpurls);
        } catch (MalformedURLException e) {
            logger.log(Level.INFO, "Malformed URL {0}", tmpurl);
            throw new IOException(e.getMessage());
        }
        return ucl;
    }

    public static void main(String[] argv) throws IOException, FileNotFoundException, ParseException, JClassAlreadyExistsException, ClassNotFoundException {
        try {
            SessionData.getInstance().setPathProperties("/namespaceConfig.properties");
            Main main = new Main();
            main.rdfConvertor();
            main.modelConvertor();
            main.WebServicesGenerator();
        } catch (GeneratorException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
