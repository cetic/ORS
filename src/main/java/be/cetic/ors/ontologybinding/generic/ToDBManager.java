/*
 *
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.ontologybinding.generic;

import be.cetic.ors.ontologybinding.generic.exception.ResourceNotFoundException;
import be.cetic.ors.ontologybinding.generic.PropertiesManager;
import java.io.IOException;
import javax.ws.rs.core.Response;
//import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
//import org.apache.jena.tdb2.DatabaseMgr;
//import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.Reasoner;
import java.net.URI;

/**
 *
 * @author fs Contains all generic methods for creating java classes for
 * converting to RDF. This one implement rdf server. 
 * Uses the Jena Library
 */
public class ToDBManager implements IToRDFManager {

    private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("ToDBManager");
    private static DatasetAccessor dataAccessor;
    private static OntModel ontmodel;
    private static String uri;
    //private Model model;
    private String rdfConnection;
    private String reasonerType;
    private String namespace;
    private boolean replace;
    private Individual idv;


    public ToDBManager(String uri) throws IOException, ResourceNotFoundException {
        logger.info("Starting ToDBManager with graph"+uri);
        initiateDB();
        setOntModel(uri);
        replace=false;
    }

    public ToDBManager() throws IOException {
        logger.info("Starting ToDBManager");
        //if (ToDBManager.ontmodel==null) {
            initiateDB();
            ToDBManager.ontmodel= createOntModel(loadDefaultModel());
        //}
    }

    private void initiateDB() throws IOException {
        rdfConnection = PropertiesManager.getInstance().getValue("rdfDataEndpoint");
        logger.info("Initializing ToDBManager");
        reasonerType = PropertiesManager.getInstance().getValue("reasoner");
        namespace = PropertiesManager.getInstance().getValue("namespace");
        logger.info("Using Reasoner "+reasonerType);

        //For Fuseki
        //auth= new SimpleAuthenticator("admin", "pw".toCharArray()));
        dataAccessor = DatasetAccessorFactory.createHTTP(rdfConnection); //,auth);
        
        //For TDB2
        //DatasetGraph graphAccessor = DatabaseMgr.connectDatasetGraph(rdfConnection);
        //graphAccessor.begin();
        //dataAccessor=DatasetAccessorFactory.create (graphAccessor);
    }

    public void setReplaceMode(boolean replace){
        this.replace=replace;
    }
    public boolean getReplaceMode(){
        return this.replace;
    }

    public Model loadDefaultModel() {
            return dataAccessor.getModel();
    }

    public Model loadModel(String uri) throws ResourceNotFoundException {
        if (dataAccessor.containsModel(uri)) 
            return dataAccessor.getModel(uri);
        else { 
            logger.info("DBManager ERROR: Could not find named model with uri: "+uri);
            throw new ResourceNotFoundException("DBManager ERROR: Could not find named model with uri: "+uri);
        }
        //return model;
    }

    private OntModel createOntModel(Model model){

        // loading the model as an ontology
        OntModel newmodel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

        if (reasonerType.contains("RDFS")){
             Reasoner reasoner = ReasonerRegistry.getRDFSReasoner(); 
             InfModel inf = ModelFactory.createInfModel(reasoner, model); 
             newmodel.add(inf);
            logger.info("Loaded model "+ToDBManager.uri+" with Reasoner "+reasonerType);
        }
        else if (reasonerType.contains("Transitive")){
             Reasoner reasoner = ReasonerRegistry.getTransitiveReasoner(); 
             InfModel inf = ModelFactory.createInfModel(reasoner, model); 
             newmodel.add(inf);
            logger.info("Loaded model "+ToDBManager.uri+" with Reasoner "+reasonerType);
        }
        else newmodel.add(model);

        newmodel.createOntology(namespace);
        return newmodel;
    }

    @Override
    public Individual getIndividual() throws ResourceNotFoundException {
        if (idv==null)
        {
            logger.info ("TODBMANAGER The individual is not yet set throwing ResouceNotFound");
            throw new ResourceNotFoundException("The individual is not yet set");
        }
        else
            return idv;
    }

    @Override
    public void setIndividual(String id, OntClass ontClass) {
        logger.info ("TODBMANAGER set/create individual with ID "+id+" of class "+ontClass); 
        idv = ToDBManager.ontmodel.createIndividual(id, ontClass);
        idv.addRDFType(ontClass);
    }

    public void removeIndividual(String id) throws ResourceNotFoundException{
        if (this.idv==null) throw new ResourceNotFoundException("Individual is not found or not set.");
        this.idv.remove();
    }

    @Override
    public void commit() {
        if (!replace)
            dataAccessor.add(ToDBManager.ontmodel);
        else putModel();
    }

    //copy at a new (or existing) graphuri 
    public void commit(String graphURI) {
        dataAccessor.add(graphURI, ToDBManager.ontmodel);
    }

