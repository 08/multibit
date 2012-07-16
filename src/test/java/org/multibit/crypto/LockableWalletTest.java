/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.multibit.crypto;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.WalletType;
import com.google.bitcoin.utils.BriefLogFormatter;

public class LockableWalletTest extends TestCase {
    static final NetworkParameters params = NetworkParameters.unitTests();

    private EncryptableWallet wallet;
    private ECKey myKey;

    private static char[] PASSWORD1 = "my helicopter contains eels".toCharArray();

    @Before
    public void setUp() throws Exception {
        myKey = new ECKey();
        wallet = new EncryptableWallet(params);
        wallet.addKey(myKey);

        BriefLogFormatter.init();
    }

    @Test
    public void testBasic() throws Exception {
        // Check the wallet is initially unencrypted and unlocked.
        assertTrue("Wallet is not an unencrypted wallet", wallet.getWalletType() == WalletType.UNENCRYPTED);
        assertTrue("Wallet is not unlocked", !wallet.isCurrentlyEncrypted());
        
        // Encrypt wallet.
        wallet.encrypt(PASSWORD1);

        // Wallet should now be of type encrypted and locked.
        assertTrue("Wallet is not an encrypted wallet", wallet.getWalletType() == WalletType.ENCRYPTED);
        assertTrue("Wallet is not locked", wallet.isCurrentlyEncrypted());

        // Decrypt wallet.
        wallet.decrypt(PASSWORD1);
        
        // Wallet should now be of type encrypted and unlocked.
        assertTrue("Wallet is not an encrypted wallet", wallet.getWalletType() == WalletType.ENCRYPTED);
        assertTrue("Wallet is not locked", !wallet.isCurrentlyEncrypted());
        
        // Remove the wallet encryption entirely.
        wallet.removeEncryption(PASSWORD1);

        // Wallet should now be of type unencrypted and unlocked.
        assertTrue("Wallet is not an unencrypted wallet", wallet.getWalletType() == WalletType.UNENCRYPTED);
        assertTrue("Wallet is not locked", !wallet.isCurrentlyEncrypted());
    }
}
