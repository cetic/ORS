/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.analyzer;

import java.util.ArrayList;

import java.util.List;

/**
 *
 * @author fs
 */
public enum PrimitiveType {
    BYTE("byte"),
    BIG_DECIMAL("BigDecimal"), 
    XMLGREGORIANCALENDAR("XMLGregorianCalendar"), 
    SHORT("short"), INT("int"), LONG("long"), FLOAT("float"), DOUBLE("double"), 
    BOOLEAN("boolean"), 
    CHAR("char"), 
    STRING("String"), 
    FLOAT_OBJECT("Float"),
    LONG_OBJECT("Long"),
    DOUBLE_OBJECT("Double"),
    INTEGER_OBJECT("Integer"),
    BOOLEAN_OBJECT("Boolean"), 
    OBJECT("Object");
    
    
    private String name = "";

    PrimitiveType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static List<String> getAllPrimitiveType() {

        ArrayList<String> names = new ArrayList<String>();
        for (PrimitiveType type : values()) {
            names.add(type.getName());
        }

        return names;
    }

    public static PrimitiveType getPrimitiveTypeByName(String src) throws PrimitiveTypeException {

        for (PrimitiveType rs : values()) {
            if (rs.getName().equals(src)) {
                return rs;
            }
        }
        throw new PrimitiveTypeException("The requested primitive doesn't exist: " + src);
    }
}
