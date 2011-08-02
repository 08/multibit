package org.multibit.viewsystem.swing.view;

import java.util.Collection;

import org.multibit.Localiser;
import org.multibit.action.Action;
import org.multibit.controller.MultiBitController;
import org.multibit.viewsystem.View;
import org.multibit.viewsystem.ViewSystem;
import org.multibit.viewsystem.swing.MultiBitFrame;

/**
 * The receive bitcoin view
 */
public class ReceiveBitcoinView implements View {

    private static final long serialVersionUID = 191435612343457705L;

    private MultiBitFrame mainFrame;

    private MultiBitController controller;

    private Localiser localiser;

    private ReceiveBitcoinDialog receiveBitcoinDialog;

    /**
     * Creates a new {@link ReceiveBitcoinView}.
     */
    public ReceiveBitcoinView(MultiBitController controller, Localiser localiser,
            MultiBitFrame mainFrame) {
        this.controller = controller;
        this.localiser = localiser;
        this.mainFrame = mainFrame;
    }

    public String getDescription() {
        return localiser.getString("receiveBitcoinsDialog.title");
    }

    /**
     * show receive bitcoins dialog
     */
    public void displayView() {
        if (receiveBitcoinDialog == null) {
            receiveBitcoinDialog = new ReceiveBitcoinDialog(mainFrame, controller);
        }
        receiveBitcoinDialog.loadForm();
        receiveBitcoinDialog.setVisible(true);

        // the action listeners of the code in the dialog do all the action
        // forwarding so nothing to do here
    }

    public void displayMessage(String messageKey, Object[] messageData, String titleKey) {
        // not implemented on this view
    }

    public void navigateAwayFromView(int nextViewId, int relationshipOfNewViewToPrevious) {
        if (ViewSystem.newViewIsParentOfPrevious == relationshipOfNewViewToPrevious) {
            if (receiveBitcoinDialog != null) {
                receiveBitcoinDialog.setVisible(false);
                receiveBitcoinDialog.dispose();
                receiveBitcoinDialog = null;
            }
        }
    }

    public void setPossibleActions(Collection<Action> possibleActions) {
        // not required in swing view
    }
}