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

package org.multibit.network;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.multibit.controller.MultiBitController;
import org.multibit.model.MultiBitModel;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.PeerAddress;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.core.ScriptException;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionInput;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.WalletEventListener;
import com.google.bitcoin.store.BlockStore;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.BoundedOverheadBlockStore;

/**
 * <p>
 * MultiBitService encapsulates the interaction with the bitcoin netork
 * including: o Peers o Block chain download o sending / receiving bitcoins
 * 
 * It is based on the bitcoinj PingService code
 * 
 * <p>
 * If running on TestNet (slow but better than using real coins on prodnet) do
 * the following:
 * <ol>
 * <li>Backup your current wallet.dat in case of unforeseen problems</li>
 * <li>Start your bitcoin client in test mode <code>bitcoin -testnet</code>.
 * This will create a new sub-directory called testnet and should not interfere
 * with normal wallets or operations.</li>
 * <li>(Optional) Choose a fresh address</li>
 * <li>(Optional) Visit the Testnet faucet
 * (https://testnet.freebitcoins.appspot.com/) to load your client with test
 * coins</li>
 * <li>Run <code>PingService -testnet</code></li>
 * <li>Wait for the block chain to download</li>
 * <li>Send some coins from your bitcoin client to the address provided in the
 * PingService console</li>
 * <li>Leave it running until you get the coins back again</li>
 * </ol>
 * </p>
 * 
 * <p>
 * The testnet can be slow or flaky as it's a shared resource. You can use the
 * <a href="http://sourceforge
 * .net/projects/bitcoin/files/Bitcoin/testnet-in-a-box/">testnet in a box</a>
 * to do everything purely locally.
 * </p>
 */
public class MultiBitService {
    public static final String MULTIBIT_PREFIX = "multibit";
    public static final String PRODUCTION_NET_PREFIX = "prodnet";
    public static final String TEST_NET_PREFIX = "testnet";
    public static final String SEPARATOR = "-";

    public static final String BLOCKCHAIN_SUFFIX = ".blockchain";
    public static final String WALLET_SUFFIX = ".wallet";

    private String walletFilename;
    private Wallet wallet;

    private PeerGroup peerGroup;

    private BlockChain chain;

    private MultiBitController controller;

    private final NetworkParameters networkParameters;

    /**
     * 
     * @param useTestNet
     *            true = test net, false = production
     * @param wallet
     *            filename current wallet filename
     */
    public MultiBitService(boolean useTestNet, MultiBitController controller) {
        this.controller = controller;

        networkParameters = useTestNet ? NetworkParameters.testNet() : NetworkParameters.prodNet();
        String filePrefix = useTestNet ? MULTIBIT_PREFIX + SEPARATOR + TEST_NET_PREFIX
                : MULTIBIT_PREFIX + SEPARATOR + PRODUCTION_NET_PREFIX;

        // Try to read the wallet from storage, create a new one if not
        // possible.
        walletFilename = controller.getModel().getUserPreference(MultiBitModel.WALLET_FILENAME);

        File walletFile;
        if (walletFilename != null) {
            try {
                walletFile = new File(walletFilename);
                wallet = Wallet.loadFromFile(walletFile);

                // set the new wallet into the model
                controller.getModel().setWallet(wallet);

                final MultiBitController finalController = controller;

                // wire up the controller as a wallet event listener
                controller.getModel().addWalletEventListener(new WalletEventListener() {
                    public void onCoinsReceived(Wallet wallet, Transaction transaction,
                            BigInteger prevBalance, BigInteger newBalance) {
                        finalController.onCoinsReceived(wallet, transaction, prevBalance,
                                newBalance);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (wallet == null || walletFilename == null) {
            // create new empty wallet
            walletFilename = filePrefix + WALLET_SUFFIX;
            walletFile = new File(walletFilename);
            wallet = new Wallet(networkParameters);

            // set the new wallet and wallet filename on the model
            controller.getModel().setWalletFilename(walletFilename);
            controller.getModel().setWallet(wallet);

            // wire up the controller as a wallet event listener
            final MultiBitController finalController = controller;
            controller.getModel().addWalletEventListener(new WalletEventListener() {
                public void onCoinsReceived(Wallet wallet, Transaction transaction,
                        BigInteger prevBalance, BigInteger newBalance) {
                    finalController.onCoinsReceived(wallet, transaction, prevBalance, newBalance);
                }
            });

            wallet.keychain.add(new ECKey());
            try {
                // TODO need to make sure dont overwrite an existing wallet
                wallet.saveToFile(walletFile);

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        // Fetch the first key in the wallet (should be the only key).
        ECKey key = wallet.keychain.get(0);

        System.out.println(wallet);

        // Load the block chain, if there is one stored locally.
        System.out.println("Reading block store from disk");
        BlockStore blockStore;
        try {
            blockStore = new BoundedOverheadBlockStore(networkParameters, new File(filePrefix
                    + ".blockchain"));

            // Connect to the localhost node. One minute timeout since we won't
            // try any other peers
            System.out.println("Connecting ...");
            chain = new BlockChain(networkParameters, wallet, blockStore);

            peerGroup = new MultiBitPeerGroup(controller, blockStore, networkParameters, chain);
            // peerGroup.addAddress(new PeerAddress(InetAddress.getLocalHost(),
            // 8333));
            peerGroup.addAddress(new PeerAddress(InetAddress.getByName("98.143.152.19"), 8333));

            // add the controller as a PeerEventListener
            peerGroup.addEventListener(controller);
            peerGroup.start();

            System.out.println("Send coins to: " + key.toAddress(networkParameters).toString());

            // The PeerGroup thread keeps us alive until something kills the
            // process.
        } catch (BlockStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * download the block chain
     */
    public void downloadBlockChain() {
        peerGroup.downloadBlockChain();
    }

    /**
     * send bitcoins
     * 
     * @param sendAddressString
     *            - the address to send to, as a String
     * @param amount
     *            - the amount to send to, in BTC, as a String
     */

    public Transaction sendCoins(String sendAddressString, String amount) throws java.io.IOException,
            AddressFormatException {
        // send the coins
        Address sendAddress = new Address(networkParameters, sendAddressString);
        Transaction sendTransaction = wallet.sendCoins(peerGroup, sendAddress, Utils.toNanoCoins(amount));
        assert sendTransaction != null; // We should never try to send more
                               // coins than we have!
        System.out.println("MultiBitService#sendCoins - Sent coins. Transaction hash is " + sendTransaction.getHashAsString());
        wallet.saveToFile(new File(controller.getModel().getWalletFilename()));
        
        return sendTransaction;
    }

    public String getWalletFilename() {
        return walletFilename;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public PeerGroup getPeerGroup() {
        return peerGroup;
    }

    public BlockChain getChain() {
        return chain;
    }
}
