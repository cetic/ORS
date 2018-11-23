package be.cetic.ors.ontologybinding.generic.document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.net.URI;
import be.cetic.ors.ontologybinding.generic.ToDBManager;
import be.cetic.ors.ontologybinding.generic.exception.ResourceNotFoundException;
//import org.apache.jena.ontology.OntClass;
//import org.apache.jena.ontology.OntProperty;
//import org.apache.jena.util.iterator.ExtendedIterator;
import java.io.InputStream;
//import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.jena.ontology.OntDocumentManager;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.apache.jena.riot.RDFDataMgr;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;;
import com.fasterxml.jackson.core.JsonParseException;;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;
//import be.cetic.ors.ontologybinding.generated.pojo.*;

//import org.apache.jena.graph.Graph;
//import org.apache.jena.graph.Node;
import be.cetic.ors.ontologybinding.generic.query.SparqlManager;
import be.cetic.ors.ontologybinding.generic.exception.SparqlManagerException;
import be.cetic.ors.ontologybinding.generic.exception.DocumentManagerException;
import java.util.List;

import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;


/** Managment interface for graphs of individuals, created or submitted by the user.
 *
 * Management interfce includes the following interfaces: create, parse, delete, list  
 *
 */
public class DocumentManager extends ToDBManager {

    public static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("DocumentManager");


    public DocumentManager() throws IOException
    {
    }

    public ArrayList<String> list() throws IOException
    {
        //OntDocumentManager ontdocmgt=ontmodel.getDocumentManager();
        //ArrayList<String> documents = new ArrayList<String>();
        SparqlManager sm= new SparqlManager();
        try {
        //Iterator <String> graphs=sm.selectDistinctGraphs().getAllValues().iterator();
        return sm.selectDistinctGraphs().getAllValues();
    
         //Iterator<Graph> graphs = ontmodel.getSubGraphs().iterator();
         //Iterator<Node> graphs = this.getDatasetGraph().listGraphNodes();
        //if (!graphs.hasNext()) 
          //  logger.info("No documents found, not even default!!!");
        //while (graphs.hasNext()){
            //Graph g=graphs.next();
           // Node g=graphs.next();
          //  String uri= /*g.toString();//g.getURI();*/graphs.next();
          //  logger.info("Listing document "+uri);
          //  documents.add(uri);
       // }
        }catch (SparqlManagerException e){
            logger.info(e.getMessage());
        }
        return new ArrayList<String>();
    }


     public void open(String uri) throws IOException, ResourceNotFoundException {

            this.setOntModel(uri);
            //TODO
     }

     public Object export(String graphuri, String rootElement, String id) throws IOException, ResourceNotFoundException {
         ORSParser parser=new ORSParser();
         parser.setContext(graphuri);
         parser.setClsUri(rootElement);
         if (id!=null) parser.setClsId(id);
         if (graphuri!=null)
         return (JsonLdProcessor.fromRDF(this.getOntModel(graphuri), parser));
         else
         return (JsonLdProcessor.fromRDF(this.getOntModel(), parser));
     }

     public void parseJSONLD(String uri, Object jsonld) throws JsonParseException, IOException {

         //Object jsonld=JsonUtils.fromString(jsonldstring);

         List<GraphMap> graphmaps= (List<GraphMap>) JsonLdProcessor.toRDF(jsonld, new ORSTripleCallback());
         for (GraphMap gm : graphmaps){
              if (uri!=null)
                     this.getOntModel(uri).add(gm.statements);
              //else if ( gm.graphName==null)
                 //    this.getOntModel(gm.graphName).add(gm.statements);
                 else 
                     this.getOntModel().add(gm.statements);
             //this.loadModel(uri).add(gm.statements);
         }
     }




     public void parse(String uri, Object doc) throws DocumentManagerException, ResourceNotFoundException {
         logger.info("Importing document");

         if (doc!=null){
                 //RDFDataMgr.read(this.getOntModel(), new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), org.apache.jena.riot.Lang.JSONLD);
                 try{
                     parseJSONLD(uri,doc );  
                    /* ObjectMapper mapper = new ObjectMapper();
                     mapper.registerModule(new JsonldModule());
                     //Bundle bundle =mapper.reader().readValue(content);
                     doc.setContent(bundle);*/
                 }
                 catch (JsonParseException e){
                     logger.info(e.getMessage());
                     throw new DocumentManagerException(e.getMessage());
                 }

                 /*catch (JsonProcessingException e) {
                     logger.info(e.getMessage());
                     //throw new DocumentManagerException(e.getMessage());
                 }*/
                 catch (IOException e) {
                     logger.info(e.getMessage());
                     throw new DocumentManagerException(e.getMessage());
                 }
         }
         if (uri!=null && uri.length()>1) this.commit(uri); //, this.getModel());
         else this.commit();//null, this.getModel());
     }


     public void create(String uri) throws DocumentManagerException {
         if (uri==null) {
             throw new DocumentManagerException("URI is not provided");
         }
         logger.info("Creating document "+uri);
         this.createOntModel(uri);
     }

     public void delete(String uri){
         //TODO: 
     }

     /*private static String streamToString(InputStream is) {

         BufferedReader br = null;
         StringBuilder sb = new StringBuilder();

         String line;
         try {

             br = new BufferedReader(new InputStreamReader(is));
             while ((line = br.readLine()) != null) {
                 sb.append(line);
             }

         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             if (br != null) {
                 try {
                     br.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }

         return sb.toString();

     }*/


}
