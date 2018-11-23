/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.ontologybinding.generic.rs;


import be.cetic.ors.ontologybinding.generic.ViewManager;
import be.cetic.ors.ontologybinding.generic.PropertyView;
import be.cetic.ors.ontologybinding.generic.ClassView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
@Path("/view")
public class RestView {

    private static final Logger logger = LoggerFactory.getLogger(RestView.class);

    @Path("/classes")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response classes(@DefaultValue("") @QueryParam("uri") String uri) throws IOException {
        try{
            logger.info("Getting classes");

            ViewManager vm = new ViewManager();
            ArrayList<ClassView> values=vm.listClasses(uri);

            // Text response
            //String classes = Arrays.toString(values.toArray(new String[values.size()])).replaceAll("^.|.$", "");
            //return Response.ok(classes).build();
           
            //GenericEntity<ArrayList<String>>generic = new GenericEntity<ArrayList<String>>(values) { } ;
            return javax.ws.rs.core.Response.ok(values, "application/json").build();

        } catch (Exception ex) {
            logger.error("Error retrievning classes", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving classes " + ex).build();
        }

    }

    /**
     * fields must be specific to an entry point. e.g. resource
     * this fields must list all the subclasses of a given resource class.
     * @return 
     */
    @Path("/classnames")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response classNames( @DefaultValue("") @QueryParam("uri") String uri) throws IOException {
        try{
            logger.info("Getting classes");

            ViewManager vm = new ViewManager();
            ArrayList<String> values=vm.listClassNames(uri);

            // Text response
            //String classes = Arrays.toString(values.toArray(new String[values.size()])).replaceAll("^.|.$", "");
            //return Response.ok(classes).build();
           
            //GenericEntity<ArrayList<String>>generic = new GenericEntity<ArrayList<String>>(values) { } ;
            return javax.ws.rs.core.Response.ok(values, "application/json").build();

        } catch (Exception ex) {
            logger.error("Error retrievning classes", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving classes " + ex).build();
        }

    }

    @Path("{classname}/properties")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    //  @Override
    public Response properties(@DefaultValue("") @QueryParam("uri") String uri, @PathParam("classname") String classname, @QueryParam("namespace") String namespace,   @DefaultValue("no") @QueryParam("inherited") String inherited) 
        throws IOException {
        try {
            logger.info("Properties of "+classname+" with "+namespace+" inherited? "+inherited);
            ViewManager vm = new ViewManager();
            boolean direct=false;
            if (inherited.equals("no"))direct=true;
            ArrayList<PropertyView> values=vm.listProperties(uri,namespace,classname,direct);

            //GenericEntity<ArrayList<PropertyView>>generic = new GenericEntity<ArrayList<PropertyView>>(values) { } ;
            return javax.ws.rs.core.Response.ok(values, "application/json").build();

        } catch (Exception ex) {
            logger.error("Error retrievning properties", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving properties" + ex).build();
        }

    }
}
