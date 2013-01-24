package org.multibit.viewsystem.swing;

import java.math.BigInteger;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import org.multibit.controller.MultiBitController;
import org.multibit.viewsystem.View;
import org.multibit.viewsystem.swing.view.panels.ShowTransactionsPanel;

public class UpdateTransactionsTimerTask extends TimerTask {
    private MultiBitController controller;
    private ShowTransactionsPanel transactionsPanel;
    private MultiBitFrame mainFrame;

    private Boolean updateTransactions = Boolean.FALSE;

    public UpdateTransactionsTimerTask(MultiBitController controller, final ShowTransactionsPanel transactionsPanel,
            MultiBitFrame mainFrame) {
        this.controller = controller;
        this.transactionsPanel = transactionsPanel;
        this.mainFrame = mainFrame;
    }

    @Override
    public void run() {
        final BigInteger finalEstimatedBalance = controller.getModel().getActiveWalletEstimatedBalance();
        final BigInteger finalAvailableToSpend = controller.getModel().getActiveWalletAvailableBalanceWithBoomerangChange();
        final boolean filesHaveBeenChangeByAnotherProcess = controller.getModel().getActivePerWalletModelData() != null && controller.getModel().getActivePerWalletModelData().isFilesHaveBeenChangedByAnotherProcess();
 
        // If viewing transactions, refresh the screen so that transaction
        // confidence icons can update.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                boolean updateThisTime = false;
                if (updateTransactions) {
                    updateTransactions = false;
                    updateThisTime = true;
                }

                if (updateThisTime) {
                    mainFrame.updateHeaderOnSwingThread(filesHaveBeenChangeByAnotherProcess, finalEstimatedBalance, finalAvailableToSpend);
                    if (controller.getCurrentView() == View.TRANSACTIONS_VIEW) {
                        // log.debug("Updating transaction view");
                        transactionsPanel.displayView();
                    }
                }
            }
        });
    }

    public boolean isUpdateTransactions() {
        return updateTransactions;
    }

    public void setUpdateTransactions(boolean updateTransactions) {
        this.updateTransactions = updateTransactions;
    }
}
