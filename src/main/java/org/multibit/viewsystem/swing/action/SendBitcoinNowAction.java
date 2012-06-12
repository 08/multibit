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
package org.multibit.viewsystem.swing.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.math.BigInteger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

import org.multibit.controller.MultiBitController;
import org.multibit.message.Message;
import org.multibit.message.MessageManager;
import org.multibit.model.AddressBookData;
import org.multibit.model.MultiBitModel;
import org.multibit.model.PerWalletModelData;
import org.multibit.model.WalletInfo;
import org.multibit.viewsystem.swing.MultiBitFrame;
import org.multibit.viewsystem.swing.view.SendBitcoinConfirmDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.Utils;

/**
 * This {@link Action} forwards to the send bitcoin now action
 */
public class SendBitcoinNowAction extends AbstractAction {

    public Logger log = LoggerFactory.getLogger(SendBitcoinNowAction.class.getName());

    private static final long serialVersionUID = 1913592460523457765L;

    private MultiBitController controller;
    private SendBitcoinConfirmDialog sendBitcoinConfirmView;

    private final static int MAX_LENGTH_OF_ERROR_MESSAGE = 70;

    /**
     * Creates a new {@link SendBitcoinNowAction}.
     */
    public SendBitcoinNowAction(MultiBitFrame mainFrame, MultiBitController controller,
            SendBitcoinConfirmDialog sendBitcoinConfirmView, ImageIcon icon) {
        super(controller.getLocaliser().getString("sendBitcoinConfirmAction.text"), icon);
        this.controller = controller;
        this.sendBitcoinConfirmView = sendBitcoinConfirmView;

        MnemonicUtil mnemonicUtil = new MnemonicUtil(controller.getLocaliser());

        putValue(SHORT_DESCRIPTION, controller.getLocaliser().getString("sendBitcoinConfirmAction.tooltip"));
        putValue(MNEMONIC_KEY, mnemonicUtil.getMnemonic("sendBitcoinConfirmAction.mnemonicKey"));
    }

    /**
     * actually send the bitcoin
     */
    public void actionPerformed(ActionEvent event) {
        // check to see if the wallet files have changed
        PerWalletModelData perWalletModelData = controller.getModel().getActivePerWalletModelData();
        boolean haveFilesChanged = controller.getFileHandler().haveFilesChanged(perWalletModelData);

        if (haveFilesChanged) {
            // set on the perWalletModelData that files have changed and fire
            // data changed
            perWalletModelData.setFilesHaveBeenChangedByAnotherProcess(true);
            controller.fireFilesHaveBeenChangedByAnotherProcess(perWalletModelData);
        } else {
            // get the data out of the wallet preferences
            String sendAddress = controller.getModel().getActiveWalletPreference(MultiBitModel.SEND_ADDRESS);
            String sendLabel = controller.getModel().getActiveWalletPreference(MultiBitModel.SEND_LABEL);
            String sendAmount = controller.getModel().getActiveWalletPreference(MultiBitModel.SEND_AMOUNT);
            String sendFeeString = controller.getModel().getUserPreference(MultiBitModel.SEND_FEE);
            BigInteger fee;
            if (sendFeeString == null || sendFeeString.equals("")) {
                fee = MultiBitModel.SEND_FEE_DEFAULT;
            } else {
                fee = Utils.toNanoCoins(sendFeeString);
            }

            if (sendLabel != null && !sendLabel.equals("")) {
                WalletInfo addressBook = perWalletModelData.getWalletInfo();
                addressBook.addSendingAddress(new AddressBookData(sendLabel, sendAddress));
            }

            sendBitcoinConfirmView.setSendConfirmText(controller.getLocaliser().getString("sendBitcoinNowAction.sendingBitcoin"), " ");

            performSend(perWalletModelData, sendAddress, sendAmount, fee);
        }
    }

