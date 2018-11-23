/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.ontologybinding.generic.rs;


import be.cetic.ors.ontologybinding.generic.document.DocumentManager;
import be.cetic.ors.ontologybinding.generic.ViewManager;
import be.cetic.ors.ontologybinding.generic.ClassView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nm
 */
@Path("/document")
public class RestDocMgt {

    private static final Logger logger = LoggerFactory.getLogger(RestDocMgt.class);

    @Path("/list")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws IOException {
        try{
            logger.info("List of documents");

            DocumentManager dm = new DocumentManager();
            ArrayList<String> values=dm.list();

            return javax.ws.rs.core.Response.ok(values, "application/json").build();

        } catch (Exception ex) {
            logger.error("Error retrievning document list", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving document list " + ex).build();
        }

    }

    @Path("/open")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response open(@QueryParam("uri") String uri) throws IOException {
        try{
            logger.info("Open document");

            DocumentManager dm = new DocumentManager();
            dm.open(uri);

            return javax.ws.rs.core.Response.ok().build();

        } catch (Exception ex) {
            logger.error("Error: Cannot load document ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving document list " + ex).build();
        }

    }

    /**
     * fields must be specific to an entry point. e.g. resource
     * this fields must list all the subclasses of a given resource class.
     * @return 
     */
    @Path("/import")
    @POST
    @Consumes("application/ld+json")
    public Response parse(@QueryParam("uri") String uri, Object doc) throws IOException {
        try{
            logger.info("Importing document");
            DocumentManager dm = new DocumentManager();
            if (doc==null){
                logger.info("Content is null so creating new document.");
                dm.create(uri);
            }
            else {
                //JSONObject obj=(JSONObject)doc.getContent();
                dm.parse(uri,doc);
            }
            return javax.ws.rs.core.Response.ok().build();
        } catch (Exception ex) {
            logger.error("Error: Cannot import ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving classes " + ex).build();
        }

    }

    /**
     * @return JSON-LD
     */
    @Path("/export")
    @GET
    @Produces("application/ld+json")
    public Response export(@QueryParam("uri") String uri, @QueryParam("element") String rootElement, @QueryParam("id") String id) throws IOException {
        try{
            logger.info("Exporting document "+uri+" with rootelement "+rootElement+" and id "+id);
            DocumentManager dm = new DocumentManager();
            Object value=dm.export(uri, rootElement, id);
            return javax.ws.rs.core.Response.ok(value, "application/ld+json").build();
        } catch (Exception ex) {
            logger.error("Error: Cannot export", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving classes " + ex).build();
        }

    }


    @Path("/create")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    //  @Override
    public Response create(@QueryParam("uri") String uri) throws IOException {
        try {
            DocumentManager dm = new DocumentManager();
            dm.create(uri);

            return javax.ws.rs.core.Response.ok().build();

        } catch (Exception ex) {
            logger.error("Error: Cannot create new document", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving properties" + ex).build();
        }
    }

    @Path("/delete")
    @DELETE
    public Response delete(@QueryParam("uri") String graphId) throws IOException {
        try {
            DocumentManager dm = new DocumentManager();
            dm.delete(graphId);

            return javax.ws.rs.core.Response.ok().build();

        } catch (Exception ex) {
            logger.error("Error retrievning properties", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving properties" + ex).build();
        }

    }
}
