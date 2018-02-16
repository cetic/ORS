/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.analyzer;

import static be.cetic.ors.Main.logger;
import com.github.javaparser.ParseException;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * condition :
 * http://stackoverflow.com/questions/20184546/how-to-generate-loops-and-conditionals-using-the-codemodel-library
 * Takes all attributes and methods(TODO) of a class and makes a hashmap.
 * @author fs
 */

public class SinglePojoAnalyzer extends Analyzer {

    //private String pathRDFClassConvertor;
   
    private AnalyzerResult analyzerResult;
    
    public SinglePojoAnalyzer(File javaFile) throws FileNotFoundException, ParseException, IOException, JClassAlreadyExistsException, ClassNotFoundException {

        super(javaFile);
     

//        PojoAnalyzer.MethodVisitor analyzerMethods = new MethodVisitor();
//        analyzerMethods.visit(cu, null);
//        logger.log(Level.INFO, "Methods size: {0}", analyzerMethods.methodsName.size());
        SinglePojoAnalyzer.VariableVisitor analyzerValiables = new SinglePojoAnalyzer.VariableVisitor();
        analyzerValiables.visit(cu, null);
        HashMap<String, List<String>> variables = analyzerValiables.variable;
        logger.log(Level.INFO, "Variables Set: {0}", Arrays.asList(variables));

        
        analyzerResult = new AnalyzerResult();
        analyzerResult.setAnalysedClass(analysedClass);
        analyzerResult.setAnalysedClassName(analysedClassName);
        analyzerResult.setVariables(variables);
        

    }

    public AnalyzerResult getAnalyzerResult() {
        return analyzerResult;
    }

  


}
