/**
 * Copyright 2011 multibit.org
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

import java.util.Date;
import java.util.Set;

import javax.swing.SwingWorker;

import org.multibit.controller.ActionForward;
import org.multibit.controller.MultiBitController;
import org.multibit.model.DataProvider;
import org.multibit.model.MultiBitModel;
import org.multibit.model.PerWalletModelData;

import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.store.BlockStoreException;

/**
 * an action to process the submit of the Reset Transactions view
 * 
 * @author jim
 * 
 */
public class ResetTransactionsSubmitAction implements Action {
    private MultiBitController controller;

    public ResetTransactionsSubmitAction(MultiBitController controller) {
        this.controller = controller;
    }

    public void execute(DataProvider dataProvider) {
        // check to see if another process has changed the active wallet
        PerWalletModelData perWalletModelData = controller.getModel().getActivePerWalletModelData();
        boolean haveFilesChanged = controller.getFileHandler().haveFilesChanged(perWalletModelData);

        if (haveFilesChanged) {
            // set on the perWalletModelData that files have changed and fire
            // data changed
            perWalletModelData.setFilesHaveBeenChangedByAnotherProcess(true);
            controller.fireFilesHaveBeenChangedByAnotherProcess(perWalletModelData);
        } else {
            // work out the earliest transaction date and save it to the wallet
            PerWalletModelData activePerWalletModelData = controller.getModel().getActivePerWalletModelData();

            Date earliestTransactionDate = new Date();
            boolean alreadyHaveEarliestTransactionDate = false;
            String storedEarliestTransactionDate = activePerWalletModelData.getWalletInfo().getProperty(
                    MultiBitModel.EARLIEST_TRANSACTION_DATE);
            if (storedEarliestTransactionDate != null && !"".equals(storedEarliestTransactionDate)) {
                // already have this date computed
                try {
                    earliestTransactionDate = new Date(Long.parseLong(storedEarliestTransactionDate));
                    alreadyHaveEarliestTransactionDate = true;
                } catch (NumberFormatException nfe) {
                    // carry on - will work it out from scratch
                }
            }

            // if do not have earliest date stored for wallet - work it out
            if (!alreadyHaveEarliestTransactionDate) {
                Set<Transaction> allTransactions = activePerWalletModelData.getWallet().getTransactions(true, true);
                if (allTransactions != null) {
                    for (Transaction transaction : allTransactions) {
                        if (transaction != null) {
                            Date updateTime = transaction.getUpdateTime();
                            if (updateTime != null && earliestTransactionDate.after(updateTime)) {
                                earliestTransactionDate = updateTime;
                            }
                            Date updateDate = transaction.getUpdatedAt();
                            if (updateDate != null && earliestTransactionDate.after(updateDate)) {
                                earliestTransactionDate = updateDate;
                            }
                        }
                    }
                }
                activePerWalletModelData.getWalletInfo().put(MultiBitModel.EARLIEST_TRANSACTION_DATE,
                        "" + earliestTransactionDate.getTime());
            }

            // remove the transactions from the wallet
            activePerWalletModelData.getWallet().removeAllTransactions();

            // save the wallet without the transactions
            controller.getFileHandler().savePerWalletModelData(perWalletModelData, true);

            // start worker thread to redownload the block chain
            final Date finalEarliestTransactionDate = earliestTransactionDate;
            @SuppressWarnings("rawtypes")
            SwingWorker worker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    try {
                        controller.getMultiBitService().replayBlockChain(finalEarliestTransactionDate);
                    } catch (BlockStoreException e) {
                        e.printStackTrace();
                    }
                    return null; // return not used
                }
            };
            worker.execute();
//            Thread workerThread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        controller.getMultiBitService().replayBlockChain(finalEarliestTransactionDate);
//                    } catch (BlockStoreException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//            workerThread.start();

            controller.setActionForwardToSibling(ActionForward.FORWARD_TO_SAME);
        }
    }
}
