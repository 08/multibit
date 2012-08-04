package org.multibit.crypto;

import java.util.Arrays;

public class EncryptedPrivateKey {

    // The initialisation vector for the AES encryption (16 bytes)
    private byte[] initialisationVector; 
       
    public EncryptedPrivateKey(byte[] initialisationVector, byte[] encryptedPrivateKey) {
        super();
        this.initialisationVector = initialisationVector;
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    // The encrypted private key
    private byte[] encryptedPrivateKey;

    public byte[] getInitialisationVector() {
        return initialisationVector;
    }

    public void setInitialisationVector(byte[] initialisationVector) {
        this.initialisationVector = initialisationVector;
    }

    public byte[] getEncryptedBytes() {
        return encryptedPrivateKey;
    }

    public void setEncryptedPrivateKey(byte[] encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(encryptedPrivateKey);
        result = prime * result + Arrays.hashCode(initialisationVector);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EncryptedPrivateKey other = (EncryptedPrivateKey) obj;
        if (!Arrays.equals(encryptedPrivateKey, other.encryptedPrivateKey))
            return false;

        if (!Arrays.equals(initialisationVector, other.initialisationVector))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EncryptedPrivateKey [initialisationVector=" + Arrays.toString(initialisationVector) + ", encryptedPrivateKey=" + Arrays.toString(encryptedPrivateKey) + "]";
    }       
}
