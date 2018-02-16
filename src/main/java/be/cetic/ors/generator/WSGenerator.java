/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.generator;

import be.cetic.ors.ApplicationConstant;
import be.cetic.ors.analyzer.AnalyzerResult;
import be.cetic.ors.analyzer.PrimitiveTypeException;
import static be.cetic.ors.generator.RDFConvertor.logger;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCatchBlock;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JDirectClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import com.helger.jcodemodel.JTryBlock;
import com.helger.jcodemodel.JVar;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.GenericEntity;

/**
 *
 * @author fs
 */
public class WSGenerator extends Generator {

    public static Logger logger = Logger.getLogger(WSGenerator.class.getName());
    String modelClassName;
    String modelClassNamePackage;
    JFieldVar loggerField;

    public WSGenerator(ApplicationConstant constants, HashMap<String, AnalyzerResult> analyzerResults, String modelClassPath) throws GeneratorException {
        // 2 nd parameter is set to null because we have to check if the class has been already analysed
        super(constants, null);

        modelClassName = modelClassPath.replaceFirst(".*.[.*]", "");
        modelClassNamePackage = modelClassPath;

        if (analyzerResults.containsKey(modelClassPath)) {// && analyzerResults.get(modelClassName).getAnalysedClass().getName().equals(modelClassNamePackage)) {
            // the model has been analysed
            analyzerResult = analyzerResults.get(modelClassPath);
        } else {
            // the model has not been already analysed. We cannot go further
            throw new GeneratorException("WSGenerator the following model: " + modelClassNamePackage + " was not been analysed");
        }

    }

    /**
     * Generate the Jax-RS code for the class given
     * It returns an instance of the analyser-result of the given class 
     * @param modelClassPath: string contaning the name of the class model and its pakage (e.g.: foo.bar.Classe)
     * @return
     * @throws JClassAlreadyExistsException
     * @throws IOException 
     */
    public AnalyzerResult buildClass(String modelClassPath) throws JClassAlreadyExistsException, IOException {

        logger.info("Computing main class");
        
        // add dependencies
        // Extends the ToRDFMAnager that contains all generic methods
        JDirectClass storageManager = codeModel.directClass("be.cetic.ors.ontologybinding.generated.manager.StorageManager");
        JDirectClass modelClass = codeModel.directClass(modelClassPath);

        // define package and classes
        JPackage jp = codeModel._package(targetPackage+".rs");
        JDefinedClass jc = jp._class("Rest" + modelClassName);
        //jc._extends(fusekiManager);
        jc.javadoc().add("Generated class.");

        jc.annotate(codeModel.ref("javax.ws.rs.Path")).param("value", "/" + modelClassName);

        //generating logger cariable
        
        AbstractJClass loggerClass =  codeModel.directClass ("java.util.logging.Logger");

        JInvocation logValue= loggerClass.staticInvoke("getLogger").arg("Rest"+modelClassName);
        //JInvocation value= JExpr.invoke("Logger.getLogger").arg("Rest"+modelClassName);
        loggerField = jc.field(JMod.PUBLIC | JMod.STATIC, Logger.class, "logger", logValue);

        // generating methods
        buildRegister(jc, storageManager);
        buildQuery(jc, storageManager);
        buildGets(jc, storageManager);

        codeModel.build(new File(targetDirectory));
        
        return analyzerResult;

    }

