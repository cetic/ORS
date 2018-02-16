/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.generator;

import be.cetic.ors.ApplicationConstant;
import be.cetic.ors.analyzer.AnalyzerResult;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JDirectClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import com.helger.jcodemodel.JVar;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JFieldRef;
import com.helger.jcodemodel.JWhileLoop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.rdf.model.StmtIterator;

/**
 *
 * @author fs
 */
public class SerializerManagerBuilder extends Generator{
    
    public static Logger logger = Logger.getLogger(StorageManagerGenerator.class.getName());
    private JDefinedClass jc;
    private boolean classOpen;
    JFieldVar loggerField;

    public SerializerManagerBuilder(ApplicationConstant constants)
            throws GeneratorException, JClassAlreadyExistsException, IOException{
        
        super(constants,null);
        classOpen = true;
        
        AbstractJClass dbManager = codeModel.ref("be.cetic.ors.ontologybinding.generic.ToDBManager");
        JPackage jp = codeModel._package(targetPackage + ".model");
        jc = jp._class("SerializerManager");
        jc._extends(dbManager);
        jc.javadoc().add("Generated class.");
        
        AbstractJClass loggerClass =  codeModel.directClass ("java.util.logging.Logger");
        JInvocation logValue= loggerClass.staticInvoke("getLogger").arg(jc.name());    
        loggerField = jc.field(JMod.PUBLIC | JMod.STATIC, Logger.class, "logger", logValue);
        
        JMethod constr = jc.constructor(JMod.PUBLIC);
        constr._throws(IOException.class);
    }
    
    
    public void addClass(AnalyzerResult analyzerResult) throws GeneratorException{

        if (classOpen){

            AbstractJClass returnClass = codeModel.ref(ArrayList.class);
            AbstractJClass ontClassClass = codeModel.ref(OntClass.class);
            AbstractJClass ontResourceClass = codeModel.ref(OntResource.class);
            AbstractJClass stmtIter = codeModel.ref(StmtIterator.class);
            AbstractJClass resourceToModel = codeModel.ref("be.cetic.ors.ontologybinding.generated.model.Model"+analyzerResult.getAnalysedClassName());
            AbstractJClass resourceClass = codeModel.ref(analyzerResult.getAnalysedClass());
            AbstractJClass returnClassEntities = returnClass.narrow(analyzerResult.getAnalysedClass());
            AbstractJClass extendedIteClassNarrow = codeModel.ref(ExtendedIterator.class).narrow(codeModel.ref(OntResource.class).wildcard());
            JMethod serRetrieveAll = jc.method(JMod.PUBLIC, returnClassEntities, analyzerResult.getAnalysedClassName()+"ToModel");
            JVar methodparam=serRetrieveAll.param(String.class, "id");
            serRetrieveAll._throws(Exception.class);

            // Generate:  List<Event> resourceList = new ArrayList<Event>();
            JInvocation resourceLists = JExpr._new(returnClassEntities);
            JVar resourceList = serRetrieveAll.body().decl(returnClassEntities, "resourceList", resourceLists);

            // Get the URI of the ontology
            JVar typeClass = serRetrieveAll.body().decl(ontClassClass, "typeClass", JExpr.ref("ontmodel").invoke("getOntClass").arg(resourceToModel.staticRef("URI")));

            //Build a condition for either retrieving the OntClass or creating it if the type is defined in the resources list.
            JConditional conditional= serRetrieveAll.body()._if((typeClass.eq(JExpr._null())));
            conditional._then().assign( typeClass, JExpr.ref("ontmodel").invoke("createClass").arg(resourceToModel.staticRef("URI")));
            JFieldRef classURI= resourceToModel.staticRef("URI");
            JInvocation log=loggerField.invoke("info").arg(classURI);
            conditional._then().add(log);

            JConditional conditional2 =  serRetrieveAll.body()._if((typeClass.eq(JExpr._null())));
            conditional2._then()._return(resourceList);

            // Get the ontology instances iterator
            JVar typeClassIt = serRetrieveAll.body().decl(extendedIteClassNarrow, "typeClassIt", typeClass.invoke("listInstances").arg(false));

            // Get the class properties iterator
            JVar properties = serRetrieveAll.body().decl(stmtIter, "properties", typeClass.invoke("listProperties"));
            JInvocation log2=loggerField.invoke("info").arg(properties.invoke("toList").invoke("toString"));
            serRetrieveAll.body().add(log2);

            JWhileLoop whileLoop = serRetrieveAll.body()._while(typeClassIt.invoke("hasNext"));
            JVar ontResource = whileLoop.body().decl(ontResourceClass, "ontResource", typeClassIt.invoke("next"));
            JVar resource = whileLoop.body().decl(resourceClass, "resource", JExpr._new(resourceToModel).arg(ontResource.invoke("asIndividual")).invoke("toModel"));
            JConditional ifallresourceORtheResource=whileLoop.body()._if((methodparam.eq(JExpr._null())).cor(ontResource.invoke("hasURI").arg(methodparam)));
            ifallresourceORtheResource._then().add(resourceList.invoke("add").arg(resource));
            serRetrieveAll.body()._return(resourceList);
        }
        else{
            throw new GeneratorException("Cannot add more code to this class, he close method has been called");
        }
    }
    
    public void close() throws IOException {
        codeModel.build(new File(targetDirectory));
        classOpen = false;
    }
    
    
}
