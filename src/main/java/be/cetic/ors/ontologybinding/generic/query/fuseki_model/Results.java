package be.cetic.ors.ontologybinding.generic.query.fuseki_model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Results {

    private List<Binding> bindings = new ArrayList<Binding>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The bindings
     */
    public List<Binding> getBindings() {
        return bindings;
    }

    /**
     * 
     * @param bindings
     *     The bindings
     */
    public void setBindings(List<Binding> bindings) {
        this.bindings = bindings;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
