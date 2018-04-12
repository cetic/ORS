/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.ontologybinding.generic;

import be.cetic.ors.ontologybinding.generic.exception.ClassURIException;
import be.cetic.ors.ontologybinding.generic.exception.ResourceNotFoundException;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

/**
 *
 * @author fs
 */
public interface IToRDFManager extends IDBManager {

    public Individual getIndividual() throws ResourceNotFoundException;

    public void setIndividual(String id, OntClass ontClass);
    
    OntModel getOntModel();

    /**
     * Once you have finished to edit your model, you can store it back to
     * fuseki tdb
     */
    void commit();

}
