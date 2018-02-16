/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.cetic.ors.ontologybinding.generic.exception;

/**
 *
 * @author fs
 */
public class ClassURIException extends Exception {

    public ClassURIException() {
    }

    public ClassURIException(String message) {
        super(message);
    }

    public ClassURIException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassURIException(Throwable cause) {
        super(cause);
    }

}
