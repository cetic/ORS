/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.ontologybinding.generic.query;

import be.cetic.ors.ontologybinding.generic.exception.SparqlManagerException;
import be.cetic.ors.ontologybinding.generic.PropertiesManager;
import be.cetic.ors.ontologybinding.generic.query.rdf4j_model.RDF4JSparqlModel;
import be.cetic.ors.ontologybinding.generic.query.fuseki_model.FusekiSparqlModel;
import be.cetic.ors.ontologybinding.generic.query.SparqlModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Feature;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.apache.log4j.Level;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fs
 */
public class SparqlManager  {

    private static final Logger logger = LoggerFactory.getLogger(SparqlManager.class);

    private Response response; 

    public SparqlManager() throws IOException {
        super();
    }

    public Response delete(String idIndividual) throws IOException {

        String sparqlDelete = "sparql-delete.velocity";

        logger.info("Loading velocity file " + Thread.currentThread().getContextClassLoader().getResource(sparqlDelete).getPath());

        // creating delete query
        Velocity.init();
        VelocityContext selectContext = new VelocityContext();

        StringWriter writerDelete = new StringWriter();
        selectContext.put("uri", idIndividual);

        InputStream inputStream
                = getClass().getClassLoader().getResourceAsStream(sparqlDelete);
        Reader inputStreamReader = new InputStreamReader(inputStream);

        Velocity.evaluate(selectContext, writerDelete, "selectTemplate", inputStreamReader);

        String deleteQuery = writerDelete.toString();
        logger.info("Generated sparql delete query: " + deleteQuery);

        // sending query to repository
        Client client = ClientBuilder.newClient();
        String conn = PropertiesManager.getInstance().getValue("rdfDataEndpointUpdate");

        logger.info("Repository update endpoint is " + conn);
        WebTarget ontologyUpdate = client.target(conn);

        Entity<String> requestBody = Entity.entity(deleteQuery, "application/sparql-update");

        Invocation invocation = ontologyUpdate.request().buildPost(requestBody);

        //Invoking the request to the RESTful API and capturing the Response.
        Response response = invocation.invoke();

        logger.info("Repository response: " + response.getStatus());

        return response;

    }

    /**
     * Submit Sparql query to RDF Repository
     * @param sparqlQuery
     * @return
     * @throws IOException
     * @throws SparqlManagerException
     */
    public String sparqlInvocation(String sparqlQuery)throws IOException, SparqlManagerException {
        logger.info("Query generated {}" + sparqlQuery);


        // sending query to the repository
        Client client = ClientBuilder.newClient();
        String conn = PropertiesManager.getInstance().getValue("rdfDataEndpointQuery");

        logger.info("Repository endpoint is " + conn);
        WebTarget target = client.target(conn);
        target.register(new LoggingFilter());

        Entity<String> requestBody = Entity.entity(sparqlQuery, "application/sparql-query");

        Invocation invocation = target.request().header("Accept", "application/sparql-results+json, */*;q=0.5").buildPost(requestBody);
        //invocation.property("Accept", "application/sparql-results+xml, */*;q=0.5");



        //Invoking the request to the RESTful API and capturing the Response.
        this.response = invocation.invoke();


        return this.response.readEntity(String.class);
    }


    /**
     * Submit Sparql query to RDF Repository and map results to SparqlModel
     * Currently using the Fuseki model for all repositories.
     * TODO: Change if needed to a model for each repository type.
     *
     * @param sparqlQuery
     * @return
     * @throws IOException
     * @throws SparqlManagerException
     */
    public SparqlModel sparqlQuery(String sparqlQuery) throws IOException, SparqlManagerException {
        String repository= PropertiesManager.getInstance().getValue("repositoryModel");
        /*Class repositoryClass = Class.forName(classname);
        ClassLoader classLoader = SparqlManager.class.getClassLoader();

         try {
             repositoryClass = classLoader.loadClass(classname);
         } catch (ClassNotFoundException e) {
             logger.info("Repository model "+classname+" not found\n"+e.getMessage()); 
         }*/
        String responseAsString = sparqlInvocation(sparqlQuery);
        if (this.response.getStatus() == 200) {
            logger.info("Repository response: " + response.getStatus());
            logger.info ("Respones string is  "+responseAsString);
            ObjectMapper mapper = new ObjectMapper();
            //TODO:Change the following conditions to be generated/automatically loaded according to the repository used.
            if (repository.contains("fuseki"))
               return mapper.readValue(responseAsString, FusekiSparqlModel.class);
            if (repository.contains("rdf4j"))
               return mapper.readValue(responseAsString, RDF4JSparqlModel.class);


        } else {
            logger.info("Sparql query returns unexpected status code " + response.getStatus());
            throw new SparqlManagerException(
                    "Sparql query returns unexpected status code " + response.getStatus()
                    + " info:" + response.getStatusInfo().getReasonPhrase());

        }
        return null;

    }

    /**
     * Currently using the Fuseki or RDF4J models depending on configuration properties.
     * TODO: Change if needed to a model for each repository type.
     */
    public SparqlModel selectDistinctProperties(String graphuri, String classURI) throws IOException, SparqlManagerException {

        String graph="";
        if (graphuri.length()>1) graph="FROM <"+graphuri+">\n";
        // creating sparql query
        String query = "SELECT DISTINCT ?predicate \n"
                     + graph
                     + "WHERE { \n";
        if (classURI!=null)
        query +=       "    ?subject a <"+classURI+"> .\n";
        query +=       "    ?subject ?predicate ?object .\n"
                     + "}";

        return sparqlQuery(query);
    }

    /**
     * Currently using the Fuseki or RDF4J models depending on configuration properties.
     * TODO: Change if needed to a model for each repository type.
     */
    public SparqlModel selectDistinctFields(String graphuri) throws IOException, SparqlManagerException {

        String graph="";
        if (graphuri.length()>1) graph="FROM <"+graphuri+">\n";
        // creating sparql query
        String query = "SELECT DISTINCT ?subject \n"
                + graph
                + "WHERE { \n"
                + " { ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class>}\n"
                + " UNION "
                + " {  ?subject  <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?type}\n"
                + "}";

        return sparqlQuery(query);
    }


    /** 
     * Used by the Document manager to retrieve a list of available named graphs aka documents in the repository.
     *
     */
    public SparqlModel selectDistinctGraphs() throws IOException, SparqlManagerException {
        String query= "SELECT DISTINCT ?graph WHERE { GRAPH ?graph { ?s ?p ?o } }";
            return sparqlQuery(query);
    }

    /**
     * Currently using the sparqlQuery method.
     * TODO: Translate query xml to SPARQL.
    public SparqlModel executeSparqlQuery(String query) throws IOException, SparqlManagerException {

        // creating sparql query
        String query = "SELECT DISTINCT ?subject \n"
                + "WHERE { \n"
                + "    ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class>.\n"
                + "}";

        return sparqlQuery(query);
    }
     */
}


