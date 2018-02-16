/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.ontologybinding.generic.query;

/**
 * Error related to the query processing by rdf repository
 * 50X
 * @author fs
 */
public class SparqlManagerException extends Exception{

    public SparqlManagerException() {
    }

    public SparqlManagerException(String message) {
        super(message);
    }

    public SparqlManagerException(Throwable cause) {
        super(cause);
    }
    
    
}
