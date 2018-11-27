
package be.cetic.ors.ontologybinding.generic.query.fuseki_model;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Binding {

    private Uri uri;
    private Type type;
    private Label label;

    private Type object;
    private Type predicate;
    private Type subject;
    private Type graph;

    // must be generic
    /*private Property property;
      private Latitude latitude;
      private Longitude longitude;
      private Altitude altitude;*/
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Type getObject() {
        return object;
    }

    public void setObject(Type object) {
        this.object = object;
    }

    public Type getPredicate() {
        return predicate;
    }

    public void setPredicate(Type predicate) {
        this.predicate = predicate;
    }

    public Type getSubject() {
        return subject;
    }

    public void setSubject(Type subject) {
        this.subject = subject;
    }

    public Type getGraph() {
        return graph;
    }

    public void setGraph(Type graph) {
        this.graph = graph;
    }


    /**
     * 
     * @return
     *     The uri
     */
    public Uri getUri() {
        return uri;
    }

    /**
     * 
     * @param uri
     *     The uri
     */
    public void setUri(Uri uri) {
        this.uri = uri;
    }

    /**
     * 
     * @return
     *     The type
     */
    public Type getType() {
        return type;
    }

    /**
     * 
     * @param type
     *     The type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * 
     * @return
     *     The label
     */
    public Label getLabel() {
        return label;
    }

    /**
     * 
     * @param label
     *     The label
     */
    public void setLabel(Label label) {
        this.label = label;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
