package org.bklab.clevertree.security;

import org.bklab.clevertree.dto.SecurityData;
import org.bklab.clevertree.exception.SecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Broderick
 */
public class SecurityHelper {
    private static final int CIPHER_BYTE_SIZE = 16;
    private Cipher cipher;
    private IvParameterSpec iv;
    private SecretKeySpec skeySpec;

    public SecurityHelper(SecurityData securityData)
            throws SecurityException {
        try {
            byte[] byteKey = securityData.getKey().getBytes("UTF-8");
            if (byteKey.length < 16) {
                byte[] newByteKey = new byte[16];
                for (int i = 0; i < byteKey.length; i++) {
                    newByteKey[i] = byteKey[i];
                }
                for (int i = byteKey.length; i < 16; i++) {
                    newByteKey[i] = 0;
                }
                byteKey = newByteKey;
            } else if (byteKey.length > 16) {
                byte[] newByteKey = new byte[16];
                for (int i = 0; i < byteKey.length; i++) {
                    newByteKey[i] = byteKey[i];
                }
                byteKey = newByteKey;
            }

            byte[] byteInitVector = securityData.getInitVector().getBytes("UTF-8");
            if (byteInitVector.length < 16) {
                byte[] newByteInitVector = new byte[16];
                for (int i = 0; i < byteInitVector.length; i++) {
                    newByteInitVector[i] = byteInitVector[i];
                }
                for (int i = byteInitVector.length; i < 16; i++) {
                    newByteInitVector[i] = 0;
                }
                byteInitVector = newByteInitVector;
            } else if (byteInitVector.length > 16) {
                byte[] newByteKeyInitVector = new byte[16];
                for (int i = 0; i < byteInitVector.length; i++) {
                    newByteKeyInitVector[i] = byteInitVector[i];
                }
                byteInitVector = newByteKeyInitVector;
            }
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            iv = new IvParameterSpec(byteInitVector);
            skeySpec = new SecretKeySpec(byteKey, "AES");
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }


    public byte[] decrypt(byte[] data)
            throws SecurityException {
        if (data == null) {
            return null;
        }
        try {
            cipher.init(2, skeySpec, iv);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }


    public byte[] encrypt(byte[] data)
            throws SecurityException {
        if (data == null) {
            return null;
        }
        try {
            cipher.init(1, skeySpec, iv);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }
}
