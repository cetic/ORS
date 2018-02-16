/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.ontologybinding.generic;

import javax.ws.rs.core.Response;
import java.io.IOException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.DatasetAccessor;


/**
 *
 * @author fs
 * // implementation -> FusekiManager
 */
public interface IDBManager {

    String namespace = "http://www.semanticweb.org/ontologies/2017/ors-core-ontology#";

    /**
     * Once you have finished to edit your model, you can store it back to DB
     */
    public void commit() ;

    /**
     * Check if an individual exist
     * @param id
     * @return 
     */
    public boolean existIndividualSubject(String id) ;

    public void deleteIndividual(String id) ;
    
    public Response deleteIndividualSparql(String idIndividual) throws  IOException ;

    public String generateOwl() ;

}
