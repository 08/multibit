package org.multibit.viewsystem.swing.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.multibit.Localiser;
import org.multibit.controller.MultiBitController;
import org.multibit.viewsystem.swing.MultiBitFrame;

/**
 * This {@link Action} represents the swing paste address action
 */
public class PasteAddressAction extends AbstractAction {

    private static final long serialVersionUID = 114352235465057705L;

    private MultiBitController controller;

    /**
     * Creates a new {@link PasteAddressAction}.
     */
    public PasteAddressAction(MultiBitController controller) {
        super(controller.getLocaliser().getString("pasteAddressAction.text"));
        this.controller = controller;
        
        putValue(SHORT_DESCRIPTION, controller.getLocaliser().getString("pasteAddressAction.tooltip"));
        putValue(MNEMONIC_KEY, controller.getLocaliser().getMnemonic("pasteAddressAction.mnemonicKey"));
    }

    /**
     * delegate to generic paste address action
     */
    public void actionPerformed(ActionEvent e) {
        org.multibit.action.PasteAddressAction genericPasteAddressAction = new org.multibit.action.PasteAddressAction(controller);
        genericPasteAddressAction.execute(null);
    }
}