    /**
     * Generate the REST C*R*UD method
     * @param jc
     * @param jcStorageManager
     * @throws JClassAlreadyExistsException
     * @throws IOException 
     */
    private void buildGets(JDefinedClass jc, JDirectClass jcStorageManager) throws JClassAlreadyExistsException, IOException {
        
        logger.info("Generating Register (Get) method");
        JDirectClass wsResponse = codeModel.directClass("javax.ws.rs.core.Response");
        JDirectClass uri = codeModel.directClass("java.net.URI");
        JDirectClass model = codeModel.directClass(modelClassNamePackage);
        
        // create register method
        JMethod registerMethod = jc.method(JMod.PUBLIC, wsResponse, "get" + modelClassName + "s");
        JVar methodparam=registerMethod.param(String.class, "id");
        
        //annotation
        methodparam.annotate(codeModel.ref("javax.ws.rs.QueryParam")).param("value", "id");
        registerMethod.annotate(codeModel.ref("javax.ws.rs.Path")).param("value", "/");
        registerMethod.annotate(codeModel.ref("javax.ws.rs.GET"));
        registerMethod.annotate(codeModel.ref("javax.ws.rs.Consumes")).param("value", "application/xml");

        //try catch
        JTryBlock tryBlock = registerMethod.body()._try();
        
        // initialize storageManager
        JVar storageManager = tryBlock.body().decl(jcStorageManager, "storageManager");
        storageManager.init(JExpr._new(jcStorageManager));

        //create a pojo object 
        //AbstractJClass pojoClass= codeModel.ref(modelClassNamePackage);
        //JDirectClass pojoClass =  codeModel.directClass(modelClassNamePackage);
        //JVar pojo=tryBlock.body().decl(pojoClass, "pojo");
        //pojo.init(JExpr._new(pojoClass));


        // call storageManager Method
        JInvocation expression = storageManager.invoke("rdfRetrieve"+modelClassName+"s").arg(methodparam);
        AbstractJClass arrayListClass = codeModel.ref(ArrayList.class);
        AbstractJClass arrayListOfEntities = arrayListClass.narrow(analyzerResult.getAnalysedClass());
        JVar entityList = tryBlock.body().decl(arrayListOfEntities, "entityList", expression);
        
        AbstractJClass genericEntity = codeModel.ref(GenericEntity.class);
        AbstractJClass genericEntityNarrow = genericEntity.narrow(arrayListClass.narrow(analyzerResult.getAnalysedClass()));
        AbstractJClass aGeneric=codeModel.anonymousClass(genericEntityNarrow);
        JInvocation rhsAssignmentExpression = JExpr._new(aGeneric).arg(JExpr.ref(entityList));
        
        JVar generic = tryBlock.body().decl(aGeneric, "generic", rhsAssignmentExpression);

        // return method
        JInvocation responseInvoke = wsResponse.staticInvoke("ok").arg(generic).arg("application/xml");
        tryBlock.body()._return(responseInvoke.invoke("build"));
        
        JCatchBlock ioException = tryBlock._catch(codeModel.ref(IOException.class)); //rdfstore
        buildCatchExceptionReturn(ioException, wsResponse, "INTERNAL_SERVER_ERROR");
        JCatchBlock URIsyntaxException = tryBlock._catch(codeModel.ref(URISyntaxException.class)); //response
        buildCatchExceptionReturn(URIsyntaxException, wsResponse, "INTERNAL_SERVER_ERROR");
        JCatchBlock badRequestException = tryBlock._catch(codeModel.ref("be.cetic.ors.ontologybinding.generic.exception.BadRequestException")); //badRequest
        buildCatchExceptionReturn(badRequestException, wsResponse, "INTERNAL_SERVER_ERROR");
        JCatchBlock xmlAnalysisException = tryBlock._catch(codeModel.ref("be.cetic.ors.ontologybinding.generic.exception.XmlAnalysisException")); //rdfstore
        buildCatchExceptionReturn(xmlAnalysisException, wsResponse, "INTERNAL_SERVER_ERROR");
        JCatchBlock classURIException = tryBlock._catch(codeModel.ref("be.cetic.ors.ontologybinding.generic.exception.ClassURIException")); //rdfstore
        buildCatchExceptionReturn(classURIException, wsResponse, "INTERNAL_SERVER_ERROR");
        JCatchBlock exception = tryBlock._catch(codeModel.ref(Exception.class));// not defined error -> HTTP 500
        buildCatchExceptionReturn(exception, wsResponse, "INTERNAL_SERVER_ERROR");
        
        
    }
    
