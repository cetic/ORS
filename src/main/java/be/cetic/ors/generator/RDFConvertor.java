/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.generator;

import be.cetic.ors.ApplicationConstant;
import be.cetic.ors.analyzer.PrimitiveType;
import be.cetic.ors.analyzer.PrimitiveTypeException;
import be.cetic.ors.analyzer.AnalyzerResult;
import com.helger.jcodemodel.AbstractJClass;

import com.helger.jcodemodel.JBlock;
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
import com.helger.jcodemodel.JVar;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author fs
 * Uses JcodeModel library to generate java code.
 * And generates the classes that are going to build the RDF files.
 */
public class RDFConvertor {

    private ApplicationConstant constants;
    private JCodeModel codeModel;
    private AnalyzerResult analyzerResult;

    private String targetPackage;
    private String targetDirectory;
    
    public static Logger logger = Logger.getLogger(RDFConvertor.class.getName());

    public RDFConvertor(ApplicationConstant constants, AnalyzerResult analyzerResult) {
        this.analyzerResult = analyzerResult;
        this.constants = constants;

        // class generator
        codeModel = new JCodeModel();

	//Specifies the package name of all java files.
        targetPackage = constants.getTARGET_PACKAGE();

	//This specifies the directory where the java files will be put
        targetDirectory = constants.getTARGET_DIRECTORY();

    }

    // Builds the classes with the toRDF method. Not completed.
    public void buildRDFClass() throws JClassAlreadyExistsException, IOException {

        // add dependencies
	// Extends the ToRDFMAnager that contains all generic methods
        AbstractJClass dbManager = codeModel.ref("be.cetic.ors.ontologybinding.generic.ToDBManager");

        // define package and classes
        JPackage jp = codeModel._package(targetPackage+".rdf");
        JDefinedClass jc = jp._class(analyzerResult.getAnalysedClassName() + "RDF");
        jc._extends(dbManager);
        jc.javadoc().add("Generated class.");

        // add default constructor
        AbstractJClass IOExceptionClass = codeModel.ref("java.io.IOException");
        JMethod constr = jc.constructor(JMod.PUBLIC);
        constr.javadoc().add("Creates a new " + jc.name() + ".");
        constr._throws(IOExceptionClass);
        
        // add instance variable
        JFieldVar uriConstantField = jc.field(JMod.PUBLIC | JMod.FINAL, String.class, "URI",
                JExpr.lit(constants.getONTOLOGY_NAMESPACE_CLASS(analyzerResult.getAnalysedClass().getName(), analyzerResult.getAnalysedClassName())));

        // add "toRDF" method
        JMethod toRDF = jc.method(JMod.PUBLIC, String.class, "toRDF");
        AbstractJClass xmlAnalysisExceptionClass = codeModel.ref("be.cetic.ors.ontologybinding.generic.exception.SerializationAnalysisException");
        AbstractJClass uriExceptionClass = codeModel.ref("be.cetic.ors.ontologybinding.generic.exception.ClassURIException");
        AbstractJClass resourceExceptionClass = codeModel.ref("be.cetic.ors.ontologybinding.generic.exception.ResourceNotFoundException");
        toRDF._throws(uriExceptionClass);
        toRDF._throws(xmlAnalysisExceptionClass);
        toRDF._throws(resourceExceptionClass);
        logger.log(Level.INFO, "Analyzing:" +analyzerResult.getAnalysedClass().getSimpleName().toLowerCase() );
        JVar xmlClassParameter = toRDF.param(analyzerResult.getAnalysedClass(), analyzerResult.getAnalysedClass().getSimpleName().toLowerCase());

        AbstractJClass ontClass = codeModel.ref("org.apache.jena.ontology.OntClass");
        JBlock jBlock = toRDF.body();

        //JInvocation initializeOntclass = JExpr._this().invoke("getRDFClass");
        //initializeOntclass.arg(uriConstantField);
        JVar ontclass = jBlock.decl(ontClass, "ontClass", JExpr.direct("ontmodel.getOntClass(URI)"));
        

        //jBlock.directStatement("ontClass = getRDFClass(URI);");
        JInvocation initializeGetID = xmlClassParameter.invoke("getId");
        JInvocation conditionExistIndvSubj = JExpr._this().invoke("existIndividualSubject");
        conditionExistIndvSubj.arg(initializeGetID);

        JConditional condition = jBlock._if(conditionExistIndvSubj.eq(JExpr.FALSE));

        JInvocation setIndividual = JExpr._this().invoke("setIndividual");
        setIndividual.arg(initializeGetID);
        setIndividual.arg(ontclass);
        condition._then().add(setIndividual);

        /*JInvocation setRDFType = JExpr._this().invoke("getIndividual").invoke("addRDFType");
        setRDFType.arg(ontclass);
        condition._then().add(setRDFType);*/
        try {
            condition._then().add(computefield(codeModel, ontclass,  xmlClassParameter));
        } catch (PrimitiveTypeException ex) {
            logger.log(Level.SEVERE, ex.getMessage(),ex);
        }
        condition._then()._return(xmlClassParameter.invoke("getId"));

        // Else Block -> id already exists -> generate exeption
        JBlock jBlockElse = condition._else();
        JInvocation exception = JExpr._new(xmlAnalysisExceptionClass);
        exception.arg("This id already exist: ");
        jBlockElse._throw(exception);

        codeModel.build(new File(targetDirectory));

    }

