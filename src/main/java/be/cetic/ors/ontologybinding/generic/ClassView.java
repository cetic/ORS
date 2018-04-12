package be.cetic.ors.ontologybinding.generic;

import java.util.List;
import java.util.ArrayList;

public class ClassView {

    private String uri;
    private String label;
    private String comment;
    private List<ClassView> equivalentTo;
    private List<ClassView> subClasses;


    public ClassView(String uri){
        this.subClasses=new ArrayList<ClassView>();
        this.equivalentTo=new ArrayList<ClassView>();
        this.uri=uri;
    }


    public String getUri(){
        return this.uri;
    }

    public void setUri(String uri){
        this.uri=uri;
    }


    //Comment
    public String getComment() {
        return comment;
    }

    public void setComment(String uri) {
        this.comment=uri;
    }

    //Label
    public String getLabel() {
        return label;
    }

    public void setLabel(String uri) {
        this.label=uri;
    }

    //Equivalent
    public List<ClassView> getEquivalentTo() {
        return equivalentTo;
    }

    public void setEquivalentTo(List<ClassView> uri) {
        this.equivalentTo=uri;
    }

    public void addEquivalentTo(ClassView uri) {
        this.equivalentTo.add(uri);
    }

    //Subclasses
    public List<ClassView> getSubClasses() {
        return subClasses;
    }

    public void setSubClasses(List<ClassView> uri) {
        this.subClasses=uri;
    }

    public void addSubClass(ClassView uri) {
        this.subClasses.add(uri);
    }

}
