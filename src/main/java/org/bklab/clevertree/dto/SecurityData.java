package org.bklab.clevertree.dto;

/**
 * @author Broderick
 */
public class SecurityData
        extends CleverTreeDTO {
    private static final long serialVersionUID = 4874324380768938169L;


    private String initVector;


    private String key;


    public SecurityData(String initVector, String key) {
        this.initVector = initVector;
        this.key = key;
    }


    public String getInitVector() {
        return initVector;
    }

    public void setInitVector(String initVector) {
        this.initVector = initVector;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String toString() { return "EncryptionData [initVector=" + initVector + ", key=" + key + "]";
    }
}
