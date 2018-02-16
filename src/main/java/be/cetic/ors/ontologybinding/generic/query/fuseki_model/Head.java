
package be.cetic.ors.ontologybinding.generic.query.fuseki_model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Head {

    private List<String> vars = new ArrayList<String>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The vars
     */
    public List<String> getVars() {
        return vars;
    }

    /**
     * 
     * @param vars
     *     The vars
     */
    public void setVars(List<String> vars) {
        this.vars = vars;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
