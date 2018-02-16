/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.analyzer;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

/**
 *
 * @author fs
 */
public class AnalyzerResult {
    
    private Class analysedClass;
    private String analysedClassName;
    
    // type , varName
    private HashMap<String, List<String>> variables;

    public Class getAnalysedClass() {
        return analysedClass;
    }

    public void setAnalysedClass(Class analysedClass) {
        this.analysedClass = analysedClass;
    }

    /**
     * @return Name of the class without package
     */
    public String getAnalysedClassName() {
        return analysedClassName;
    }

    public void setAnalysedClassName(String analysedClassName) {
        this.analysedClassName = analysedClassName;
    }

    public HashMap<String, List<String>> getVariables() {
        return variables;
    }

    public Iterator getVariablesIterator() {
        return variables.entrySet().iterator();
    }

    public void setVariables(HashMap<String, List<String>> variables) {
        this.variables = variables;
    }
    
    
    
}
