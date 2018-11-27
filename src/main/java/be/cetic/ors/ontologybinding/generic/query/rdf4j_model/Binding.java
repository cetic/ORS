
package be.cetic.ors.ontologybinding.generic.query.rdf4j_model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "object",
    "predicate",
    "subject"
})
public class Binding {

    @JsonProperty("object")
    private Var object;
    @JsonProperty("predicate")
    private Var predicate;
    @JsonProperty("subject")
    private Var subject;
    @JsonProperty("graph")
    private Var graph;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("object")
    public Var getObject() {
        return object;
    }

    @JsonProperty("object")
    public void setObject(Var object) {
        this.object = object;
    }

    @JsonProperty("predicate")
    public Var getPredicate() {
        return predicate;
    }

    @JsonProperty("predicate")
    public void setPredicate(Var predicate) {
        this.predicate = predicate;
    }

    @JsonProperty("subject")
    public Var getSubject() {
        return subject;
    }

    @JsonProperty("subject")
    public void setSubject(Var subject) {
        this.subject = subject;
    }

    @JsonProperty("graph")
    public Var getGraph() {
        return graph;
    }

    @JsonProperty("grap")
    public void setGraph(Var graph) {
        this.graph = graph;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
