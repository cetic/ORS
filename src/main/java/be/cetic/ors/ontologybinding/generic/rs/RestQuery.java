/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.ontologybinding.generic.rs;


import be.cetic.ors.ontologybinding.generic.query.SparqlManagerException;
import be.cetic.ors.ontologybinding.generic.query.SparqlManager;
import be.cetic.ors.ontologybinding.generic.query.fuseki_model.FusekiSparqlModel;
import be.cetic.ors.ontologybinding.generic.query.SparqlModel;
import be.cetic.ors.ontologybinding.generic.query.QueryManager;
import be.cetic.ors.ontologybinding.generic.query.QueryManagerException;
import generated.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fs
 */
@Path("/query")
public class RestQuery {

    private static final Logger logger = LoggerFactory.getLogger(RestQuery.class);

    /**
     * fields must be specific to an entry point. e.g. resource
     * this fields must list all the subclasses of a given resource class.
     * @return 
     */
    @Path("/fields")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response fields() throws IOException {
        try{
            logger.info("Getting fields");

            SparqlManager sparqlManager = new SparqlManager();
            SparqlModel sm = sparqlManager.selectDistinctFields();
            ArrayList values=sm.getAllValues();

            String fields = Arrays.toString(values.toArray(new String[values.size()])).replaceAll("^.|.$", "");

            //return Response.ok(fields.toLowerCase()).build();
            return Response.ok(fields).build();

        } catch (SparqlManagerException ex) {
            logger.error("Error exceuting sparql query", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error exceuting sparql query " + ex).build();
        }

    }

    @Path("/predicates")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    //  @Override
    public Response predicates() throws IOException {
        try {
            logger.info("Predicate");
            SparqlManager sparqlManager = new SparqlManager();
            SparqlModel fs = sparqlManager.selectDistinctProperties();

            ArrayList values=fs.getAllValues();

            String predicates = Arrays.toString(values.toArray(new String[values.size()])).replaceAll("^.|.$", "");

            return Response.ok(predicates).build();
        } catch (SparqlManagerException ex) {
            logger.error("Error exceuting sparql query", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error exceuting sparql query " + ex).build();
        }

    }

    // Must be detailled for each root hierarchy e.g. : 
    // a put on query/resource will return resource object while 
    // a put on query/foo will return a foo object
    @Path("/")
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    //@Override
    public Response query(Query query) {
        try {
            logger.info("Getting query with fields:\n" + Arrays.toString((query.getField()).toArray())+"\n");
            logger.info("Getting query with filters:\n" + Arrays.toString((query.getFilter()).toArray())+"\n");
            QueryManager qm = new QueryManager(query);
            String gq = qm.generateSparqlQuery();
            logger.info(" translated to: \n"+gq);
            SparqlManager sparqlManager = new SparqlManager();
            //SparqlModel fs = sparqlManager.sparqlQuery(gq);
            //ArrayList values = fs.getAllValues(); 
            //String results = Arrays.toString(values.toArray(new String[values.size()])).replaceAll("^.|.$", "");
            String results = sparqlManager.sparqlInvocation(gq);

            return Response.ok(results).build();
        } catch (QueryManagerException ex) {
            logger.error("Error XML query", ex);
            return Response.status(Response.Status.BAD_REQUEST).entity("Error XML query " + ex).build();
       } catch (IOException ex) {
            logger.error("Bad configuration. Check properties file", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Bad configuration. Check properties file " + ex).build();
        } catch (SparqlManagerException ex) {
            logger.error("Sparql endpoint returns unexpected code", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Sparql endpoint returns unexpected code " + ex).build();
       
        }

    }

}
