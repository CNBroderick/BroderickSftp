package org.bklab.clevertree.exception;

/**
 * @author Broderick
 */
public abstract class SmarTreeException
        extends Exception {
    private static final long serialVersionUID = 2660385911824671021L;


    public SmarTreeException(String path) {
        super(path);
    }


    public SmarTreeException(Exception cause) {
        super(cause);
    }
}
