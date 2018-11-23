package be.cetic.ors.ontologybinding.generic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.net.URI;
import be.cetic.ors.ontologybinding.generic.ToDBManager;
//import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.util.iterator.ExtendedIterator;


/** Manager of the model for the View related Rest API
 *
 */
public class ViewManager extends ToDBManager {

    public static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("ViewManager");

    public ViewManager() throws IOException
    {
    }

     public ArrayList<ClassView> listClasses(String uri)
     {
         ArrayList<ClassView> classList = new ArrayList<ClassView>();
         ExtendedIterator<OntClass> rootClassIt;
         if (uri.length()>1)rootClassIt = this.getOntModel(uri).listHierarchyRootClasses();
         else rootClassIt = this.getOntModel().listHierarchyRootClasses();
         while (rootClassIt.hasNext() ) {
            classList.add(parseClass(rootClassIt.next()));
         }
         return classList;
     }
         


     private ClassView parseClass(OntClass cls){
         String classURI=cls.getURI();
         if (cls.getURI()==null) classURI="_:b"+UUID.randomUUID();
         ClassView vc= new ClassView(classURI);
         if (cls.getComment(null)!=null) vc.setComment(cls.getComment(null));
         if (cls.getLabel(null)!=null)vc.setLabel(cls.getLabel(null));
         if (cls.getEquivalentClass()!=null){
             ExtendedIterator<OntClass> eqClassIt = cls.listEquivalentClasses();
             while(eqClassIt.hasNext()){
                 OntClass eq = eqClassIt.next();
                 ClassView eqv =new ClassView(eq.getURI());
                 vc.addEquivalentTo(eqv);
             }
         }
         if (cls.getSubClass()!=null){
             ExtendedIterator<OntClass> subClassIt = cls.listSubClasses();
             while(subClassIt.hasNext()){
                 OntClass sub = subClassIt.next();
                 ClassView subv = parseClass(sub);
                 vc.addSubClass(subv);
             }
         }
         return vc;
     }


    public ArrayList<String> listClassNames(String uri)
    {
        ArrayList<String> classList = new ArrayList<String>();
        ExtendedIterator<OntClass> typeClassIt;
        if (uri.length()>1) typeClassIt = this.getOntModel(uri).listNamedClasses();
        else typeClassIt = this.getOntModel().listNamedClasses();
        while (typeClassIt.hasNext()) {
            OntClass cl = typeClassIt.next();
            classList.add(cl.getURI());
        }
        logger.info("Class list:\n"
                +Arrays.toString(classList.toArray()));
        return classList;
    }

    public ArrayList<PropertyView> listProperties(String graphuri, String namespace, String classname, boolean direct)
    {
        String uri=namespace+classname;
        if (!uri.startsWith("http")){
            uri=this.getOntModel().getNsPrefixURI("")+classname;
        }
        logger.info("Properties for class:"+uri);
        ArrayList<PropertyView> propertyList = new ArrayList<PropertyView>();
        OntClass typeClass; 
        if (graphuri.length()>1) typeClass = this.getOntModel(graphuri).getOntClass(uri);
        else typeClass = this.getOntModel().getOntClass(uri);
        if (typeClass == null) {
            logger.info("Typeclass is null!!!");
            //typeClass = ontmodel.createClass(uri);
            return propertyList;
        }
        ExtendedIterator<OntProperty> propIt = typeClass.listDeclaredProperties(direct);
        while (propIt.hasNext()) {
            OntProperty prop = propIt.next();
            PropertyView vp= new PropertyView(prop.getURI());
            if (prop.getRange()!=null)vp.setRange(prop.getRange().getURI());
            if (prop.getDomain()!=null)vp.setDomain(prop.getDomain().getURI());
            if (prop.getInverse()!=null)vp.setInverse(prop.getInverse().getURI());
            if (prop.getInverseOf()!=null)vp.setInverseOf(prop.getInverseOf().getURI());
            if (prop.getEquivalentProperty()!=null)vp.setEquivalent(prop.getEquivalentProperty().getURI());
            vp.setIsFunctional(prop.isFunctionalProperty());
            vp.setIsInverseFunctional(prop.isInverseFunctionalProperty());
            propertyList.add(vp);
                //logger.info("Property "+prop.getURI()+" is Object"); 
        }
        logger.info("Got properties:\n"
                +Arrays.toString(propertyList.toArray()));
        return propertyList;
    }
}
