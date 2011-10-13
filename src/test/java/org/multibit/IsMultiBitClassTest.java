/**
 * Copyright 2011 MultiBit
 */

package org.multibit;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Test to ensure that the multibit duplicates of bitcoinj classes are available
 * correctly
 * 
 * @author jim
 * 
 */
public class IsMultiBitClassTest extends TestCase {
    private static final String GOOGLE_PREFIX = "com.google.bitcoin.core.";

    @Test
    public void testIsMultiBitClass() throws ClassNotFoundException {
        checkClass("Peer");
        checkClass("PeerGroup");
        checkClass("Sha256Hash");
        checkClass("Transaction");
        checkClass("TransactionInput");
        checkClass("Wallet");
        checkClass("WalletEventListener");
    }

    private void checkClass(String className) throws ClassNotFoundException {
        Class<? extends Object> clazz = Class.forName(GOOGLE_PREFIX + className);

        Class<? extends Object>[] interfaces = clazz.getInterfaces();
        if (interfaces == null) {
            fail("Class " + clazz + " does not implement isMultiBitClass marker interface");
        } else {
            boolean success = false;
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i].getName().equals("org.multibit.IsMultiBitClass")) {
                    success = true;
                    break;
                }
            }
            if (success) {
                // carry on
            } else {
                fail("Class " + clazz + " does not implement isMultiBitClass marker interface");
            }
        }

    }
}
