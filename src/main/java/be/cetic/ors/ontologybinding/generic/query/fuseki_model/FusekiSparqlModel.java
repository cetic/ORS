
package be.cetic.ors.ontologybinding.generic.query.fuseki_model;

import be.cetic.ors.ontologybinding.generic.query.SparqlModel;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class FusekiSparqlModel extends SparqlModel{

    private Head head;
    private Results results;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The head
     */
    public Head getHead() {
        return head;
    }

    /**
     * 
     * @param head
     *     The head
     */
    public void setHead(Head head) {
        this.head = head;
    }

    /**
     * 
     * @return
     *     The results
     */
    public Results getResults() {
        return results;
    }

    /**
     * 
     * @param results
     *     The results
     */
    public void setResults(Results results) {
        this.results = results;
    }


    public ArrayList getAllValues(){
        ArrayList values = new ArrayList();
        List<Binding> bindings = this.getResults().getBindings();
        for (Binding binding : bindings) {
            if (binding.getType()!=null) values.add(binding.getType().getValue());
            if (binding.getSubject()!=null)values.add(binding.getSubject().getValue());
            if (binding.getPredicate()!=null)values.add(binding.getPredicate().getValue());
            if (binding.getObject()!=null)values.add(binding.getObject().getValue());
        }
        return values;
    }


}
