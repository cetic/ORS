/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.ontologybinding.generic.rs;


import be.cetic.ors.ontologybinding.generic.exception.SparqlManagerException;
import be.cetic.ors.ontologybinding.generic.query.SparqlManager;
import be.cetic.ors.ontologybinding.generic.query.fuseki_model.FusekiSparqlModel;
import be.cetic.ors.ontologybinding.generic.query.SparqlModel;
import be.cetic.ors.ontologybinding.generic.query.QueryManager;
import be.cetic.ors.ontologybinding.generic.query.schema.*;
import be.cetic.ors.ontologybinding.generic.ViewManager;
import be.cetic.ors.ontologybinding.generic.PropertyView;
import be.cetic.ors.ontologybinding.generic.ClassView;
import be.cetic.ors.ontologybinding.generic.exception.QueryManagerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response fields(@DefaultValue("") @QueryParam("uri") String graphuri) throws IOException {
        try{
            logger.info("Getting fields");

            SparqlManager sparqlManager = new SparqlManager();
            SparqlModel sm = sparqlManager.selectDistinctFields(graphuri);
            ArrayList values=sm.getAllValues();

            //String fields = Arrays.toString(values.toArray(new String[values.size()])).replaceAll("^.|.$", "");

            //return Response.ok(fields.toLowerCase()).build();
            return Response.ok(values).build();

        } catch (SparqlManagerException ex) {
            logger.error("Error exceuting sparql query", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error exceuting sparql query " + ex).build();
        }

    }

    //@Produces(MediaType.TEXT_PLAIN)
    //  @Override
    @Path("/predicates")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response predicates( @DefaultValue("") @QueryParam("uri") String graphuri, @QueryParam("field") String classURI) throws IOException {
        try {
            logger.info("Predicate");
            if (classURI!=null){
                ArrayList<String> values=new ArrayList<String>();
                String[] parts = classURI.split("#");
                String namespace = parts[0]+"#"; 
                String classname = parts[1];
                logger.info("Properties of "+classname+" with "+namespace);
                ViewManager vm = new ViewManager();
                ArrayList<PropertyView> properties=vm.listProperties(graphuri, namespace,classname,false);
                for (PropertyView prop : properties) values.add(prop.getUri());
                return javax.ws.rs.core.Response.ok(values, "application/json").build();
            }
            else {
                SparqlManager sparqlManager = new SparqlManager();
                SparqlModel fs = sparqlManager.selectDistinctProperties(graphuri, classURI);

                ArrayList values=fs.getAllValues();

                //String predicates = Arrays.toString(values.toArray(new String[values.size()])).replaceAll("^.|.$", "");

                return Response.ok(values).build();
            }
        }
        catch (SparqlManagerException ex) {
            logger.error("Error exceuting sparql query", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error exceuting sparql query " + ex).build();
        }
        catch (Exception vex){
            logger.error("Error retrievning properties", vex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving properties" + vex).build();
        }

    }

    //@Produces(MediaType.TEXT_PLAIN)
    //  @Override
    @Path("/operators")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response operators() throws IOException {
        List<String> values=QueryManager.validOperator;
        logger.info("Operator List "+values);
        return Response.ok(values).build();
    }

    // Must be detailled for each root hierarchy e.g. : 
    // a put on query/resource will return resource object while 
    // a put on query/foo will return a foo object
    //@Consumes(MediaType.APPLICATION_XML)
    @Path("/")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //@Override
    public Response query( @DefaultValue("") @QueryParam("uri") String graphuri, Query query) {
        try {
            logger.info("Getting query with fields:\n" + Arrays.toString((query.getField()).toArray())+"\n");
            logger.info("Getting query with filters:\n" + Arrays.toString((query.getFilter()).toArray())+"\n");
            QueryManager qm = new QueryManager(query);
            String gq = qm.generateSparqlQuery(graphuri);
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
