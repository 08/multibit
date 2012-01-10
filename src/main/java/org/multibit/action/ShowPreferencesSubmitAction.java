package org.multibit.action;

import java.math.BigInteger;

import org.multibit.controller.ActionForward;
import org.multibit.controller.MultiBitController;
import org.multibit.model.Data;
import org.multibit.model.DataProvider;
import org.multibit.model.Item;
import org.multibit.model.MultiBitModel;

import com.google.bitcoin.core.Utils;

/**
 * an action to process the submit of the Preferences view
 * 
 * @author jim
 * 
 */
public class ShowPreferencesSubmitAction implements Action {
    private MultiBitController controller;

    public ShowPreferencesSubmitAction(MultiBitController controller) {
        this.controller = controller;
    }

    public void execute(DataProvider dataProvider) {
        boolean feeValidationError = false;
        String updateStatusText = "";

        if (dataProvider != null) {
            Data data = dataProvider.getData();

            if (data != null) {
                Item feeItem = data.getItem(MultiBitModel.SEND_FEE);
                if (feeItem != null) {
                    // check fee is set
                    if (feeItem.getNewValue() == null || "".equals(feeItem.getNewValue())) {
                        // fee must be set validation error
                        controller.getModel().setUserPreference(MultiBitModel.SEND_FEE, (String) feeItem.getOriginalValue());
                        feeValidationError = true;
                        updateStatusText = controller.getLocaliser().getString("showPreferencesPanel.aFeeMustBeSet");
                    }

                    if (!feeValidationError) {
                        try {
                            // check fee is a number
                            BigInteger feeAsBigInteger = Utils.toNanoCoins((String) feeItem.getNewValue());

                            // check fee is at least the minimum fee
                            if (feeAsBigInteger.compareTo(MultiBitModel.SEND_MINIMUM_FEE) < 0) {
                                feeValidationError = true;
                                updateStatusText = controller.getLocaliser().getString(
                                        "showPreferencesPanel.feeCannotBeSmallerThanMinimumFee");
                            } else {
                                // fee is ok
                                controller.getModel().setUserPreference(MultiBitModel.SEND_FEE, (String) feeItem.getNewValue());
                            }
                        } catch (NumberFormatException nfe) {
                            // recycle the old fee and set status message
                            controller.getModel()
                                    .setUserPreference(MultiBitModel.SEND_FEE, (String) feeItem.getOriginalValue());
                            feeValidationError = true;
                            updateStatusText = controller.getLocaliser().getString(
                                    "showPreferencesPanel.couldNotUnderstandFee", new Object[] { feeItem.getNewValue() });
                        } catch (ArithmeticException ae) {
                            // recycle the old fee and set status message
                            controller.getModel()
                                    .setUserPreference(MultiBitModel.SEND_FEE, (String) feeItem.getOriginalValue());
                            feeValidationError = true;
                            updateStatusText = controller.getLocaliser().getString(
                                    "showPreferencesPanel.couldNotUnderstandFee", new Object[] { feeItem.getNewValue() });
                        }
                    }
                }

                Item languageItem = data.getItem(MultiBitModel.USER_LANGUAGE_CODE);
                if (languageItem != null && languageItem.getNewValue() != null
                        && !languageItem.getNewValue().equals(languageItem.getOriginalValue())) {
                    // new language to set on model
                    controller.getModel().setUserPreference(MultiBitModel.USER_LANGUAGE_CODE,
                            (String) languageItem.getNewValue());
                    controller.fireLanguageChanged();
                }

                Item openURIDialogItem = data.getItem(MultiBitModel.OPEN_URI_SHOW_DIALOG);
                if (openURIDialogItem != null && openURIDialogItem.getNewValue() != null) {
                    controller.getModel().setUserPreference(MultiBitModel.OPEN_URI_SHOW_DIALOG,
                            (String) openURIDialogItem.getNewValue());
                }

                Item openURIUseUriItem = data.getItem(MultiBitModel.OPEN_URI_USE_URI);
                if (openURIUseUriItem != null && openURIUseUriItem.getNewValue() != null) {
                    controller.getModel().setUserPreference(MultiBitModel.OPEN_URI_USE_URI,
                            (String) openURIUseUriItem.getNewValue());
                }
                
                // font data
                boolean fontHasChanged = false;
                Item fontNameItem = data.getItem(MultiBitModel.FONT_NAME);
                if (fontNameItem != null && fontNameItem.getNewValue() != null) {
                    controller.getModel().setUserPreference(MultiBitModel.FONT_NAME,
                            (String) fontNameItem.getNewValue());
                    
                    if (!fontNameItem.getNewValue().equals(fontNameItem.getOriginalValue())) {
                        fontHasChanged = true;
                    }
                }

                Item fontStyleItem = data.getItem(MultiBitModel.FONT_STYLE);
                if (fontStyleItem != null && fontStyleItem.getNewValue() != null) {
                    controller.getModel().setUserPreference(MultiBitModel.FONT_STYLE,
                            (String) fontStyleItem.getNewValue());
                    
                    if (!fontStyleItem.getNewValue().equals(fontStyleItem.getOriginalValue())) {
                        fontHasChanged = true;
                    }
                }

                Item fontSizeItem = data.getItem(MultiBitModel.FONT_SIZE);
                if (fontSizeItem != null && fontSizeItem.getNewValue() != null) {
                    controller.getModel().setUserPreference(MultiBitModel.FONT_SIZE,
                            (String) fontSizeItem.getNewValue());
                    
                    if (!fontSizeItem.getNewValue().equals(fontSizeItem.getOriginalValue())) {
                        fontHasChanged = true;
                    }
                }
                
                if (fontHasChanged) {
                    // redo everything
                    controller.fireLanguageChanged();
                }
            }
        }

        // return to parent view
        controller.setActionForwardToSibling(ActionForward.FORWARD_TO_SAME);

        if (feeValidationError) {
            controller.updateDownloadStatus(updateStatusText);
        }
    }
}
