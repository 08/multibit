/**
 * Copyright 2012 multibit.org
 *
 * Licensed under the MIT license (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://opensource.org/licenses/mit-license.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.multibit.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.swing.SwingWorker;

import org.multibit.controller.ActionForward;
import org.multibit.controller.MultiBitController;
import org.multibit.file.PrivateKeyAndDate;
import org.multibit.file.PrivateKeysHandler;
import org.multibit.model.Data;
import org.multibit.model.DataProvider;
import org.multibit.model.Item;
import org.multibit.model.MultiBitModel;
import org.multibit.model.PerWalletModelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.store.BlockStoreException;

/**
 * an action to process the submit of the Import Private Keys submit action
 * 
 * @author jim
 * 
 */
public class ImportPrivateKeysSubmitAction implements Action {
    private static final Logger log = LoggerFactory.getLogger(ImportPrivateKeysSubmitAction.class);

    private MultiBitController controller;

    public ImportPrivateKeysSubmitAction(MultiBitController controller) {
        this.controller = controller;
    }

    public void execute(DataProvider dataProvider) {
        PerWalletModelData perWalletModelData = controller.getModel().getActivePerWalletModelData();

        String message = controller.getLocaliser().getString("showImportPrivateKeysAction.noDataWasImported");

        // get the required output file
        if (dataProvider != null) {
            Data data = dataProvider.getData();

            if (data != null) {
                Item privateKeysOutputFilenameItem = data.getItem(MultiBitModel.PRIVATE_KEY_FILENAME);
                if (privateKeysOutputFilenameItem != null) {
                    String importFilename = (String) privateKeysOutputFilenameItem.getNewValue();

                    if (importFilename == null || importFilename.equals("")) {
                        // no import file - nothing to do
                        message = controller.getLocaliser().getString("showImportPrivateKeysAction.privateKeysNothingToDo");
                    } else {
                        try {
                            PrivateKeysHandler privateKeysHandler = new PrivateKeysHandler(controller.getMultiBitService().getNetworkParameters());

                            ArrayList<PrivateKeyAndDate> privateKeyAndDateArray = privateKeysHandler.importPrivateKeys(new File(
                                    importFilename));

                            // add to wallet and keep track of earliest
                            // transaction date
                            // go backwards from now
                            Wallet walletToAddKeysTo = perWalletModelData.getWallet();
                            Date earliestTransactionDate = new Date();
                            if (privateKeyAndDateArray != null) {
                                for (PrivateKeyAndDate privateKeyAndDate : privateKeyAndDateArray) {
                                    ECKey keyToAdd = privateKeyAndDate.getKey();
                                    if (keyToAdd != null) {
                                        if (walletToAddKeysTo != null && !keyChainContainsPrivateKey(walletToAddKeysTo.getKeychain(), keyToAdd)) {
                                            walletToAddKeysTo.addKey(keyToAdd);

                                            // update earliest transaction date
                                            if (privateKeyAndDate.getDate() == null) {
                                                // need to go back to the
                                                // genesis block
                                                earliestTransactionDate = null;
                                            } else {
                                                if (earliestTransactionDate != null) {
                                                    earliestTransactionDate = earliestTransactionDate.before(privateKeyAndDate
                                                            .getDate()) ? earliestTransactionDate : privateKeyAndDate.getDate();
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                            controller.getFileHandler().savePerWalletModelData(perWalletModelData, false);
                            controller.getModel().createAddressBookReceivingAddresses(perWalletModelData.getWalletFilename());

                            // begin blockchain replay
                            try {
                                controller.getMultiBitService().replayBlockChain(earliestTransactionDate);
                            } catch (BlockStoreException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            message = controller.getLocaliser().getString("showImportPrivateKeysAction.privateKeysImportSuccess");
                        } catch (IOException e) {
                            log.error(e.getClass().getName() + " " + e.getMessage());
                            message = controller.getLocaliser().getString("showImportPrivateKeysAction.privateKeysImportFailure",
                                    new Object[] { e.getClass().getName() + " " + e.getMessage() });
                        }
                    }
                }
            }
        }

        controller.getModel().setUserPreference(MultiBitModel.DISPLAY_IMPORT_PRIVATE_KEYS_MESSAGE, "true");
        controller.getModel().setUserPreference(MultiBitModel.IMPORT_PRIVATE_KEYS_MESSAGE, message);
        controller.setActionForwardToSibling(ActionForward.FORWARD_TO_SAME);
    }
    
    /**
     * this method is here because there is no equals on ECKey
     */
    private boolean keyChainContainsPrivateKey(ArrayList<ECKey> keyChain, ECKey keyToAdd) {
        if (keyChain == null || keyToAdd == null) {
            return false;
        } else {
            for (ECKey loopKey : keyChain) {
                if (Arrays.equals(keyToAdd.getPrivKeyBytes(), loopKey.getPrivKeyBytes())) {
                    return true;
                }
            }
            return false;
        }
    }
}
