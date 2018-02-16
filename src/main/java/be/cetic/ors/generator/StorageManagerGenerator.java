/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.generator;

import be.cetic.ors.ApplicationConstant;
import be.cetic.ors.analyzer.AnalyzerResult;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JCase;
import com.helger.jcodemodel.JCast;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JDirectClass;
import com.helger.jcodemodel.JExpr;
import static com.helger.jcodemodel.JExpr.lit;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JForLoop;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import com.helger.jcodemodel.JSwitch;
import com.helger.jcodemodel.JVar;
import com.helger.jcodemodel.JWhileLoop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 *
 * @author fs
 */
public class StorageManagerGenerator extends Generator {

    public static Logger logger = Logger.getLogger(StorageManagerGenerator.class.getName());

    private JDefinedClass jc;
    private boolean classOpen = true;

    public StorageManagerGenerator(ApplicationConstant constants) throws GeneratorException, JClassAlreadyExistsException, IOException {
        super(constants, null);

        logger.info("Computing main class");

        // define package and classes
        JPackage jp = codeModel._package(targetPackage + ".manager");
        jc = jp._class("StorageManager");
        jc.javadoc().add("Generated class.");

    }
    
    /**
     * Create a RDFretrieve method for a super class
     *
     * @param analyzerResult analyser of the superClass
     * @param enumClassName enum in relation with the superClass
     * @return
     */
    public void createRDFretrieveAll(AnalyzerResult analyzerResult) throws GeneratorException {
        if (classOpen) {
            JDirectClass serializerManagerClass = codeModel.directClass("be.cetic.ors.ontologybinding.generated.model.SerializerManager");   
            AbstractJClass returnClass = codeModel.ref(ArrayList.class);
            AbstractJClass ontClassClass = codeModel.ref(OntClass.class);
            AbstractJClass ontResourceClass = codeModel.ref(OntResource.class);
            AbstractJClass resourceSerializer = codeModel.ref("be.cetic.ors.ontologybinding.generated.model.Model"+analyzerResult.getAnalysedClassName());
            AbstractJClass resourceClass = codeModel.ref(analyzerResult.getAnalysedClass());
            AbstractJClass returnClassEntities = returnClass.narrow(analyzerResult.getAnalysedClass());
            AbstractJClass extendedIteClassNarrow = codeModel.ref(ExtendedIterator.class).narrow(codeModel.ref(OntResource.class).wildcard());
            
            JMethod rdfRetrieveAll = jc.method(JMod.PUBLIC, returnClassEntities, "rdfRetrieve"+analyzerResult.getAnalysedClassName()+"s");
            JVar methodparam=rdfRetrieveAll.param(String.class, "id");
            rdfRetrieveAll._throws(Exception.class);
            
            // To generate a extra class -> SerializerManager we decide to skip this class generation
            JInvocation newSerializerManager = JExpr._new(serializerManagerClass);
            JVar serializerManager = rdfRetrieveAll.body().decl(serializerManagerClass, "serializerManager", newSerializerManager);
            rdfRetrieveAll.body()._return(serializerManager.invoke(analyzerResult.getAnalysedClassName()+"ToModel").arg(methodparam));
            
        } else {
            throw new GeneratorException("Cannot add more code to this class, he close method has been called");
        }
    }

    /**
     * Create a RDFstore method for a super class
     *
     * @param analyzerResult analyser of the superClass
     * @param enumClassName enum in relation with the superClass
     * @return
     */
    public RDFStore createRDFstore(AnalyzerResult analyzerResult, String enumClassName) throws GeneratorException {
        if (classOpen) {
            JDirectClass enumClass = codeModel.directClass(enumClassName);

            JMethod rdfStore = jc.method(JMod.PUBLIC, String.class, "rdfStore");
            rdfStore._throws(Exception.class);
            JVar rootClassArgument = rdfStore.param(analyzerResult.getAnalysedClass(), "rootClassArgument");
            JVar idCreated = rdfStore.body().decl(codeModel.ref(String.class), "idCreated");
            rdfStore.body().assign(idCreated, JExpr.lit("Error"));

            JInvocation expression = enumClass.staticInvoke("getResourceByClassName").arg(rootClassArgument.invoke("getClass").invoke("getName"));
            JVar enumVar = rdfStore.body().decl(codeModel.directClass(enumClassName), "enumVar", expression);

            JSwitch jswitch = rdfStore.body()._switch(enumVar);

            rdfStore.body()._return(idCreated);

            return new RDFStore(rootClassArgument, jswitch, idCreated);

        } else {
            throw new GeneratorException("Cannot add more code to this class, he close method has been called");
        }
    }

    /**
     * needed data to call addCase Function
     */
    public class RDFStore {

        private JVar rootClassArgument;
        private JSwitch jswitch;
        private JVar idCreated;

        public RDFStore(JVar rootClassArgument, JSwitch jswitch, JVar idCreated) {
            this.rootClassArgument = rootClassArgument;
            this.jswitch = jswitch;
            this.idCreated = idCreated;
        }
        
    }

    /**
     * Add a case to a selected switch with the treatment to convert the entity
     * to RDF
     *
     * @param superClass
     */
    public void addCase(RDFStore rdfs, JDefinedClass enumClass, String enumConstant, String subClassName) throws GeneratorException {
        if (classOpen) {
            JDirectClass rdfManagerClass = codeModel.directClass(targetPackage+".rdf."+subClassName.replaceFirst(".*.[.*]", "")+"RDF");
            JCase jcase = rdfs.jswitch._case(enumClass.enumConstant(enumConstant));

            //more treatment
            JCast cast = JExpr.cast(codeModel.directClass(subClassName), rdfs.rootClassArgument);
            JVar subClassVar = jcase.body().decl(codeModel.directClass(subClassName), "subClassVar", cast);
            
            JVar rdfManager = jcase.body().decl(rdfManagerClass, "rdfManager");
            jcase.body().add(rdfManager.assign(JExpr._new(rdfManagerClass)));
            jcase.body().add(rdfs.idCreated.assign(rdfManager.invoke("toRDF").arg(subClassVar)));
            jcase.body().add(rdfManager.invoke("commit"));
            jcase.body()._break();
            
        } else {
            throw new GeneratorException("Cannot add more code to this class, he close method has been called");
        }
    }

    public void close() throws IOException {
        codeModel.build(new File(targetDirectory));
        classOpen = false;
    }

}