    /**
     * send the transaction directly
     */
    private void performSend(PerWalletModelData perWalletModelData, String sendAddress, String sendAmount, BigInteger fee) {
        String message = null;

        boolean sendWasSuccessful = Boolean.FALSE;
        try {
            log.debug("Sending from wallet " + perWalletModelData.getWalletFilename() + ", amount = " + sendAmount + ", fee = "
                    + fee + " to address = " + sendAddress);
            Transaction transaction = controller.getMultiBitService().sendCoins(perWalletModelData, sendAddress, sendAmount, fee);
            if (transaction == null) {
                // a null transaction returned indicates there was not
                // enough money (in spite of our validation)
                message = controller.getLocaliser().getString("sendBitcoinNowAction.thereWereInsufficientFundsForTheSend");
                log.error(message);
            } else {
                sendWasSuccessful = Boolean.TRUE;
                log.debug("Sent transaction was:\n" + transaction.toString());
            }

            // save the wallet
            controller.getFileHandler().savePerWalletModelData(perWalletModelData, false);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            message = e.getMessage();
        } catch (AddressFormatException e) {
            log.error(e.getMessage(), e);
            message = e.getMessage();
        } catch (Exception e) {
            // really trying to catch anything that goes wrong with the
            // send bitcoin
            log.error(e.getMessage(), e);
            message = e.getMessage();
        }

        if (sendWasSuccessful) {
            String successMessage = controller.getLocaliser().getString("sendBitcoinNowAction.bitcoinSentOk");
            if (sendBitcoinConfirmView != null && sendBitcoinConfirmView.isVisible()) {
                sendBitcoinConfirmView.setSendConfirmText(
                        controller.getLocaliser().getString("sendBitcoinNowAction.bitcoinSentOk"), "");
            } else {
                MessageManager.INSTANCE.addMessage(new Message(successMessage));
            }
        } else {
            log.error(message);

            if (message != null && message.length() > MAX_LENGTH_OF_ERROR_MESSAGE) {
                message = message.substring(0, MAX_LENGTH_OF_ERROR_MESSAGE) + "...";
            }

            String errorMessage = controller.getLocaliser().getString("sendBitcoinNowAction.bitcoinSendFailed");
            if (sendBitcoinConfirmView != null && sendBitcoinConfirmView.isVisible()) {
                sendBitcoinConfirmView.setSendConfirmText(errorMessage, message);
            } else {
                MessageManager.INSTANCE.addMessage(new Message(errorMessage + " " + message));
            }
        }

        log.debug("firing fireRecreateAllViews...");
        controller.fireRecreateAllViews(false);
        log.debug("firing fireRecreateAllViews...done");
    }

    /**
     * send the transaction in a background Swing worker thread
     */
    private void performSendInBackground(final PerWalletModelData perWalletModelData, final String sendAddress,
            final String sendAmount, final BigInteger fee) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            private String message = null;

            @Override
            protected Boolean doInBackground() throws Exception {
                boolean sendWasSuccessful = Boolean.FALSE;
                try {
                    log.debug("Sending from wallet " + perWalletModelData.getWalletFilename() + ", amount = " + sendAmount
                            + ", fee = " + fee + " to address = " + sendAddress);
                    Transaction transaction = controller.getMultiBitService().sendCoins(perWalletModelData, sendAddress,
                            sendAmount, fee);
                    if (transaction == null) {
                        // a null transaction returned indicates there was not
                        // enough money (in spite of our validation)
                        message = controller.getLocaliser().getString("sendBitcoinNowAction.thereWereInsufficientFundsForTheSend");
                        log.error(message);
                    } else {
                        sendWasSuccessful = Boolean.TRUE;
                        log.debug("Sent transaction was:\n" + transaction.toString());
                    }

                    // save the wallet
                    controller.getFileHandler().savePerWalletModelData(perWalletModelData, false);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    message = e.getMessage();
                } catch (AddressFormatException e) {
                    log.error(e.getMessage(), e);
                    message = e.getMessage();
                } catch (Exception e) {
                    // really trying to catch anything that goes wrong with the
                    // send bitcoin
                    log.error(e.getMessage(), e);
                    message = e.getMessage();
                }

                return sendWasSuccessful;
            }

            protected void done() {
                try {
                    Boolean sendWasSuccessful = get();
                    if (sendWasSuccessful) {
                        String successMessage = controller.getLocaliser().getString("sendBitcoinNowAction.bitcoinSentOk");
                        if (sendBitcoinConfirmView != null && sendBitcoinConfirmView.isVisible()) {
                            sendBitcoinConfirmView.setSendConfirmText(
                                    controller.getLocaliser().getString("sendBitcoinNowAction.bitcoinSentOk"), "");
                        } else {
                            MessageManager.INSTANCE.addMessage(new Message(successMessage));
                        }
                    } else {
                        log.error(message);

                        if (message != null && message.length() > MAX_LENGTH_OF_ERROR_MESSAGE) {
                            message = message.substring(0, MAX_LENGTH_OF_ERROR_MESSAGE) + "...";
                        }

                        String errorMessage = controller.getLocaliser().getString("sendBitcoinNowAction.bitcoinSendFailed");
                        if (sendBitcoinConfirmView != null && sendBitcoinConfirmView.isVisible()) {
                            sendBitcoinConfirmView.setSendConfirmText(errorMessage, message);
                        } else {
                            MessageManager.INSTANCE.addMessage(new Message(errorMessage + " " + message));
                        }
                    }
                } catch (Exception e) {
                    // not really used but caught so that SwingWorker shuts down
                    // cleanly
                    log.error(e.getClass() + " " + e.getMessage());
                }
                log.debug("firing fireRecreateAllViews...");
                controller.fireRecreateAllViews(false);
                log.debug("firing fireRecreateAllViews...done");
            }
        };
        log.debug("Sending coins in background SwingWorker thread");
        worker.execute();
    }
}