package org.multibit.action;

import org.multibit.controller.MultiBitController;
import org.multibit.model.AddressBookData;
import org.multibit.model.Data;
import org.multibit.model.DataProvider;
import org.multibit.model.Item;
import org.multibit.model.MultiBitModel;

import com.google.bitcoin.core.ECKey;

/**
 * an action to process the submit of the CreateOrEditAddress view
 * 
 * @author jim
 * 
 */
public class CreateOrEditAddressSubmitAction implements Action {

    private MultiBitController controller;
    private boolean isCreate;
    private boolean isReceiving;

    public CreateOrEditAddressSubmitAction(MultiBitController controller, boolean isCreate, boolean isReceiving) {
        this.controller = controller;
        this.isCreate = isCreate;
        this.isReceiving = isReceiving;
    }

    public void execute(DataProvider dataProvider) {
        // get the receive address and label and put it in the user preferences
        // and address book
        if (dataProvider != null) {
            if (isReceiving) {
                String receiveAddress = null;
                String receiveLabel = null;

                Data data = dataProvider.getData();

                if (data != null) {
                    Item receiveAddressItem = data.getItem(MultiBitModel.RECEIVE_ADDRESS);
                    if (receiveAddressItem != null && receiveAddressItem.getNewValue() != null) {
                        receiveAddress = (String) receiveAddressItem.getNewValue();
                        controller.getModel().setUserPreference(MultiBitModel.RECEIVE_ADDRESS, receiveAddress);
                    }
                    Item receiveLabelItem = data.getItem(MultiBitModel.RECEIVE_LABEL);
                    if (receiveLabelItem != null && receiveLabelItem.getNewValue() != null) {
                        receiveLabel = (String) receiveLabelItem.getNewValue();
                        controller.getModel().setUserPreference(MultiBitModel.RECEIVE_LABEL, receiveLabel);
                    }
                }

                if (receiveAddress != null) {
                    if (receiveLabel == null) {
                        receiveLabel = "";
                    }
                    controller.getModel().getAddressBook()
                            .addReceivingAddress(new AddressBookData(receiveLabel, receiveAddress));
                }
                
                if (isCreate) {
                    Item receiveNewKeyItem = data.getItem(MultiBitModel.RECEIVE_NEW_KEY);
                    if (receiveNewKeyItem != null) {
                        ECKey newKey = (ECKey)receiveNewKeyItem.getNewValue();
                        controller.getModel().getWallet().keychain.add(newKey);
                    }
                }
            } else {
                String sendAddress = null;
                String sendLabel = null;

                Data data = dataProvider.getData();

                if (data != null) {
                    Item sendAddressItem = data.getItem(MultiBitModel.SEND_ADDRESS);
                    if (sendAddressItem != null && sendAddressItem.getNewValue() != null) {
                        sendAddress = (String) sendAddressItem.getNewValue();
                        controller.getModel().setUserPreference(MultiBitModel.SEND_ADDRESS, sendAddress);
                    }
                    Item sendLabelItem = data.getItem(MultiBitModel.SEND_LABEL);
                    if (sendLabelItem != null && sendLabelItem.getNewValue() != null) {
                        sendLabel = (String) sendLabelItem.getNewValue();
                        controller.getModel().setUserPreference(MultiBitModel.SEND_LABEL, sendLabel);
                    }
                }

                if (sendAddress != null) {
                    if (sendLabel == null) {
                        sendLabel = "";
                    }
                    controller.getModel().getAddressBook()
                            .addSendingAddress(new AddressBookData(sendLabel, sendAddress));
                }
                
            }
        }
        controller.setActionForwardToParent();
    }

    public String getDisplayText() {
        // would not normally be seen
        return "createOrEditAddressSubmit";
    }
}
