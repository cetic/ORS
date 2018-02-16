/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.generator;

import be.cetic.ors.ApplicationConstant;

import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JDirectClass;
import com.helger.jcodemodel.JExpr;
import static com.helger.jcodemodel.JExpr.lit;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JForLoop;

import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;

import com.helger.jcodemodel.JVar;
import java.io.File;
import java.io.IOException;

import java.util.logging.Logger;

/**
 *
 * @author fs
 */
public class StorageManagerEnumGenerator extends Generator {

    public static Logger logger = Logger.getLogger(StorageManagerEnumGenerator.class.getName());

    private JDefinedClass jc;
    private String className;
    private boolean classOpen = true;

    public StorageManagerEnumGenerator(ApplicationConstant constants, String modelClassPath) throws GeneratorException, JClassAlreadyExistsException, IOException {
        super(constants, null);

        String modelClassName = modelClassPath.replaceFirst(".*.[.*]", "");
        String modelClassNamePackage = modelClassPath;
        
        JDirectClass superClass = codeModel.directClass(modelClassNamePackage);

        logger.info("Computing main class");
        
        // define package and classes
        JPackage jp = codeModel._package(targetPackage+".manager");
        jc = jp._enum("Enum" + modelClassName);
        className = targetPackage+".manager."+"Enum" + modelClassName;
        //jc._extends(fusekiManager);
        jc.javadoc().add("Generated class.");

        //variables
        JFieldVar uri = jc.field(JMod.PRIVATE | JMod.FINAL, String.class, "uri");
        JFieldVar className = jc.field(JMod.PRIVATE | JMod.FINAL, String.class, "className");
        JFieldVar name = jc.field(JMod.PRIVATE | JMod.FINAL, String.class, "name");
        
        //constructor
        JMethod constr = jc.constructor(JMod.PRIVATE);
        JVar newUri = constr.param(String.class, "uri");
        JVar newClassName = constr.param(String.class, "className");
        JVar newName = constr.param(String.class, "name");
        
        constr.body().add(JExpr._this().ref(uri).assign(newUri));
        constr.body().add(JExpr._this().ref(className).assign(newClassName));
        constr.body().add(JExpr._this().ref(name).assign(newName));
        
        // ascessors
        jc.method(JMod.PUBLIC, String.class, "getName").body()._return(name);
        jc.method(JMod.PUBLIC, String.class, "getClassName").body()._return(className);
        jc.method(JMod.PUBLIC, String.class, "getUri").body()._return(uri);
        
        // method GetByClassName (return an instance of the enum class that is relative to the given classPackageName)
        JMethod getByClassName = jc.method(JMod.PUBLIC| JMod.STATIC, jc, "getResourceByClassName");
        getByClassName._throws(Exception.class);
        
        JVar classPackageName = getByClassName.param(String.class, "classPackageName");
        
        JForLoop forLoop = getByClassName.body()._for();
        JVar ivar = forLoop.init(codeModel.INT, "i", JExpr.lit(0));
        forLoop.test(ivar.lt(JExpr.invoke("values").ref("length")));
        forLoop.update(ivar.assignPlus(JExpr.lit(1)));
        
        JConditional condition = forLoop.body()._if(JExpr.invoke("values").component(ivar).invoke("getClassName").invoke("equals").arg(classPackageName));
        condition._then()._return(JExpr.invoke("values").component(ivar));
        
        getByClassName.body()._throw(JExpr._new(codeModel._ref(Exception.class)).arg("The requested resource modelClassPath doesn't exist or is not yet configurated"));

    }

    /**
     * Add a new enum constant
     * @param uri
     * @param className
     * @param name
     * @return
     * @throws GeneratorException 
     */
    public String addSubClass(String uri, String className, String name) throws GeneratorException {
        if (classOpen) {
            String enumConst = name.toUpperCase();
            jc.enumConstant(enumConst).arg(lit(uri)).arg(lit(className)).arg(lit(name));
            return enumConst;
        } else {
            throw new GeneratorException("Cannot add more code to this class, he close method has been called");
        }
    }

    public void close() throws IOException {
        codeModel.build(new File(targetDirectory));
        classOpen = false;
    }

    public String getClassName() {
        return className;
    }

    public JDefinedClass getJc() {
        return jc;
    }
    
    
    

}
