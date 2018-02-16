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
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JDirectClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import com.helger.jcodemodel.JVar;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;

/**
 *
 * @author fs
 * Uses JcodeModel library to generate java code.
 * And generates the classes that are going to build the model files for serialization.
 */
public class SerializerConvertor {

    private ApplicationConstant constants;
    private JCodeModel codeModel;
    private AnalyzerResult analyzerResult;

    private String targetPackage;
    private String targetDirectory;
    
    public static Logger logger = Logger.getLogger(SerializerConvertor.class.getName());

    public SerializerConvertor(ApplicationConstant constants, AnalyzerResult analyzerResult) {
        this.analyzerResult = analyzerResult;
        this.constants = constants;

        // class generator
        codeModel = new JCodeModel();

	//Specifies the package name of all java files.
        targetPackage = constants.getTARGET_PACKAGE();

	//This specifies the directory where the java files will be put
        targetDirectory = constants.getTARGET_DIRECTORY();
        

    }

    // Builds the classes with the toModel method. Not completed.
    public void buildSerializerClass() throws JClassAlreadyExistsException, IOException {

        
        // define package and classes
        JPackage jp = codeModel._package(targetPackage+".model");
        JDefinedClass jc = jp._class("Model"+analyzerResult.getAnalysedClassName() );
        jc.javadoc().add("Generated class.");
        AbstractJClass individualsClass = codeModel.ref(Individual.class);
        AbstractJClass resourcesClass = codeModel.ref(Resource.class);
        AbstractJClass ontModelClass = codeModel.ref(OntModel.class);
        AbstractJClass ioExceptionClass = codeModel.ref(IOException.class);
        AbstractJClass resourceClass = codeModel.ref(analyzerResult.getAnalysedClass());
        AbstractJClass ontClassClass = codeModel.ref(OntClass.class);
        AbstractJClass rdfNodeClass = codeModel.ref(org.apache.jena.rdf.model.RDFNode.class);
        
        // add instance variable
        JFieldVar individual = jc.field(JMod.PROTECTED, individualsClass, "individual");
        //JFieldVar ontModel = jc.field(JMod.PROTECTED, ontModelClass, "ontModel");
        //Class namespace contains the name of the class while class ontology does not. The later is used for the properties. 
        String className=analyzerResult.getAnalysedClassName();
        String classNamespace=constants.getONTOLOGY_NAMESPACE_CLASS(analyzerResult.getAnalysedClass().getName(), analyzerResult.getAnalysedClassName());
        String ontologyOfClass= classNamespace.replace(className,"");
        JFieldVar uriConstantField = jc.field(JMod.PUBLIC | JMod.FINAL | JMod.STATIC, String.class, "URI", JExpr.lit(classNamespace));


        // add default constructor
        JMethod constr = jc.constructor(JMod.PUBLIC);
        constr._throws(ioExceptionClass);
        JVar constrIdv = constr.param(individualsClass, "idv");
        //JVar constrOnt = constr.param(ontModelClass, "ont");
        constr.javadoc().add("Creates a new " + jc.name() + ".");
        
        constr.body().assign(individual, constrIdv);
        //constr.body().assign(ontModel, constrOnt);
        
        // add "toModel" method
        JMethod toModel = jc.method(JMod.PUBLIC, resourceClass, "toModel");
        toModel._throws(ioExceptionClass);
        JInvocation createXmlResource = JExpr._new(resourceClass);
        JVar resourceInstance = toModel.body().decl(resourceClass, "instance", createXmlResource);
        
        //new OntModel()
        JInvocation getOntModel = individual.invoke("getOntModel");
        JVar ont=toModel.body().decl(ontModelClass, "ont", getOntModel); 

        //toXML.body().add(resourceInstance.invoke("setId").arg(individual.invoke("getURI")));
        Iterator vars=analyzerResult.getVariablesIterator();
        while (vars.hasNext()){
            java.util.Map.Entry<String, List<String>> pair= (java.util.Map.Entry<String, List<String>>)vars.next();
            String var_type=pair.getKey();
            List<String> typevars=pair.getValue();
            for (int i = 0; i < typevars.size();i++){
                String varname=typevars.get(i);
                String methodname="set"+varname.substring(0, 1).toUpperCase() + varname.substring(1);
                //String objectname=varname.substring(0, 1).toUpperCase() + varname.substring(1);
                logger.info("Adding "+varname+" with type "+var_type);
                if (methodname.equals("setId")) toModel.body().add(resourceInstance.invoke("setId").arg(individual.invoke("getURI")));
                else {
                    JInvocation property=ont.invoke("createOntProperty").arg(ontologyOfClass+varname);
                    JInvocation getter=individual.invoke("getPropertyValue").arg(property);
                    JVar value=toModel.body().decl(rdfNodeClass,varname, getter);

                    //check if value is null here.
                    JConditional conditional= toModel.body()._if((value.ne(JExpr._null())));

                    if (var_type.contains("String") || var_type.contains("Literal"))
                    conditional._then().add(resourceInstance.invoke(methodname).arg(value.invoke("toString")));
                    else if (var_type.contains("int") || var_type.contains("INT")){
                        //JInvocation toInt= codeModel.ref(Integer.class).staticInvoke("parseInt").arg(value.invoke("toString"));
                        //conditional._then().add(resourceInstance.invoke(methodname).arg(toInt));
                        conditional._then().add(resourceInstance.invoke(methodname).arg(value.invoke("asLiteral").invoke("getInt")));
                    }
                    else if (var_type.contains("double") || var_type.contains("DOUBLE")){
                        conditional._then().add(resourceInstance.invoke(methodname).arg(value.invoke("asLiteral").invoke("getDouble")));

                    }
                    //instance.setObjectName((new ModelObjectName(value)).toModel());
                    //resourceInstance.invoke(methodname).arg( (TODO:new ObjectName(value)).invoke("toModel"));
                    else {
                        logger.info("ADDING Object property"+var_type);
                        //try{
                            JVar rs=conditional._then().decl(resourcesClass, "rs", value.invoke("asResource"));
                            JVar ind=conditional._then().decl(individualsClass, "ind", ont.invoke("getIndividual").arg(rs.invoke("getURI")));
                            JDirectClass  objectClass= codeModel.directClass(jp.name()+"."+"Model"+var_type);
                            JVar obj=conditional._then().decl(objectClass, "obj", JExpr._new(objectClass).arg(ind));
                            conditional._then().add(resourceInstance.invoke(methodname).arg(obj.invoke("toModel")));
                        //}catch(ClassNotFoundException e){logger.info(e.getMessage());}
                    }
                }
            }
        }
        toModel.body()._return(resourceInstance);
             
        codeModel.build(new File(targetDirectory));
        
    }

}