    // for creating new graphs.  
    public void commit(String graphURI, Model model) {
        if (graphURI!=null && graphURI.length()>1)dataAccessor.add(graphURI, model);
    }

    /* for replacing existing graphs -- not used, use setOntModel and putModel 
    public void putModel(String graphURI) throws ResourceNotFoundException {
        dataAccessor.putModel(graphURI, createOntModel(loadDefaultModel()));
    }*/

    public void putModel() {// throws ResourceNotFoundException {
        dataAccessor.putModel(ToDBManager.ontmodel);
    }
    
    @Override
    public boolean existIndividualSubject(String id) {
        return ToDBManager.ontmodel.getIndividual(id) != null;
    }

    @Override
    public void deleteIndividual(String id) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Response deleteIndividualSparql(String idIndividual) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String generateOwl() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    //This only returns the private variable ontmodel
    @Override
    public OntModel getOntModel() {
        return ToDBManager.ontmodel;
    }

    //this returns an existing or creates a new one 
    public OntModel getOntModel(String uri) {
        if (uri.length()>1) 
            try{
                ToDBManager.uri=uri;
                ToDBManager.ontmodel= createOntModel(loadModel(uri));
            }catch(ResourceNotFoundException e){
               createOntModel(uri);
            }
        return ToDBManager.ontmodel;
    }

    /*public Model getModel() {
        return model;
    }*/

    //this changes the current ontmodel to an existing one. If uri does not exist throws exception.
    public void setOntModel(String uri) throws ResourceNotFoundException {
        if(uri==null  || uri.length()<2 ) ToDBManager.ontmodel= createOntModel(loadDefaultModel());
        else 
        {
            ToDBManager.ontmodel= createOntModel(loadModel(uri));
            ToDBManager.uri=uri;
        }
    }

    /*null sets the default model.
    public void setModel(Model model) {
        this.model=model;
    }*/

    public void createOntModel(String uri)  {
        ToDBManager.uri=uri;
        ToDBManager.ontmodel= createOntModel(loadDefaultModel());
        commit(uri,ontmodel);
    }

    public String getModelUri(){
        return ToDBManager.uri;
    }

    /*public DatasetGraph getDatasetGraph() {
        return graphAccessor;
    }
    public void closeDatasetGraph() {
        graphAccessor.close();
    }*/

    //Helper methods
     public OntProperty getPropertyByName(OntClass ontclass, String propertyName){
         ExtendedIterator<OntProperty> xiter=ontclass.listDeclaredProperties(true);
         while (xiter.hasNext()){
             OntProperty prop=xiter.next();
             System.out.println("Comapring "+prop.getLocalName()+" with "+propertyName);
             if (prop.getLocalName().equals(propertyName)) return prop;//.getURI();
         }

         //If not found:
         String ns=ontclass.getNameSpace();
         //return ns+propertyName;
         return  this.getOntModel().createOntProperty(ns+propertyName);
     }

     public void resetLiteralProperty(OntProperty prop) throws ResourceNotFoundException {
             this.getIndividual().setPropertyValue(prop,null); 
                     //this.getOntModel().asRDFNode(NodeFactory.createLiteral("")));
                     //this.getOntModel().asRDFNode(NodeFactory.createBlankNode()));
     }

     public void addLiteralProperty(OntProperty prop, String value) throws ResourceNotFoundException {
             this.getIndividual().addProperty(prop, value);
     }
     public void setLiteralProperty(OntProperty prop, String value) throws ResourceNotFoundException {
         if (!this.getReplaceMode())
             this.getIndividual().addProperty(prop, value);
         else
             this.getIndividual().setPropertyValue(prop, 
                     this.getOntModel().asRDFNode(NodeFactory.createLiteral(value)));
     }

     public void setObjectProperty(OntProperty prop, String id) throws ResourceNotFoundException {
        logger.info ("TODBMANAGER setting object propperty "+prop+" with ID "+id+" on individual "+this.getIndividual()); 
         if (!this.getReplaceMode())
              this.getIndividual().addProperty(prop, 
                      this.getOntModel().getIndividual(id));
         else
             this.getIndividual().setPropertyValue(prop, 
                     this.getOntModel().asRDFNode(NodeFactory.createURI(id)));
     }

     public void resetObjectProperty(OntProperty prop) throws ResourceNotFoundException {
        logger.info ("TODBMANAGER resetting object propperty "+prop+" on individual "+this.getIndividual()); 
             this.getIndividual().setPropertyValue(prop, null);
     }

     public void addObjectProperty(OntProperty prop, String id) throws ResourceNotFoundException {
        logger.info ("TODBMANAGER setting object propperty "+prop+" with ID "+id+" on individual "+this.getIndividual()); 
              this.getIndividual().addProperty(prop, 
                      this.getOntModel().getIndividual(id));
     }




}