    /**
     * Generate the REST *C*RUD method
     * @param jc
     * @param jcStorageManager
     * @throws JClassAlreadyExistsException
     * @throws IOException 
     */
    private void buildRegister(JDefinedClass jc, JDirectClass jcStorageManager) throws JClassAlreadyExistsException, IOException {
        
        logger.info("Generating Register (POST) method");
        JDirectClass wsResponse = codeModel.directClass("javax.ws.rs.core.Response");
        JDirectClass uri = codeModel.directClass("java.net.URI");
        JDirectClass model = codeModel.directClass(modelClassNamePackage);

        // create register method
        JMethod registerMethod = jc.method(JMod.PUBLIC, wsResponse, "register" + modelClassName);
        JVar registerMethodModel = registerMethod.param(analyzerResult.getAnalysedClass(), analyzerResult.getAnalysedClass().getSimpleName().toLowerCase());

        //annotation
        registerMethod.annotate(codeModel.ref("javax.ws.rs.Path")).param("value", "/");
        registerMethod.annotate(codeModel.ref("javax.ws.rs.POST"));
        registerMethod.annotate(codeModel.ref("javax.ws.rs.Consumes")).param("value", "application/xml");

        //try catch
        JTryBlock tryBlock = registerMethod.body()._try();
        
        // initialize storageManager
        JVar storageManager = tryBlock.body().decl(jcStorageManager, "storageManager");
        storageManager.init(JExpr._new(jcStorageManager));

        // call storageManager Method
        JInvocation expression = storageManager.invoke("rdfStore").arg(registerMethodModel);
        JVar identifiant = tryBlock.body().decl(codeModel._ref(String.class), "identifiant", expression);

        // return method
        JInvocation responseInvoke = wsResponse.staticInvoke("created").arg(JExpr._new(uri).arg(identifiant));
        tryBlock.body()._return(responseInvoke.invoke("build"));
        
        JCatchBlock ioException = tryBlock._catch(codeModel.ref(IOException.class)); //rdfstore
        buildCatchExceptionReturn(ioException, wsResponse, "INTERNAL_SERVER_ERROR");
        JCatchBlock URIsyntaxException = tryBlock._catch(codeModel.ref(URISyntaxException.class)); //response
        buildCatchExceptionReturn(URIsyntaxException, wsResponse, "INTERNAL_SERVER_ERROR");
        JCatchBlock badRequestException = tryBlock._catch(codeModel.ref("be.cetic.ors.ontologybinding.generic.exception.BadRequestException")); //badRequest
        buildCatchExceptionReturn(badRequestException, wsResponse, "INTERNAL_SERVER_ERROR");
        JCatchBlock xmlAnalysisException = tryBlock._catch(codeModel.ref("be.cetic.ors.ontologybinding.generic.exception.XmlAnalysisException")); //rdfstore
        buildCatchExceptionReturn(xmlAnalysisException, wsResponse, "INTERNAL_SERVER_ERROR");
        JCatchBlock classURIException = tryBlock._catch(codeModel.ref("be.cetic.ors.ontologybinding.generic.exception.ClassURIException")); //rdfstore
        buildCatchExceptionReturn(classURIException, wsResponse, "INTERNAL_SERVER_ERROR");
        JCatchBlock exception = tryBlock._catch(codeModel.ref(Exception.class));// not defined error -> HTTP 500
        buildCatchExceptionReturn(exception, wsResponse, "INTERNAL_SERVER_ERROR");
        
        
    }
    
    /**
     * Create a catch block that returns a WS error code
     * @param catchBlock
     * @param wsResponse 
     * @param resultRequest
     *  could be: BAD_REQUEST 400, INTERNAL_SERVER_ERROR 500, ...
     */
    private void buildCatchExceptionReturn(JCatchBlock catchBlock, JDirectClass wsResponse, String resultRequest ){
        JVar exceptionParam=catchBlock.param("e");
        //JExpr.invoke(
        JInvocation log=loggerField.invoke("log").arg(codeModel.ref(java.util.logging.Level.class).staticRef("WARNING")).arg("Error").arg(exceptionParam);
        JInvocation catchReturn = wsResponse.staticInvoke("status");
        catchReturn.arg(wsResponse.staticRef("Status").ref(resultRequest));
        catchBlock.body().add(log);
        catchBlock.body()._return(catchReturn.invoke("entity").arg(exceptionParam.invoke("getMessage")).invoke("build"));
    }
    
