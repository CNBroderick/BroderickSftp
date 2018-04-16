package org.bklab.clevertree.exception;

/**
 * @author Broderick
 */
public class BadPathException
        extends SmarTreeException {
    private static final long serialVersionUID = 6672186863392660130L;


    public BadPathException(String path) {
        super(path);
    }
}