    /**
     * Get the variable of the XMLClass and generate the conversion tools
     *
     * @param codeModel
     * @param xmlClassParameter
     * @return
     * @throws PrimitiveTypeException
     */
    private JBlock computefield(JCodeModel codeModel, JVar ontclass, JVar xmlClassParameter) throws PrimitiveTypeException {

        JBlock jBlock = new JBlock();
        jBlock.directStatement("//Variable conversion");
        for (Map.Entry<String, List<String>> entry : analyzerResult.getVariables().entrySet()) {
            String type = entry.getKey();
            if (PrimitiveType.getAllPrimitiveType().contains(type)) {
                logger.log(Level.INFO, "Primitive type detected: {0}", type);
                PrimitiveType ptype = PrimitiveType.getPrimitiveTypeByName(type);

                for (String variableName : entry.getValue()) {
                    // e.g.: the variable foo must be translated to getFoo() 
                    // e.g.: the variable currentLocation.geographicPointAltitude must be translated into getCurrentLocation().getGeographicPointAltitude()
                    int cpt =1;
                    JInvocation getVariable = null;
                    for (String decomposedVariable: variableName.split("\\.")){
                        String methodName = "get" + decomposedVariable.substring(0, 1).toUpperCase() + decomposedVariable.substring(1);
                        if (cpt ==1){
                            getVariable = xmlClassParameter.invoke(methodName);
                            cpt++;
                        }else{
                            getVariable = getVariable.invoke(methodName);
                        }
                    }
                    if (!variableName.equals("id")) createDataProperty(codeModel, ontclass ,jBlock, getVariable, variableName.replace(".", "_"));
                    
                }

            }else{
                logger.log(Level.INFO, "Object type detected: {0}", type);
                for (String variableName : entry.getValue()) {
                    // e.g.: the variable foo must be translated to getFoo() 
                    // e.g.: the variable currentLocation.geographicPointAltitude must be translated into getCurrentLocation().getGeographicPointAltitude()
                    int cpt =1;
                    JInvocation getVariable = null;
                    for (String decomposedVariable: variableName.split("\\.")){
                        String methodName = "get" + decomposedVariable.substring(0, 1).toUpperCase() + decomposedVariable.substring(1);
                        if (cpt ==1){
                            getVariable = xmlClassParameter.invoke(methodName);
                            cpt++;
                        }else{
                            getVariable = getVariable.invoke(methodName);
                        }
                    }
                    createObjectProperty(codeModel, ontclass ,jBlock, getVariable, variableName.replace(".", "_"));
                }
            }
        }
        return jBlock;
    }

    /**
     * Assign a variable to a data property
     *
     * @param codeModel
     * @param jBlock the container
     * @param getVariable the invocation to get the variable
     * @param getVariableName the name of the variable
     */
    private void createDataProperty(JCodeModel codeModel, JVar ontclass,  JBlock jBlock, JInvocation getVariable, String getVariableName) {
        // data property 
        JDirectClass ontPropertyClass = codeModel.directClass("org.apache.jena.ontology.OntProperty");
        JInvocation ontPropertyInv = JExpr._this().invoke("getPropertyByName").arg(ontclass).arg(getVariableName);
        JVar dataproperty = jBlock.decl(ontPropertyClass, "property4" + getVariableName, ontPropertyInv);

        //idvGeoPoint.addLiteral(propertyLongitud, resource.getCurrentLocation().getGeographicPointLongitudeCooordinate());
        JInvocation invAddLitt = JExpr._this().invoke("getIndividual").invoke("addProperty");
        invAddLitt.arg(dataproperty);
        JInvocation datapropertyvalue= codeModel.ref(java.util.Objects.class).staticInvoke("toString").arg(getVariable).arg("");
        invAddLitt.arg(datapropertyvalue); // Objects.toString(teclocation.getStreetname_nl(),"")

        jBlock.add(invAddLitt);
    }

    /**
     * Assign a variable to an object  property
     *
     * @param codeModel
     * @param jBlock the container
     * @param getVariable the invocation to get the variable
     * @param getVariableName the name of the variable
     */
    private void createObjectProperty(JCodeModel codeModel, JVar ontclass,  JBlock jBlock, JInvocation getVariable, String getVariableName) {
        // object property 
        JDirectClass ontPropertyClass = codeModel.directClass("org.apache.jena.ontology.OntProperty");
        JInvocation ontPropertyInv = JExpr._this().invoke("getPropertyByName").arg(ontclass).arg(getVariableName);
        JVar dataproperty = jBlock.decl(ontPropertyClass, "property4" + getVariableName, ontPropertyInv);

        JConditional jCond = jBlock._if(getVariable.ne(JExpr._null()));

        //idvGeoPoint.addLiteral(propertyLongitud, resource.getCurrentLocation().getGeographicPointLongitudeCooordinate());
        JInvocation invAddLitt = JExpr._this().invoke("getIndividual").invoke("addProperty");
        invAddLitt.arg(dataproperty);
        JInvocation datapropertyvalue=JExpr._this().invoke("getOntModel").invoke("getIndividual").arg(getVariable.invoke("getId"));
        invAddLitt.arg(datapropertyvalue); 

        jCond._then().add(invAddLitt);
    }

}
