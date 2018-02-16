/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.analyzer;

import static be.cetic.ors.Main.logger;
import static be.cetic.ors.Main.classLoader;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author fs
 */
public class Analyzer {

    String analysedClassName;
    String analysedClassFullName;
    Class analysedClass;
    CompilationUnit cu;
    ArrayList<String> hierarchy;

    public Analyzer(File javaFile) throws FileNotFoundException, ParseException, IOException, ClassNotFoundException  {
        logger.log(Level.INFO, "Parsing: {0}", javaFile.getName());
        
    

        FileInputStream in = new FileInputStream(javaFile);

        try {
            // parse the file
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }

        logger.log(Level.INFO, "Loading: {0}", javaFile.getName());

        analysedClassName = javaFile.getName().replaceFirst("[.][^.]+$", "");
        String packageName = cu.getPackage().getName().toString();
        
        analysedClass = Class.forName(packageName + "." + analysedClassName, true, classLoader);
        
        // class hierarchy [java.lang.object, T1, T2, ... , this object ]
        hierarchy = getClassHierarchy(analysedClass, new ArrayList<>());
        Collections.reverse(hierarchy);
        logger.log(Level.INFO, "hierarchy: {0}", Arrays.toString(hierarchy.toArray()));
                
    }
    
    /**
     * Compute the class hierarchy
     * @param child
     * @param hierarchy
     * @return 
     */
    private ArrayList<String> getClassHierarchy(Class child, ArrayList<String> hierarchy){
        if (child.getName().equals("java.lang.Object")){
            hierarchy.add(child.getName());
            return hierarchy;
        }else{
            hierarchy.add(child.getName());
            return getClassHierarchy(child.getSuperclass(), hierarchy);
        }
    }
            
    protected class VariableVisitor extends VoidVisitorAdapter {

        // variable type, nariable name
        HashMap<String, List<String>> variable = new HashMap<String, List<String>>();

        @Override
        public void visit(FieldDeclaration n, Object arg) {

            for (VariableDeclarator vd : n.getVariables()) {
                    logger.info("Analyzer is addding variable "+vd.getId().getName());
                // first we avoid internal classes. Internal classes are merged
                if (!n.getType().toString().contains(".")) {
                    if (!variable.containsKey(n.getType().toString())) {
                        variable.put(n.getType().toString(), new ArrayList<String>());
                    }
                    variable.get(n.getType().toString()).add(vd.getId().getName());
                    logger.info("Analyzer is addding variable "+vd.getId().getName()+" with type"+n.getType().toString());
                    //+test
                    if (!n.getType().toString().contains("String")) logger.info("#######Analyzer is addding variable "+vd.getId().getName()+" with type"+n.getType().toString());
                    /*if (n.getType().toString().equals("Station")) {

                        logger.log(Level.INFO, "XXZ: {0}", n.getData());
                        logger.log(Level.INFO, "XXX: {0}", n.getChildrenNodes().get(0).getChildrenNodes());
                    }*/
                    //+test
                }
            }
            super.visit(n, arg); //To change body of generated methods, choose Tools | Templates.
        }
    }

    protected class MethodVisitor extends VoidVisitorAdapter {

        List<String> methodsName = new ArrayList<String>();

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            // here you can access the attributes of the method.
            // this method will be called for all methods in this 
            // CompilationUnit, including inner class methods
            methodsName.add(n.getName());
            System.out.println(n.getName());
            super.visit(n, arg);
        }
    }

    protected class ClassVisitor extends VoidVisitorAdapter {

        List<String> methodsName = new ArrayList<String>();

//        @Override
//        public void visit(TypeExpr n, Object arg) {
//            System.out.println("****"+n.getType().toString());
//            super.visit(n, arg); //To change body of generated methods, choose Tools | Templates.
//        }
        @Override
        public void visit(ClassOrInterfaceType n, Object arg) {
            System.out.println("*****" + n.getName());
            super.visit(n, arg); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
