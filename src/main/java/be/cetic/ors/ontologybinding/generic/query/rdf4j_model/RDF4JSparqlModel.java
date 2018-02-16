
package be.cetic.ors.ontologybinding.generic.query.rdf4j_model;

import be.cetic.ors.ontologybinding.generic.query.SparqlModel;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "head",
    "results"
})
public class RDF4JSparqlModel extends SparqlModel{

    @JsonProperty("head")
    private Head head;
    @JsonProperty("results")
    private Results results;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public void setHead(Head head) {
        this.head = head;
    }

    @JsonProperty("results")
    public Results getResults() {
        return results;
    }

    @JsonProperty("results")
    public void setResults(Results results) {
        this.results = results;
    }


    public ArrayList getAllValues(){
        ArrayList values = new ArrayList();
        List<Binding> bindings = this.getResults().getBindings();
        for (Binding binding : bindings) { 
            if (binding.getSubject()!=null)values.add(binding.getSubject().getValue());
            if (binding.getPredicate()!=null)values.add(binding.getPredicate().getValue());
            if (binding.getObject()!=null)values.add(binding.getObject().getValue());
        }

        return values;
    }


}
