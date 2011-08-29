package org.multibit.viewsystem.swing.view;

import java.util.Collection;

import javax.swing.JFrame;

import org.multibit.Localiser;
import org.multibit.action.Action;
import org.multibit.controller.MultiBitController;
import org.multibit.viewsystem.View;
import org.multibit.viewsystem.ViewSystem;

/**
 * The create new receiving address view
 */
public class ObseleteCreateOrEditAddressView implements View {

    private static final long serialVersionUID = 191435655543457705L;

    private JFrame mainFrame;

    private MultiBitController controller;

    private Localiser localiser;

    private boolean isCreate;
    private boolean isReceiving;

    private ObseleteCreateOrEditAddressDialog createOrEditAddressDialog;

    /**
     * Creates a new {@link ObseleteCreateOrEditAddressView}.
     */
    public ObseleteCreateOrEditAddressView(MultiBitController controller, Localiser localiser,
            JFrame mainFrame, boolean isCreate, boolean isReceiving) {
        this.controller = controller;
        this.localiser = localiser;
        this.mainFrame = mainFrame;
        this.isCreate = isCreate;
        this.isReceiving = isReceiving;
    }

    public String getDescription() {
        if (isCreate) {
            if (isReceiving) {
                return localiser.getString("createNewReceivingAddressAction.tooltip");
            } else {
                return localiser.getString("createNewSendingAddressAction.tooltip");
            }
        } else {
            if (isReceiving) {
                return localiser.getString("editReceivingAddressAction.tooltip");
            } else {
                return localiser.getString("editSendingAddressAction.tooltip");
            }
        }
    }

    /**
     * show the CreateOrEditAddressDialog
     */
    public void displayView() {
        if (createOrEditAddressDialog == null) {
            createOrEditAddressDialog = new ObseleteCreateOrEditAddressDialog(mainFrame, controller,
                    isCreate, isReceiving);
        }
        createOrEditAddressDialog.loadForm();
        createOrEditAddressDialog.setVisible(true);

        // the action listeners of the code in the dialog do all the action
        // forwarding so nothing to do here
    }

    public void displayMessage(String messageKey, Object[] messageData, String titleKey) {
        // not implemented on this view
    }

    public void navigateAwayFromView(int nextViewId, int relationshipOfNewViewToPrevious) {
        if (ViewSystem.NEW_VIEW_IS_PARENT_OF_PREVIOUS == relationshipOfNewViewToPrevious) {
            if (createOrEditAddressDialog != null) {
                createOrEditAddressDialog.setVisible(false);
                createOrEditAddressDialog.dispose();
                createOrEditAddressDialog = null;
            }
        }
    }

    public void setPossibleActions(Collection<Action> possibleActions) {
        // not required in swing view
    }
}