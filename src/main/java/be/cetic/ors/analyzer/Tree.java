/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.analyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author fs   
 */
public class Tree<T> implements Iterable<Tree<T>> {

    T data;
    Tree<T> parent;
    List<Tree<T>> children;

    public static Logger logger = Logger.getLogger(Tree.class.getName());

    public Tree(T data) {
        this.data = data;
        this.children = new LinkedList<Tree<T>>();
    }

    public T getData() {
        return data;
    }

    public Tree<T> addChild(T child) {
        
        if(findChildData(child)!=null){
            return findChildData(child);
        }else{
            //create new node
            Tree<T> childNode = new Tree<T>(child);
            childNode.parent = this;
            this.children.add(childNode);
            return childNode;
        }
    }
    
    private Tree findChildData(T data){
        for (Tree tree: this.children){
            if(tree.data.equals(data)){
                return tree; 
            }
        }
        return null;
    }

    /**
     * returns all super class that inherit directly from java.lang.Object
     * @return 
     */
    public ArrayList<Tree<T>> getModelSuperClasses(){
        ArrayList<Tree<T>> datas = new ArrayList<>();
        for (Tree<T> tree: this.children){
            datas.add(tree);
        }
        return datas;
    }

    public ArrayList<Tree<T>> getModelClasses(Tree<T> subTree, ArrayList<Tree<T>> datas){
        if ( datas==null) datas = new ArrayList<Tree<T>>();
        for (Tree<T> tree: subTree.children){
            datas.add(tree);
            logger.info("adding "+tree.getData().toString());
            datas=getModelClasses(tree, datas);
        }
        return datas;
    }

    public ArrayList<Tree<T>> getInheritorClasses(Tree<T> family, String classname, ArrayList<Tree<T>> classes){
        for (Tree<T> tree: family.children){
            classes = new ArrayList<Tree<T>>();
            String data=tree.data.toString();
            if (data.contains(classname))return classes;
            else {
                classes=getInheritorClasses(tree, classname, classes);
                if (classes!=null){
                    classes.add(tree);
                    logger.info("inheritor class of "+classname+" is "+data);
                    return classes;
                }
            }
        }
        return null;
    }
    
    /**
     * return all child data from a selected root tree
     * @param subTree
     * @return 
     */
    public ArrayList<T> getAllChildData(/*Tree<T> subTree*/){
        ArrayList<T> result = new ArrayList<>();
        return getAllChild(this, result);
    }
    
    private ArrayList<T> getAllChild(Tree<T> subTree, ArrayList<T> dataList){
        dataList.add(subTree.data);
        for(Tree<T> t:subTree.children){
            getAllChild(t, dataList);
        }
        return dataList;
    }
    
    // other features ...
    @Override
    public Iterator<Tree<T>> iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
