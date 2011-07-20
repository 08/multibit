package org.multibit.viewsystem.swing.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.multibit.ActionForward;
import org.multibit.MultiBitController;
import org.multibit.viewsystem.Localiser;
import org.multibit.viewsystem.swing.MultiBitFrame;
import org.multibit.viewsystem.swing.ReceiveBitcoinsDialog;

/**
 * This {@link Action} sends bitcoin
 */
public class ReceiveBitcoinAction extends AbstractAction {

    private static final long serialVersionUID = 1913592460523457765L;

    private MultiBitFrame mainFrame;
    private MultiBitController controller;
    private Localiser localiser;
    
    /**
     * Creates a new {@link ReceiveBitcoinAction}.
     */
    public ReceiveBitcoinAction(MultiBitController controller, 
            Localiser localiser, ImageIcon icon, MultiBitFrame mainFrame) {
        super(localiser.getString("receiveBitcoinAction.text"), icon);
        this.controller = controller;
        this.localiser = localiser;
        putValue(SHORT_DESCRIPTION, localiser.getString("receiveBitcoinAction.tooltip"));
        putValue(MNEMONIC_KEY, localiser.getMnemonic("receiveBitcoinAction.mnemonicKey"));
        
        this.mainFrame = mainFrame;
    }

    /**
     * receive bitcoins
     */
    public void actionPerformed(ActionEvent e) {  
        controller.setActionForward(ActionForward.FORWARD_TO_RECEIVE_BITCOINS);
        
        ReceiveBitcoinsDialog receiveBitcoinsDialog = new ReceiveBitcoinsDialog(mainFrame, localiser);       
        receiveBitcoinsDialog.setVisible(true);      
    }
}