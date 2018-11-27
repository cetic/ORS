package be.cetic.ors.ontologybinding.generic.document;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdError.Error;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.core.RDFParser;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Individual;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.rdf.model.RDFVisitor;
import org.apache.jena.rdf.model.AnonId;

import java.util.Map;



public class ORSParser implements RDFParser {

    public static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("ORSParser");

    String context;
    public void setContext(String context){
        this.context=context;
    }

    public String getContext(){
        return context;
    }

    String clsUri;
    public void setClsUri(String clsUri){
        this.clsUri=clsUri;
    }

    public String getClsUri(){
        return clsUri;
    }

    String clsId;
    public void setClsId(String clsId){
        this.clsId=clsId;
    }

    public String getClsId(){
        return clsId;
    }

    void importModel(RDFDataset result, OntModel model){
        final Map<String, String> nsPrefixMap = model.getNsPrefixMap();
        for (final String prefix : nsPrefixMap.keySet()) {
            result.setNamespace(prefix, nsPrefixMap.get(prefix));
        }

        //if(context==null) context= model.getGraph().toString();
        StmtIterator stmts = model.listStatements();
        while (stmts.hasNext()){
            handleStatement(result, stmts.next(), context);
        }
    }

    void importIndividuals(RDFDataset result, OntModel model){
            logger.info("Fetching individuals");
        final Map<String, String> nsPrefixMap = model.getNsPrefixMap();
        for (final String prefix : nsPrefixMap.keySet()) {
            result.setNamespace(prefix, nsPrefixMap.get(prefix));
        }

            logger.info("for resource "+clsUri);
        Resource cls=model.createResource(clsUri);//"http://case.example.org/core#Bundle");
        ExtendedIterator<Individual> inds = model.listIndividuals(cls);
        while (inds.hasNext()){
            Individual ind =inds.next();
            logger.info("individual:"+ind.getURI()+" <> "+clsId);
            if (clsId!=null && clsId.length()>1){
                if (ind.getURI().equals(clsId))
                    handleIndividual(result, ind /*s.next()*/, context);
            }
            else
                    handleIndividual(result, ind /*s.next()*/, context);
        }
    }


    private void handleIndividual(RDFDataset result, Individual ind, String context) {
        StmtIterator stmts=ind.listProperties();
            logger.info("Handling individual: "+ind);
        while (stmts.hasNext()){
             handleStatement(result, stmts.next(), context);
         }
    }

    private void handleStatement(RDFDataset result, Statement statement, String context) {
        final Resource subject = statement.getSubject();
        final Property predicate = statement.getPredicate();
        final RDFNode object = statement.getObject();

          RDFVisitor rdfVisitor = new RDFVisitor() {

              @Override
                  public Object visitURI(Resource r, String uri) {
                      //System.out.println("RDFVisitor ----> "+uri);
                      return uri;
                  }

              @Override
                  public Object visitLiteral(Literal l) {
                      //System.out.println("RDFVisitor ----> "+l);
                      return l.getLexicalForm();
                  }

              @Override
                  public Object visitBlank(Resource r, AnonId id) {
                      //System.out.println("RDFVisitor ----> blank node "+id);
                      return id.getLabelString();
                  }
          };


        //System.out.println("Got statemtnet: "+statement);
        if (object.isLiteral()) {
            final Literal literal = object.asLiteral();
            addStatement(result, context, subject, predicate, literal.getValue(),
                    literal.getDatatype().getURI());
        }
       /*  else if (object instanceof LanguageTagLiteral) {
            final LanguageTagLiteral literal = (LanguageTagLiteral) object;
            addStatement(result, context, subject, predicate, literal.getValue(),
                    literal.getLanguageTag()); 
                    }
                    */
        else if (object.canAs(Individual.class) ) {
            //final Literal literal = object.asLiteral();
            //addStatement(result, context, subject, predicate, literal.getValue());
            Individual ind=object.as(Individual.class);
            addStatement(result, context, subject, predicate, object.asResource().getURI());
            handleIndividual(result, ind, context);
        }
        else if (object.isURIResource()){
            addStatement(result, context, subject, predicate, object.asResource().getURI());
        }
        else {
            //ignore blank nodes 
            //
            //System.out.println("Ignoring statement with object: "+object.toString());
            // visitWith(rdfVisitor).toString());
        }
    }

    private void addStatement(RDFDataset result, String context, Resource subject, Property predicate,
            String object) {
        if (context == null) {
            logger.info("Triple: subject="+subject+" predicate="+predicate+" object="+object); 
            result.addTriple(subject.getURI(), predicate.getURI(), object);
        } else {
            result.addQuad(subject.getURI(), predicate.getURI(), object,
                    context);
        }
    }

    /*private void addStatement(RDFDataset result, URI context, Resource subject, URI predicate,
            String value) {
        if (context == null) {
            result.addTriple(subject.toString(), predicate.toString(), value, null, null);
        } else {
            result.addQuad(subject.toString(), predicate.toString(), value, null, null,
                    context.toString());
        }
    }*/

    private void addStatement(RDFDataset result, String context, Resource subject, Property predicate,
            Object value, String datatype) {
        if (context == null) {
            result.addTriple(subject.getURI(), predicate.getURI(), value.toString(), datatype, null);
        } else {
            result.addQuad(
                    subject.getURI(), predicate.getURI(), value.toString(), datatype, null , context);
        }
    }

    /*private void addStatement(RDFDataset result, URI context, Resource subject, URI predicate,
            String value, String language) {
        if (context == null) {
            result.addTriple(subject.toString(), predicate.toString(), value, null, language);
        } else {
            result.addQuad(subject.toString(), predicate.toString(), value, null, language,
                    context.toString());
        }
    }*/



    @Override
    public RDFDataset parse(Object input) throws JsonLdError {
        final RDFDataset result = new RDFDataset();

        // empty dataset if no input given
        if (input == null) {
            return result;
        }

        if (input instanceof OntModel) {
            //importModel(result, (OntModel) input);
            importIndividuals(result, (OntModel) input);
        }/* else if (input instanceof Model) {
            importModel(result, .createOntModel((Model)input));
        }*/ else {
            throw new JsonLdError(Error.INVALID_INPUT,
                    "ORS parser expects a  Jena Model or OntModel as input");
        }

        return result;
    }

}
