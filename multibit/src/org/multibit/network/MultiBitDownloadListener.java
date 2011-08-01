/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.multibit.network;

import java.text.DateFormat;
import java.util.Date;

import org.multibit.controller.MultiBitController;

import com.google.bitcoin.core.DownloadListener;

/**
 * Listen to chain download events and print useful informational messages.
 * 
 * <p>
 * progress, startDownload, doneDownload maybe be overridden to change the way
 * the user is notified.
 * 
 * <p>
 * Methods are called with the event listener object locked so your
 * implementation does not have to be thread safe.
 * 
 * @author miron@google.com (Miron Cuperman a.k.a. devrandom)
 * 
 */
public class MultiBitDownloadListener extends DownloadListener {
    private static final double DONE_FOR_DOUBLES = 99.99;  // not quite 100 per cent to cater for rounding
    private static final int CRITERIA_LARGE_NUMBER_OF_BLOCKS = 1000;

    MultiBitController controller;

    public MultiBitDownloadListener(MultiBitController controller) {
        this.controller = controller;
    }

    /**
     * Called when download progress is made.
     * 
     * @param pct
     *            the percentage of chain downloaded, estimated
     * @param date
     *            the date of the last block downloaded
     */
    protected void progress(double pct, Date date) {
        if (pct > DONE_FOR_DOUBLES) {
            // we are done downloading
            doneDownload();
        } else {
            String downloadStatusText = controller.getLocaliser().getString(
                    "multiBitDownloadListener.progressText",
                    new Object[] {
                            (int) pct,
                            DateFormat.getDateInstance(DateFormat.MEDIUM,
                                    controller.getLocaliser().getLocale()).format(date) });
            controller.updateDownloadStatus(downloadStatusText);
        }
    }

    /**
     * Called when download is initiated.
     * 
     * @param blocks
     *            the number of blocks to download, estimated
     */
    protected void startDownload(int blocks) {
        if (blocks == 0) {
            doneDownload();
        } else {
            String startDownloadText;
            if (blocks <= CRITERIA_LARGE_NUMBER_OF_BLOCKS) {
                startDownloadText = controller.getLocaliser().getString(
                        "multiBitDownloadListener.startDownloadTextShort", new Object[] { blocks });
            } else {
                startDownloadText = controller.getLocaliser().getString(
                        "multiBitDownloadListener.startDownloadTextLong", new Object[] { blocks });
            }
            controller.updateDownloadStatus(startDownloadText);
        }
    }

    /**
     * Called when we are done downloading the block chain.
     */
    protected void doneDownload() {
        String downloadStatusText = controller.getLocaliser().getString(
                "multiBitDownloadListener.doneDownloadText");
        controller.updateDownloadStatus(downloadStatusText);
    }
}