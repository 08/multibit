package org.multibit.viewsystem.swing.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.multibit.ActionForward;
import org.multibit.MultiBitController;
import org.multibit.viewsystem.Localiser;

/**
 * This {@link Action} shows the MultiBit help
 */
public class ShowHelpContentsAction extends AbstractAction {

    private static final long serialVersionUID = 1923492460523457765L;

    private MultiBitController controller;
    
    /**
     * Creates a new {@link ShowHelpContentsAction}.
     */
    public ShowHelpContentsAction(MultiBitController controller, Localiser localiser, ImageIcon icon) {
        super(localiser.getString("showHelpContentsAction.text"), icon);
        this.controller = controller;
        putValue(SHORT_DESCRIPTION, localiser.getString("showHelpContentsAction.tooltip"));
        putValue(MNEMONIC_KEY, localiser.getMnemonic("showHelpContentsAction.mnemonicKey"));
    }

    /**
     * forward to the help contents view
     */
    public void actionPerformed(ActionEvent e) { 
        controller.setActionForward(ActionForward.FORWARD_TO_HELP_CONTENTS);
     }
}