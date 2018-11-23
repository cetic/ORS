package be.cetic.ors.ontologybinding.generic.document;

import com.github.jsonldjava.core.JsonLdTripleCallback;
import com.github.jsonldjava.core.RDFDataset;
import static com.github.jsonldjava.core.JsonLdConsts.*;

import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Property;

import java.util.List;
import java.util.ArrayList;


public class ORSTripleCallback implements JsonLdTripleCallback {

    public Object call(RDFDataset dataset){
        final ArrayList<GraphMap> graphs= new  ArrayList<GraphMap>(); 
        for (String graphName : dataset.graphNames()) {
            final List<Statement> statements= new  ArrayList<Statement>(); 
            final List<RDFDataset.Quad> triples = dataset.getQuads(graphName);
            if ("@default".equals(graphName)) {
                graphName = null;
            }
            for (final RDFDataset.Quad triple : triples) {
                statements.add(toJenaStmt(triple,graphName, null));
            }
            GraphMap gm=new GraphMap(graphName, statements);
            graphs.add(gm);
            
        }
        return graphs;
    }

    private Statement toJenaStmt(RDFDataset.Quad triple, String graphName, String bnode){
        final RDFDataset.Node s = triple.getSubject();
        final RDFDataset.Node p = triple.getPredicate();
        final RDFDataset.Node o = triple.getObject();

        Resource subject = ResourceFactory.createResource();
        if (s.isIRI()) {
            //do a jena Node with s.getValue()
            subject=ResourceFactory.createResource(s.getValue());
        }
        // normalization mode
        else if (bnode != null) {
            // do a jena blank node  
            // testing anonymous resource for now.
            // bnode.equals(s.getValue()) ? "_:a" : "_:z"
        }
        // normal mode
        else {
            // testing anonymous resource for now.
            //subject=ResourceFactory.createResource(s.getValue());
            // s.getValue();
        }
        if (subject==null) System.out.println("GOT NULL subject");

        Property predicate =null;
        if (p.isIRI()) {
            predicate=ResourceFactory.createProperty(p.getValue());
            //p.getValue()
        }
        // otherwise it must be a bnode 
        else {
            predicate=ResourceFactory.createProperty(bnode, p.getValue());
        }
        if (predicate==null) System.out.println("GOT NULL Predicate");

        RDFNode object = null;
        // object is IRI, bnode or literal
        if (o.isIRI()) {
            //o.getValue()
            object=ResourceFactory.createResource(o.getValue());
        } else if (o.isBlankNode()) {
            // normalization mode
            if (bnode != null) {
                object=ResourceFactory.createResource(bnode.toString());
                //output.append(bnode.equals(o.getValue()) ? "_:a" : "_:z");
            }
            // normal mode
            else {
                object=ResourceFactory.createResource();
                //o.getValue();
            }
        } else {
            //Literal
            //o.getValue()
            if (RDF_LANGSTRING.equals(o.getDatatype())) {
                object=ResourceFactory.createLangLiteral(o.getValue(), o.getLanguage());
                //  output.append("@").append(o.getLanguage());
            } else if (!XSD_STRING.equals(o.getDatatype())) {
                object=ResourceFactory.createTypedLiteral(o.getValue());// ,RDFo.getDatatype().toString())

            }
            else object=ResourceFactory.createStringLiteral(o.getValue());
        }
        if (object==null) System.out.println("GOT NULL Object");

        return ResourceFactory.createStatement(subject,predicate,object);


    }
}
