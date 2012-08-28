package org.multibit.controller;

import org.multibit.model.WalletBusyListener;

public class TestWalletBusyListener implements WalletBusyListener {
    boolean walletBusy = false;

    @Override
    public void walletBusyChange(boolean newWalletIsBusy) {
        walletBusy = newWalletIsBusy;
    }

    boolean isWalletBusy() {
        return walletBusy;
    }
}
