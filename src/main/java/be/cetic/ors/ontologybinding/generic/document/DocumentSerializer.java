package be.cetic.ors.ontologybinding.generic.document;

import java.io.IOException;
import org.apache.jena.riot.RDFDataMgr;
import java.io.StringWriter;
import org.apache.jena.rdf.model.Model;



public class DocumentSerializer {

    public static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("DocumentSerializer");



     public String serialize(String id, String title, Model model) {
         logger.info("Serializing document");
         StringWriter w=new StringWriter();
         RDFDataMgr.write(w,model,org.apache.jena.riot.Lang.JSONLD);
         return w.toString();
     }

}
