/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.ontologybinding.generic.query;

import generated.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.RandomStringUtils;

/**
 *
 * @author nm
 */
public class QueryManager extends GenericQueryManager {

    private static final Logger logger = LoggerFactory.getLogger(QueryManager.class);

    public QueryManager(Query query) throws QueryManagerException, java.io.IOException {
        super(query);
    }

    /**
     * TODO generated class. 
     * The class generator grabs all namespaces defined in namespace.config and adds them as prefixes in SPARQL query.
     * Though this is not needed. So implement generator only if such functionality is required.
     * 
     * @return String
     */
    String sparqlPrefixes(){
        //StringBuilder sb = new StringBuilder("PREFIX : <http://www.semanticweb.org/nm/ontologies/2017/6/mobits-lite#>\n");
        //sb.append("PREFIX ext1: <http://www.semanticweb.org/nm/ontologies/2017/6/TECSTP#>\n");
        return ""; //sb.toString();
    }

    /**
     * Returns sparql query linked to fields
     *
     * @return
     */
    String sparqlFields() {

        logger.info("Parsing fields");

        StringBuilder sb = new StringBuilder();//"?uri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type. \n");
        boolean firstTime = true;

        for (String field : fields) {
            if (!firstTime) {
                sb.append(" UNION \n");
            } else {
                firstTime = false;
            }
            /*TODO? If special checks are required do them here. Otherwise the default will do.
            if (field.equals("TecStop"))
                sb.append("{?uri a ext1:TecStop .");
            else if (field.equals("TecLocation"))
                sb.append("{?uri a ext1:TecLocation .");
            //sb.append("{?uri a <http://www.semanticweb.org/nm/ontologies/2017/6/TECSTP#TecLocation>.");
            else {
                if (field.contains("http")) 
                    sb.append("{?uri a <"+field+">.");
                else sb.append("{?uri a :"+field+".");
            }
             */
            sb.append("{?uri a <"+field+">.");
            sb.append("\n");
            sb.append("?uri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type.}");
            selectFields+="?uri ?type ";
            if (!firstTime) {
                sb.append("\n");
            }

        }
        return sb.toString();

    }

    String sparqlExpression() throws QueryManagerException {
        logger.info("Parsing expression");

        List<String> validOperator = Arrays.asList("in", "not in", "textsearch");

        StringBuilder sb = new StringBuilder("\n");

        for (Expression expression : expressions) {
            String operator = expression.getOperator().toLowerCase();
            logger.info("operator: " + operator);
            if (validOperator.contains(operator)) {
                if (operator.equals("textsearch")) {
                    String randomVar = RandomStringUtils.randomAlphanumeric(5);
                    sb.append("OPTIONAL {?uri <").append(expression.getPredicate()).append("> ?").append(randomVar).append(" }\n");
                    sb.append("filter (REGEX(str(?").append(randomVar).append("), '").append(expression.getValue()).append("','i')).\n");
                    selectFields+="?"+randomVar+" ";
                } else {
                    // returns ?uri propertyNs:eventLocatedNear ?property.
                    sb.append("?uri <").append(expression.getPredicate()).append("> ?property. ").append("\n");
                    // returns filter (resourceNs:Aubange in (?property)).
                    sb.append("filter (<").append(expression.getValue()).append("> ").append(operator).append(" ").append("(?property)).").append("\n");
                    selectFields+="?property ";
                }
            } else {
                throw new QueryManagerException("Unknown operator found un the query. Available operator are: " + String.join(",", validOperator));
            }
        }
        return sb.toString();
    }

    String sparqlIndividual(){
        logger.info("Parsing individuals");

        //StringBuilder sb = new StringBuilder();
        ArrayList<String> individuals = new ArrayList<>();
        for (String id : ids) {
            // returns <http://www.semanticweb.org/ontologies/2015/0/redirnet-core-ontology#BelgianPolice> ?property ?value.
            //sb.append("<").append(id).append("> ?uri ?value.\n");
            //individuals.add("{<"+id+"> ?uri ?value}");
            individuals.add("{<" + id + "> ?property ?uri}");
            if (!selectFields.contains("property"))
                    selectFields+="?property ";
            if (!selectFields.contains("uri"))
                    selectFields+="?uri ";
        }
        String result = String.join(" UNION ", individuals);
        return result + "\n";
    }




}
