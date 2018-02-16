
package be.cetic.ors.ontologybinding.generic.query;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public abstract class SparqlModel {

    //private Head head;
    //private Results results;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The head
    public Head getHead() {
        return head;
    }
     */

    /**
     * 
     * @param head
     *     The head
    public void setHead(Head head) {
        this.head = head;
    }
     */

    /**
     * 
     * @return
     *     The results
    public Results getResults() {
        return results;
    }
     */

    /**
     * 
     * @param results
     *     The results
    public void setResults(Results results) {
        this.results = results;
    }
     */

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public abstract ArrayList getAllValues();

}
