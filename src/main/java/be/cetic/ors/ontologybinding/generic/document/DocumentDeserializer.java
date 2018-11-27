package be.cetic.ors.ontologybinding.generic.document;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;


//import be.cetic.ors.ontologybinding.generic.ToDBManager;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;



public class DocumentDeserializer extends StdDeserializer<Object> {

    public static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("DocumentDeserializer");

    public DocumentDeserializer() 
    {
        this(null);
    }

    public DocumentDeserializer(Class<?> vc) 
    {
        super(vc);
    }

     @Override
     public Object deserialize(JsonParser jp, DeserializationContext context) throws IOException {
         logger.info("Importing document");
         JsonNode node = jp.getCodec().readTree(jp);
         java.util.Iterator<String> fn = node.fieldNames();
         while (fn.hasNext()){
             logger.info("Element: "+fn.next());
         }
         logger.info("JSON is:\n"+node);
         //InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
         return node;
     }

}
