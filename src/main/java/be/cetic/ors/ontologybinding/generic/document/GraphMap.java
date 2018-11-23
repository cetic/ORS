package be.cetic.ors.ontologybinding.generic.document;
 
import org.apache.jena.rdf.model.Statement;

import java.util.List;

public class GraphMap{
       public List<Statement> statements=null;
       public String graphName=null;
           
       public GraphMap(String name, List<Statement> sts){
           this.statements=sts;
           this.graphName=name;
       }
   
   }