    private void buildGetOne(JDefinedClass jc, JDirectClass jcStorageManager) throws JClassAlreadyExistsException, IOException {
         logger.info("Implemented as get all with optional filter by id");
    }
    
    private void buildDelete(JDefinedClass jc, JDirectClass jcStorageManager) throws JClassAlreadyExistsException, IOException {
         logger.info("Generating Delete method");
    }
    
    private void buildUpdate(JDefinedClass jc, JDirectClass jcStorageManager) throws JClassAlreadyExistsException, IOException {
         logger.info("Generating Update (PUT) method");
    }
    
    private void buildQuery(JDefinedClass jc, JDirectClass jcStorageManager) throws JClassAlreadyExistsException, IOException {
         logger.info("Generating Query (PUT) method");
        JDirectClass wsResponse = codeModel.directClass("javax.ws.rs.core.Response");
        JDirectClass uri = codeModel.directClass("java.net.URI");
        JDirectClass model = codeModel.directClass(modelClassNamePackage);
        AbstractJClass queryClass = codeModel.ref("generated.Query");
        AbstractJClass arrayListClass = codeModel.ref(ArrayList.class);
        AbstractJClass arrayListOfjc = arrayListClass.narrow(jc);
        
        // create register method
        JMethod registerMethod = jc.method(JMod.PUBLIC, wsResponse, "query" + modelClassName);
        JVar registerMethodModel = registerMethod.param(queryClass, "query");

        //annotation
        registerMethod.annotate(codeModel.ref("javax.ws.rs.Path")).param("value", "/query");
        registerMethod.annotate(codeModel.ref("javax.ws.rs.PUT"));
        registerMethod.annotate(codeModel.ref("javax.ws.rs.Consumes")).param("value", "application/xml");
        registerMethod.annotate(codeModel.ref("javax.ws.rs.Produces")).param("value", "application/xml");

        //ArrayList initialisation
        JVar uris = registerMethod.body().decl(arrayListOfjc, "uris");
        uris.init(JExpr._new(arrayListOfjc));
        
        //try catch
        JTryBlock tryBlock = registerMethod.body()._try();
        
        //TODO: queryManager to implement
       
        // return method
        JInvocation responseInvoke = wsResponse.staticInvoke("ok").arg(uris);
        tryBlock.body()._return(responseInvoke.invoke("build"));
        
//        JCatchBlock ioException = tryBlock._catch(codeModel.ref(IOException.class)); //rdfstore
//        buildCatchExceptionReturn(ioException, wsResponse, "INTERNAL_SERVER_ERROR");
//        JCatchBlock URIsyntaxException = tryBlock._catch(codeModel.ref(URISyntaxException.class)); //response
//        buildCatchExceptionReturn(URIsyntaxException, wsResponse, "INTERNAL_SERVER_ERROR");
//        JCatchBlock badRequestException = tryBlock._catch(codeModel.ref("be.cetic.ors.ontologybinding.generic.exception.BadRequestException")); //badRequest
//        buildCatchExceptionReturn(badRequestException, wsResponse, "INTERNAL_SERVER_ERROR");
//        JCatchBlock xmlAnalysisException = tryBlock._catch(codeModel.ref("be.cetic.ors.ontologybinding.generic.exception.XmlAnalysisException")); //rdfstore
//        buildCatchExceptionReturn(xmlAnalysisException, wsResponse, "INTERNAL_SERVER_ERROR");
//        JCatchBlock classURIException = tryBlock._catch(codeModel.ref("be.cetic.ors.ontologybinding.generic.exception.ClassURIException")); //rdfstore
//        buildCatchExceptionReturn(classURIException, wsResponse, "INTERNAL_SERVER_ERROR");
        JCatchBlock exception = tryBlock._catch(codeModel.ref(Exception.class));// not defined error -> HTTP 500
        buildCatchExceptionReturn(exception, wsResponse, "INTERNAL_SERVER_ERROR");
    }

}
