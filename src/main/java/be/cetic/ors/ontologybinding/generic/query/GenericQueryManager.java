/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.ontologybinding.generic.query;

import generated.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fs
 */
public abstract class GenericQueryManager {

    List<Expression> expressions = new ArrayList<Expression>();
    List<String> ids = new ArrayList<>();
    List<String> fields;
    String selectFields="";//Always leave a space after the variable name
    public static List<String> validOperator = Arrays.asList("in", "not in", "textsearch");

    private static final Logger logger = LoggerFactory.getLogger(GenericQueryManager.class);

    public GenericQueryManager(Query query) throws QueryManagerException, java.io.IOException {

        // Analysing filters and getting list of instances and expressions
        List<QueryFilter> filters = query.getFilter();
        List<Individual> instances = new ArrayList<Individual>();
        for (QueryFilter filter : filters) {
            expressions.addAll(filter.getExpression());
            instances.addAll(filter.getIndividual());
        }


        // getting all id
        for (Individual i : instances) {
            ids.add(i.getId());
        }
        logger.info("Instances asked: " + String.join(",", ids));

        // Analysing fields
        fields = query.getField();
        try {
            for (String field : fields) {
                logger.info("Checking field: " + field);
            }
        } catch (Exception e) {
            throw new QueryManagerException("Bad field name");
        }

    }


    /** The following are implemented in QueryManager class
     *
     */
    abstract String sparqlPrefixes();

    abstract String sparqlFields();

    abstract String sparqlExpression() throws QueryManagerException;

    abstract String sparqlIndividual();
    

    /**
     * Transform the xml query into a Sparql query This method executes the
     * sparql query and returns a datamodel
     *
     * @return
     * @throws QueryManagerException
     * @throws IOException
     * @throws SparqlManagerException
     */
    public String generateSparqlQuery() throws QueryManagerException {
        logger.info("Creating Sparql query");
        StringBuilder sb = new StringBuilder("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n");
        StringBuilder wb = new StringBuilder( " WHERE {\n");
        wb.append(sparqlFields());
        wb.append(sparqlIndividual());
        wb.append(sparqlExpression());
        wb.append("OPTIONAL { ?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label }");//adding labels if available
        selectFields+="?label ";
        wb.append("}");

        sb.append(sparqlPrefixes());//"PREFIX resourceNs: <http://www.semanticweb.org/ontologies/2015/0/redirnet-core-ontology#>\n");
        sb.append("\n");
        sb.append("SELECT Distinct ");
        sb.append(selectFields);

        String sparqlQuery = sb.toString()+wb.toString();
        logger.info("Sparql query: ", sparqlQuery);

        return sparqlQuery;
    }

}
