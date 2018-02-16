/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.ontologybinding.generic.query.fuseki_model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represent the model of a query done in SPARQL
 * @author fs
 */
	
@XmlRootElement()
public class Individual {
    private String uri;
    private String type;
    private String label;

    public Individual() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    
}
