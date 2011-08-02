package org.multibit.action;

import org.multibit.controller.ActionForward;
import org.multibit.controller.MultiBitController;
import org.multibit.model.Data;
import org.multibit.model.DataProvider;
import org.multibit.model.Item;
import org.multibit.model.MultiBitModel;

/**
 * an action to copy the address in the supplied formbean to the system clipboard
 * @author jim
 *
 */
public class CopyAddressAction implements Action {
    private MultiBitController controller;
    
    public CopyAddressAction(MultiBitController controller) {
        this.controller = controller;    
    }
    
    public void execute(DataProvider dataProvider) {
        if (dataProvider != null) {
            Data data = dataProvider.getData();
            
            if (data != null)  {
                Item item = data.getItem(MultiBitModel.RECEIVE_ADDRESS);
                if (item != null && item.getNewValue() != null) {
                    
                    // copy to clipboard
                    TextTransfer textTransfer = new TextTransfer();
                    textTransfer.setClipboardContents((String)item.getNewValue());            
                }
            }
        }
        // forward back to the same view
        controller.setActionForwardToSibling(ActionForward.FORWARD_TO_SAME);       
    }
    
    public String getDisplayText() {
        // TODO localise
        return "copyAddress";
    }
}


