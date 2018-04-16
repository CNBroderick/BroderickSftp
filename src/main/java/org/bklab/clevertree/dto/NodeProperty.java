package org.bklab.clevertree.dto;

/**
 * @author Broderick
 */
public class NodeProperty
        extends CleverTreeDTO {
    private static final long serialVersionUID = 889958446301081115L;


    private String name;

    private boolean propertyProtected;

    private String value;


    public NodeProperty(String name, String value) {
        this.name = name;
        this.value = value;
        propertyProtected = false;
    }


    public NodeProperty(String name, String value, boolean propertyProtected) {
        this.name = name;
        this.value = value;
        this.propertyProtected = propertyProtected;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isProtected() {
        return propertyProtected;
    }

    public void setProtected(boolean propertyProtected) {
        this.propertyProtected = propertyProtected;
    }

    public String toString() {
        return value;
    }
}
