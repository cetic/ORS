package be.cetic.ors.ontologybinding.generic;

//import com.fasterxml.jackson.annotation.JsonRootName;


public class PropertyView {

    private String uri;
    private String domain;
    private String range;
    private String inverse;
    private String inverseOf;
    private String equivalent;

    private boolean isFunctional;
    private boolean isInverseFunctional;

    public PropertyView(String uri){
        this.uri=uri;
    }


    public String getUri(){
        return this.uri;
    }

    public void setUri(String uri){
        this.uri=uri;
    }

    //Domain
    public String getDomain() {
        return domain;
    }

    public void setDomain(String uri) {
        this.domain=uri;
    }

    //Range
    public String getRange() {
        return range;
    }

    public void setRange(String uri) {
        this.range=uri;
    }

    //Inverse
    public String getInverse() {
        return inverse;
    }

    public void setInverse(String uri) {
        this.inverse=uri;
    }

    //InverseOf
    public String getInverseOf() {
        return inverseOf;
    }

    public void setInverseOf(String uri) {
        this.inverseOf=uri;
    }

    //Equivalent
    public String getEquivalent() {
        return equivalent;
    }

    public void setEquivalent(String uri) {
        this.equivalent=uri;
    }

    //isFunctional
    public boolean getIsFunctional() {
        return isFunctional;
    }

    public void setIsFunctional(boolean func) {
        this.isFunctional=func;
    }

    //isInverseFunctional
    public boolean getIsInverseFunctional() {
        return isInverseFunctional;
    }

    public void setIsInverseFunctional(boolean ifunc) {
        this.isInverseFunctional=ifunc;
    }

}
