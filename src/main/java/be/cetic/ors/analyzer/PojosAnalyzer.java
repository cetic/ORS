/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.analyzer;

import static be.cetic.ors.Main.logger;
import com.github.javaparser.ParseException;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author fs 1. Aggregates all the results from the SinglePojoAnalyzer in one
 * hashmap. 2. Replaces non-primitive-types by the result of the coresponding
 * output from singlepojoanalyser. The primitivetypes are defined by an enum in
 * the PrimitiveType.java.
 */
public class PojosAnalyzer {

    List<File> javaFiles;
    HashMap<String, HashMap<String, List<String>>> ast = new HashMap<>();
    HashMap<String, AnalyzerResult> analyzerResults = new HashMap<>();
    Tree rootHierarchy = new Tree("java.lang.Object");

    public PojosAnalyzer(List<File> javaFiles) throws ParseException, IOException, FileNotFoundException, JClassAlreadyExistsException, ClassNotFoundException {
        this.javaFiles = javaFiles;

        // general map
        // className, typeVar, nameVar
        //HashMap<String, HashMap<String, List<String>>> astVars = new HashMap<>();

        //1st pass: aggregation of SinglePojoAnalyzer results
        for (File javaFile : javaFiles) {
            SinglePojoAnalyzer pojoAnalyzer = new SinglePojoAnalyzer(javaFile);
            //astVars.put(pojoAnalyzer.getAnalyzerResult().getAnalysedClassName(), pojoAnalyzer.getAnalyzerResult().getVariables());
            ast.put(pojoAnalyzer.getAnalyzerResult().getAnalysedClass().getName(), pojoAnalyzer.getAnalyzerResult().getVariables());
            analyzerResults.put(pojoAnalyzer.getAnalyzerResult().getAnalysedClass().getName(), pojoAnalyzer.getAnalyzerResult());
            insertClassHierarchy(rootHierarchy, 1, pojoAnalyzer.hierarchy);// 1 to escape the java.lang.Object

        }

        //logger.log(Level.INFO, "AST (first pass): {0}", Arrays.asList(astVars));
        logger.log(Level.INFO, "AST (first pass): {0}", Arrays.asList(ast));

        /* 2nd pass 
         * Fix structure and add inherited variables
         * No extends keyword is added.
         */

        ast.forEach((k, v) -> {
            HashMap<String, List<String>> vars = new HashMap<>();
            vars.putAll(v);
            logger.log(Level.INFO, "Vars (second pass): {0}", Arrays.asList(vars));

            ArrayList<Tree> inheritanceTree= rootHierarchy.getInheritorClasses(rootHierarchy, k,null);
            for (Tree ikt: inheritanceTree){
                HashMap<String, List<String>> ikvars=ast.get(ikt.data); 
                logger.log(Level.INFO, "Inner loop: Inherited Vars (second pass): {0}", Arrays.asList(ikvars));
                ikvars.forEach((ikkey, ikvalue) -> {
                    if (!vars.containsKey(ikkey))vars.put(ikkey,ikvalue);
                    else{
                       vars.get(ikkey).addAll(ikvalue);
                       //the following statements in this block are only for removing any duplicates.
                       HashSet hs = new HashSet();
                       hs.addAll(vars.get(ikkey));
                       vars.get(ikkey).clear();
                       vars.get(ikkey).addAll(hs);
                    }
                });
                //if (ikvars!=null)vars.putAll(ikvars);
                logger.log(Level.INFO, "Inner loop: all  Vars (second pass): {0}", Arrays.asList(vars));
            }

            analyzerResults.get(k).setVariables(vars);
        });




        /* replaces non-primitive type with the results form singlepojo, so the structure becomes flat. 
        // Special algorithm by FS.
        // AST optimisation only PrimitiveType in variables
        astVars.forEach((keyClass, alVars) -> {
            ast.put(keyClass, new HashMap<>());
            //System.out.println("Key : " + keyClass + " Value : " + alVars);
            alVars.forEach((keyTypeVar, nameVars) -> {
                //System.out.println("SKey : " + keyTypeVar + " Value : " + nameVar);
                if (!PrimitiveType.getAllPrimitiveType().contains(keyTypeVar) && astVars.containsKey(keyTypeVar)) {
                    nameVars.forEach((nameVar) -> {
                        astVars.get(keyTypeVar).forEach((key, vals) -> {
                            ArrayList<String> val = new ArrayList<>();
                            vals.forEach((x) -> {
                                val.add(nameVar + "." + x);
                            });
                            ast.get(keyClass).put(key, val);
                        });
                    });
                } else {
                    ast.get(keyClass).put(keyTypeVar, nameVars);
                }
            });

        });*/


        /*Step 3. update the structure into the expected format. 
        ast.forEach((k, v) -> {
            analyzerResults.get(k).setVariables(v);
        });*/
    }

    private void insertClassHierarchy(Tree tree, int cpt, ArrayList<String> hierarchy) {
        if (cpt < hierarchy.size()) {
            Tree currentTree = tree.addChild(hierarchy.get(cpt));
            insertClassHierarchy(currentTree, ++cpt, hierarchy);
        }
    }

    /**
     * [{LOCATION={String=[id]}, Station={String=[aa]}, Agent={String=[name,
     * ability]},
     * GEOGRAPHICPOINT={BigDecimal=[geographicPointLongitudeCooordinate,
     * geographicPointLatitudeCooordinate, geographicPointAltitude]},
     * Resource={String=[id, label, comment],
     * BigDecimal=[currentLocation.geographicPointLongitudeCooordinate,
     * currentLocation.geographicPointLatitudeCooordinate,
     * currentLocation.geographicPointAltitude]}, Event={String=[description,
     * id]}, Resource2={String=[id, label, comment]},
     * Sensor={String=[sensorFunction, localization, timeInformation, mobility,
     * orientation]}}]
     *
     * @return
     */
    public HashMap<String, HashMap<String, List<String>>> getAst() {
        return ast;
    }

    public HashMap<String, AnalyzerResult> getAnalyzerResults() {
        return analyzerResults;
    }

    public Tree getRootHierarchy() {
        return rootHierarchy;
    }
    
}
