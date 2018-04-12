/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.ontologybinding.generic;

import be.cetic.ors.ontologybinding.generic.exception.ResourceNotFoundException;
import be.cetic.ors.ontologybinding.generic.PropertiesManager;
import java.io.IOException;
import javax.ws.rs.core.Response;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.Reasoner;

/**
 *
 * @author fs Contains all generic methods for creating java classes for
 * converting to RDF. This one implement rdf server. 
 * Uses the Jena Library
 */
public class ToDBManager implements IToRDFManager {

    protected DatasetAccessor dataAccessor;
    protected OntModel ontmodel;
    
    private Individual idv;

    public ToDBManager() throws IOException {

        String rdfConnection = PropertiesManager.getInstance().getValue("rdfDataEndpoint");
        String reasonertype = PropertiesManager.getInstance().getValue("reasoner");
        dataAccessor = DatasetAccessorFactory.createHTTP(
                rdfConnection, new SimpleAuthenticator("admin", "pw".toCharArray()));

        // get model
        Model model = dataAccessor.getModel();

        // loading the model as an ontology
        ontmodel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

        if (reasonertype.contains("RDFS")){
             Reasoner reasoner = ReasonerRegistry.getRDFSReasoner(); 
             InfModel inf = ModelFactory.createInfModel(reasoner, model); 
             ontmodel.add(inf);
        }
        else if (reasonertype.contains("Transitive")){
             Reasoner reasoner = ReasonerRegistry.getTransitiveReasoner(); 
             InfModel inf = ModelFactory.createInfModel(reasoner, model); 
             ontmodel.add(inf);
        }
        else ontmodel.add(model);

        ontmodel.createOntology(namespace);

    }

    @Override
    public Individual getIndividual() throws ResourceNotFoundException {
        if (idv==null)
            throw new ResourceNotFoundException("The individual is not yet set");
        else
            return idv;
    }

    @Override
    public void setIndividual(String id, OntClass ontClass) {
        idv = ontmodel.createIndividual(id, ontClass);
        idv.addRDFType(ontClass);
    }

    @Override
    public void commit() {
        dataAccessor.add(ontmodel);
    }

    @Override
    public boolean existIndividualSubject(String id) {
        return ontmodel.getIndividual(id) != null;
    }

    @Override
    public void deleteIndividual(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Response deleteIndividualSparql(String idIndividual) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String generateOwl() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OntModel getOntModel() {
        return ontmodel;
    }



    //Helper method
     public OntProperty getPropertyByName(OntClass ontclass, String propertyName){
         ExtendedIterator<OntProperty> xiter=ontclass.listDeclaredProperties(true);
         while (xiter.hasNext()){
             OntProperty prop=xiter.next();
             System.out.println("Comapring "+prop.getLocalName()+" with "+propertyName);
             if (prop.getLocalName().equals(propertyName)) return prop;//.getURI();
         }

         //If not found:
         String ns=ontclass.getNameSpace();
         return  this.getOntModel().createOntProperty(ns+propertyName);
         //return ns+propertyName;
     }

}
