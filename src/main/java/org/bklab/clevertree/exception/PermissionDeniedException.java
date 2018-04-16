package org.bklab.clevertree.exception;

/**
 * @author Broderick
 */
public class PermissionDeniedException
        extends SmarTreeException {
    private static final long serialVersionUID = 6672186863392660130L;


    public PermissionDeniedException(String propertyName) {
        super(propertyName);
    }
}
