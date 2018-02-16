/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.generator;

import be.cetic.ors.ApplicationConstant;
import be.cetic.ors.analyzer.AnalyzerResult;
import com.helger.jcodemodel.JCodeModel;
import java.util.logging.Logger;

/**
 *
 * @author fs
 */
public class Generator {

     ApplicationConstant constants;
     JCodeModel codeModel;
     AnalyzerResult analyzerResult;

     String targetPackage;
     String targetDirectory;

    private static Logger logger = Logger.getLogger(Generator.class.getName());

    public Generator(ApplicationConstant constants, AnalyzerResult analyzerResult) {
        this.analyzerResult = analyzerResult;
        this.constants = constants;

        // class generator
        codeModel = new JCodeModel();

        //Specifies the package name of all java files.
        targetPackage = constants.getTARGET_PACKAGE();

        //This specifies the directory where the java files will be put
        targetDirectory = constants.getTARGET_DIRECTORY();
    }
    
}
