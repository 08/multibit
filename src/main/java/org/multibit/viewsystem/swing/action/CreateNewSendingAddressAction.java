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
package org.multibit.viewsystem.swing.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.multibit.controller.MultiBitController;
import org.multibit.model.AddressBookData;
import org.multibit.model.MultiBitModel;
import org.multibit.model.PerWalletModelData;
import org.multibit.model.WalletInfo;
import org.multibit.model.WalletVersion;
import org.multibit.viewsystem.swing.view.AbstractTradePanel;

/**
 * This {@link Action} represents an action to create a sending address.
 */
public class CreateNewSendingAddressAction extends MultiBitSubmitAction {

    private static final long serialVersionUID = 200111935465875405L;

    private MultiBitController controller;

    private AbstractTradePanel sendBitcoinPanel;

    /**
     * Creates a new {@link CreateNewSendingAddressAction}.
     */
    public CreateNewSendingAddressAction(MultiBitController controller, AbstractTradePanel sendBitcoinPanel) {
        super(controller, "createOrEditAddressAction.createReceiving.text", "createOrEditAddressAction.createSending.tooltip",
                "createOrEditAddressAction.createSending.mnemonicKey", null);
        this.sendBitcoinPanel = sendBitcoinPanel;
    }

    /**
     * Create new send address.
     */
    public void actionPerformed(ActionEvent e) {
        if (abort()) {
            return;
        }

        // Check to see if the wallet files have changed.
        PerWalletModelData perWalletModelData = controller.getModel().getActivePerWalletModelData();

        WalletInfo walletInfo = perWalletModelData.getWalletInfo();
        if (walletInfo == null) {
            walletInfo = new WalletInfo(perWalletModelData.getWalletFilename(), WalletVersion.PROTOBUF_ENCRYPTED);
            perWalletModelData.setWalletInfo(walletInfo);
        }

        if (walletInfo.getSendingAddresses().size() == 0) {
            String address = controller.getModel().getActiveWalletPreference(MultiBitModel.SEND_ADDRESS);
            String label = controller.getModel().getActiveWalletPreference(MultiBitModel.SEND_LABEL);

            perWalletModelData.getWalletInfo().addSendingAddress(new AddressBookData(label, address));
            sendBitcoinPanel.getAddressesTableModel().fireTableStructureChanged();
            controller.getModel().getActivePerWalletModelData().setDirty(true);
        } else {
            perWalletModelData.getWalletInfo().addSendingAddress(new AddressBookData("", ""));
            sendBitcoinPanel.getAddressesTableModel().fireTableDataChanged();
            sendBitcoinPanel.selectRows();

            controller.getModel().setActiveWalletPreference(MultiBitModel.SEND_ADDRESS, "");
            controller.getModel().setActiveWalletPreference(MultiBitModel.SEND_LABEL, "");
        }
        controller.displayView(controller.getCurrentView());

        if (sendBitcoinPanel != null && sendBitcoinPanel.getLabelTextArea() != null) {
            sendBitcoinPanel.getLabelTextArea().requestFocusInWindow();
        }
    }
}