/**
 * Copyright 2012 multibit.org
 *
 * Licensed under the MIT license (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://opensource.org/licenses/mit-license.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.multibit.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.multibit.ApplicationDataDirectoryLocator;
import org.multibit.controller.MultiBitController;
import org.multibit.model.MultiBitModel;
import org.multibit.model.PerWalletModelData;
import org.multibit.model.WalletInfo;
import org.multibit.model.WalletVersion;
import org.multibit.network.MultiBitService;
import org.multibit.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.Wallet;

/**
 * Class consolidating the File IO in MultiBit for wallets and wallet infos.
 * 
 * @author jim
 * 
 */
public class FileHandler {

    private static Logger log = LoggerFactory.getLogger(FileHandler.class);

    public static final String USER_PROPERTIES_FILE_NAME = "multibit.properties";
    public static final String USER_PROPERTIES_HEADER_TEXT = "multibit";

    private static final String SEPARATOR = "-";
    public static final String BACKUP_SUFFIX_FORMAT = "yyyyMMddHHmmss";

    private MultiBitController controller;

    private Date dateForBackupName = null;

    public FileHandler(MultiBitController controller) {
        this.controller = controller;
    }

    public PerWalletModelData loadFromFile(File walletFile) throws WalletLoadException {
        if (walletFile == null) {
            return null;
        }

        String walletFilename = walletFile.getAbsolutePath();
        
        try {
            // See if the wallet is serialized or protobuf.
            WalletInfo walletInfo;
            if (isWalletSerialised(walletFile)) {
                walletInfo = new WalletInfo(walletFilename, WalletVersion.SERIALIZED);
            } else {
                walletInfo = new WalletInfo(walletFilename, WalletVersion.PROTOBUF_ENCRYPTED);
            }

            Wallet wallet = Wallet.loadFromFile(walletFile);

            // Add the new wallet into the model.
            PerWalletModelData perWalletModelData = controller.getModel().addWallet(wallet, walletFilename);

            perWalletModelData.setWalletInfo(walletInfo);

            synchronized (walletInfo) {
                rememberFileSizesAndLastModified(walletFile, walletInfo);
                perWalletModelData.setDirty(false);
            }

            return perWalletModelData;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getClass().getCanonicalName() + " "  + e.getMessage());
            throw new WalletLoadException(e.getClass().getCanonicalName() + " "  + e.getMessage(), e);
        } 
    }
    
    boolean isWalletSerialised(File walletFile) {
        boolean isWalletSerialised = false;
        InputStream stream = null;
        try {
            // Determine what kind of wallet stream this is: Java Serialization or protobuf format.
            stream = new BufferedInputStream(new FileInputStream(walletFile));
            isWalletSerialised = stream.read() == 0xac && stream.read() == 0xed;
        } catch (FileNotFoundException e) {
            log.error(e.getClass().getCanonicalName() + " " + e.getMessage());
        } catch (IOException e) {
            log.error(e.getClass().getCanonicalName() + " " + e.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    log.error(e.getClass().getCanonicalName() + " " + e.getMessage());
                } 
            }
        }
        return isWalletSerialised;
    }

    /**
     * Save the perWalletModelData to file.
     * 
     * @param perWalletModelData
     *            TODO give notification of whether data was written to a backup
     *            file
     * @param forceWrite
     *            force the write of the perWalletModelData
     */
    public void savePerWalletModelData(PerWalletModelData perWalletModelData, boolean forceWrite) {
        // log.info("Wallet details for wallet file = " +
        // perWalletModelData.getWalletFilename() + "\n"
        // + perWalletModelData.getWallet().toString());
 
        if (perWalletModelData == null || perWalletModelData.getWalletFilename() == null) {
            return;
        }
        
        File walletFile = new File(perWalletModelData.getWalletFilename());
        
        WalletInfo walletInfo = perWalletModelData.getWalletInfo();
               
        synchronized (walletInfo) {
            // Save the perWalletModelData if it is dirty or if forceWrite is true.
            if (perWalletModelData.isDirty() || forceWrite) {
                // Check dates and sizes of files.
                boolean filesHaveChanged = haveFilesChanged(perWalletModelData);
                
                if (!filesHaveChanged || forceWrite) {
                    // Normal write of data.
                    String walletInfoFilename = WalletInfo.createWalletInfoFilename(perWalletModelData.getWalletFilename());
                    saveWalletAndWalletInfo(perWalletModelData, perWalletModelData.getWalletFilename(), walletInfoFilename);

                    rememberFileSizesAndLastModified(walletFile, walletInfo);

                    // The perWalletModelData is no longer dirty.
                    perWalletModelData.setDirty(false);
                } else {
                    // Write to backup files.
                    backupPerWalletModelData(perWalletModelData);
                }
            }
        }
        return;
    }

    public void backupPerWalletModelData(PerWalletModelData perWalletModelData) {
        // Write to backup files.
        // Work out / reuse the backup file names.
        String walletInfoBackupFilename = null;
        String walletBackupFilename = null;

        try {
            if (perWalletModelData.getWalletBackupFilename() != null) {
                walletBackupFilename = perWalletModelData.getWalletBackupFilename();
            }
            if (perWalletModelData.getWalletInfoBackupFilename() != null) {
                walletInfoBackupFilename = perWalletModelData.getWalletInfoBackupFilename();
            }

            if (walletBackupFilename == null) {
                walletBackupFilename = createBackupFilename(new File(perWalletModelData.getWalletFilename()), false);
                perWalletModelData.setWalletBackupFilename(walletBackupFilename);

                walletInfoBackupFilename = createBackupFilename(
                        new File(WalletInfo.createWalletInfoFilename(perWalletModelData.getWalletFilename())), true);
                perWalletModelData.setWalletInfoBackupFilename(walletInfoBackupFilename);
            }
            
            saveWalletAndWalletInfo(perWalletModelData, walletBackupFilename, walletInfoBackupFilename);

            // The perWalletModelData is no longer dirty.
            perWalletModelData.setDirty(false);
            
            log.info("Written backup wallet files to '" + walletBackupFilename + "'");
        } catch (IOException ioe) {
            log.error(ioe.getClass().getCanonicalName() + " "  + ioe.getMessage());
            throw new WalletSaveException("Cannot backup wallet '" + perWalletModelData.getWalletFilename(), ioe);            
        }
    }
    
    private void saveWalletAndWalletInfo(PerWalletModelData perWalletModelData, String walletFilename, String walletInfoFilename) {
        File walletFile = new File(walletFilename);
        WalletInfo walletInfo = perWalletModelData.getWalletInfo();

        // Save the companion wallet info.
        walletInfo.writeToFile(walletInfoFilename, walletInfo.getWalletVersion());

        // Save the wallet file
        try {
        if (perWalletModelData.getWallet() != null) {
            if (WalletVersion.SERIALIZED == walletInfo.getWalletVersion()) {
                saveToFileAsSerialised(perWalletModelData.getWallet(), walletFile);
            } else if (WalletVersion.PROTOBUF == walletInfo.getWalletVersion() || WalletVersion.PROTOBUF_ENCRYPTED == walletInfo.getWalletVersion()) {
                perWalletModelData.getWallet().saveToFile(walletFile);
            } else {
                throw new WalletSaveException("Cannot save wallet '" + perWalletModelData.getWalletFilename() + "'. Its wallet version is '" + walletInfo.getWalletVersion().toString() + "' but this version of MultiBit does not understand that format.");
            }
        }
        } catch (IOException ioe) {
            throw new WalletSaveException("Cannot save wallet '" + perWalletModelData.getWalletFilename(), ioe);
        }
    }

    /**
     * Secure delete the wallet and the wallet info file.
     * 
     * @param perWalletModelData
     */
    public void deleteWalletAndWalletInfo(PerWalletModelData perWalletModelData) {
        if (perWalletModelData == null) {
            return;
        }
        
        File walletFile = new File(perWalletModelData.getWalletFilename());
        WalletInfo walletInfo = perWalletModelData.getWalletInfo();
        String walletInfoFilenameAsString = WalletInfo.createWalletInfoFilename(perWalletModelData.getWalletFilename());
        File walletInfoFile = new File(walletInfoFilenameAsString);
               
        synchronized (walletInfo) {
            // See if either of the files are readonly - abort.
            if (!walletFile.canWrite() || !walletInfoFile.canWrite()) {
                throw new DeleteWalletException(controller.getLocaliser().getString("deleteWalletException.walletWasReadonly"));
            }
            
            // Delete the wallet info file first, then the wallet.
            try {
                FileHandler.secureDelete(walletInfoFile);
                FileHandler.secureDelete(walletFile);
                walletInfo.setDeleted(true);
            } catch (IOException ioe) {
                log.error(ioe.getClass().getCanonicalName() + " " + ioe.getMessage());
                throw new DeleteWalletException(controller.getLocaliser().getString("deleteWalletException.genericCouldNotDelete",
                            new String[]{perWalletModelData.getWalletFilename()}));                        
            }
        }
        
        // If the wallet was deleted, delete the model data.
        if (walletInfo.isDeleted()) {
                controller.getModel().remove(perWalletModelData);
        }
        return;
    }

    public boolean haveFilesChanged(PerWalletModelData perWalletModelData) {
        if (perWalletModelData == null || perWalletModelData.getWalletFilename() == null) {
            return false;
        }

        boolean haveFilesChanged = false;

        String walletInfoFilename = WalletInfo.createWalletInfoFilename(perWalletModelData.getWalletFilename());
        File walletInfoFile = new File(walletInfoFilename);
        File walletFile = new File(perWalletModelData.getWalletFilename());

        WalletInfo walletInfo = perWalletModelData.getWalletInfo();

        if (walletInfo != null) {
            synchronized (walletInfo) {
                String walletFileSize = "" + walletFile.length();
                String walletFileLastModified = "" + walletFile.lastModified();
                String walletInfoFileSize = "" + walletInfoFile.length();
                String walletInfoFileLastModified = "" + walletInfoFile.lastModified();
                if (walletInfo != null) {
                    if (!walletFileSize.equals(walletInfo.getProperty(MultiBitModel.WALLET_FILE_SIZE))) {
                        haveFilesChanged = true;
                    }

                    if (!walletFileLastModified.equals(walletInfo.getProperty(MultiBitModel.WALLET_FILE_LAST_MODIFIED))) {
                        haveFilesChanged = true;
                    }

                    if (!walletInfoFileSize.equals(walletInfo.getProperty(MultiBitModel.WALLET_INFO_FILE_SIZE))) {
                        haveFilesChanged = true;
                    }

                    if (!walletInfoFileLastModified.equals(walletInfo.getProperty(MultiBitModel.WALLET_INFO_FILE_LAST_MODIFIED))) {
                        haveFilesChanged = true;
                    }
                }
                if (haveFilesChanged) {
                    log.debug("Result of check of whether files have changed for wallet filename "
                            + perWalletModelData.getWalletFilename() + " was " + haveFilesChanged + ".");
                }

                // Create backup filenames early if the files have changed.
                // (It is then available in the tooltip).
                if (haveFilesChanged && perWalletModelData.getWalletBackupFilename() == null) {
                    try {
                        perWalletModelData.setWalletBackupFilename(createBackupFilename(walletFile, false));

                        perWalletModelData.setWalletInfoBackupFilename(createBackupFilename(
                                new File(WalletInfo.createWalletInfoFilename(perWalletModelData.getWalletFilename())), true));
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }

        return haveFilesChanged;
    }

    /**
     * Keep a record of the wallet and wallet info files sizes and date last
     * modified.
     * 
     * @param walletFile
     *            The wallet file
     * @param walletInfo
     *            The wallet info
     */
    private void rememberFileSizesAndLastModified(File walletFile, WalletInfo walletInfo) {
        // Get the files' last modified data and sizes and store them in the
        // wallet properties.
        if (walletFile == null || walletInfo == null) {
            return;
        }

        long walletFileSize = walletFile.length();
        long walletFileLastModified = walletFile.lastModified();

        String walletFilename = walletFile.getAbsolutePath();
        String walletInfoFilename = WalletInfo.createWalletInfoFilename(walletFilename);
        File walletInfoFile = new File(walletInfoFilename);
        long walletInfoFileSize = walletInfoFile.length();
        long walletInfoFileLastModified = walletInfoFile.lastModified();

        walletInfo.put(MultiBitModel.WALLET_FILE_SIZE, "" + walletFileSize);
        walletInfo.put(MultiBitModel.WALLET_FILE_LAST_MODIFIED, "" + walletFileLastModified);
        walletInfo.put(MultiBitModel.WALLET_INFO_FILE_SIZE, "" + walletInfoFileSize);
        walletInfo.put(MultiBitModel.WALLET_INFO_FILE_LAST_MODIFIED, "" + walletInfoFileLastModified);

        // TODO Fix this - create a toString()
        log.debug("rememberFileSizesAndLastModified: Wallet filename " + walletFilename + " , " + MultiBitModel.WALLET_FILE_SIZE + " " + walletFileSize + " ,"
                + MultiBitModel.WALLET_FILE_LAST_MODIFIED + " " + walletFileLastModified + " ,"
                + MultiBitModel.WALLET_INFO_FILE_SIZE + " " + walletInfoFileSize + " ,"
                + MultiBitModel.WALLET_INFO_FILE_LAST_MODIFIED + " " + walletInfoFileLastModified);
    }

    public static void writeUserPreferences(MultiBitController controller) {
        // Save all the wallets' filenames in the user preferences.
        if (controller.getModel().getPerWalletModelDataList() != null) {
            List<PerWalletModelData> perWalletModelDataList = controller.getModel().getPerWalletModelDataList();

            int numberOfWallets = perWalletModelDataList.size();
            log.debug("When writing wallets, there were " + numberOfWallets);
            controller.getModel().setUserPreference(MultiBitModel.NUMBER_OF_WALLETS, numberOfWallets + "");
            controller.getModel().setUserPreference(MultiBitModel.ACTIVE_WALLET_FILENAME,
                    controller.getModel().getActiveWalletFilename());
            if (numberOfWallets > 0) {
                for (int i = 1; i <= numberOfWallets; i++) {
                    PerWalletModelData perWalletModelData = perWalletModelDataList.get(i - 1);
                    if (perWalletModelData.getWalletFilename() != null) {
                        controller.getModel().setUserPreference(MultiBitModel.WALLET_FILENAME_PREFIX + i,
                                perWalletModelData.getWalletFilename());
                    }
                }
            }
        }

        // Write the user preference properties.
        Properties userPreferences = controller.getModel().getAllUserPreferences();
        OutputStream outputStream;
        try {
            String userPropertiesFilename;
            if ("".equals(controller.getApplicationDataDirectoryLocator().getApplicationDataDirectory())) {
                userPropertiesFilename = USER_PROPERTIES_FILE_NAME;
            } else {
                userPropertiesFilename = controller.getApplicationDataDirectoryLocator().getApplicationDataDirectory()
                        + File.separator + USER_PROPERTIES_FILE_NAME;
            }

            outputStream = new FileOutputStream(userPropertiesFilename);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, "UTF8");
            userPreferences.store(outputStreamWriter, USER_PROPERTIES_HEADER_TEXT);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static Properties loadUserPreferences(ApplicationDataDirectoryLocator applicationDataDirectoryLocator) {
        Properties userPreferences = new Properties();
        try {
            String userPropertiesFilename;
            if ("".equals(applicationDataDirectoryLocator.getApplicationDataDirectory())) {
                userPropertiesFilename = USER_PROPERTIES_FILE_NAME;
            } else {
                userPropertiesFilename = applicationDataDirectoryLocator.getApplicationDataDirectory() + File.separator
                        + USER_PROPERTIES_FILE_NAME;
            }
            InputStream inputStream = new FileInputStream(userPropertiesFilename);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF8");
            userPreferences.load(inputStreamReader);
        } catch (FileNotFoundException e) {
            // Ok - may not have been created yet.
        } catch (IOException e) {
            // Ok may not be written yet.
        }

        return userPreferences;
    }

    
    /**
     * Uses Java serialization to save the wallet to the given file.
     */
    private synchronized void saveToFileAsSerialised(Wallet wallet, File f) throws IOException {
        log.debug("Saving wallet to file " + f.getAbsolutePath() + " in version 1 (serialised) format.");
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(f);
            saveToFileStreamAsSerialised(wallet, stream);
        } finally {
            if (stream != null)
                stream.close();
        }
    }

    /**
     * Uses Java serialization to save the wallet to the given file stream.
     */
    private synchronized void saveToFileStreamAsSerialised(Wallet wallet, OutputStream f) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(f);
        oos.writeObject(wallet);
        oos.close();
    }
    
    /**
     * Create a backup filename the format is: original file: filename.suffix.
     * backup file: filename-yyyymmddhhmmss.suffix
     * 
     * @param file
     * @param reusePreviousBackupDate
     *            Reuse the previously created backup date so that wallet and
     *            wallet info names match
     * @return
     * @throws IOException
     */
    private String createBackupFilename(File file, boolean reusePreviousBackupDate) throws IOException {
        String filename = file.getAbsolutePath();

        // Find suffix.
        int suffixSeparator = filename.lastIndexOf(".");
        String stem = filename.substring(0, suffixSeparator);
        String suffix = filename.substring(suffixSeparator); // Includes separating dot.

        if (dateForBackupName == null || !reusePreviousBackupDate) {
            dateForBackupName = new Date();
        }
        DateFormat dateFormat = new SimpleDateFormat(BACKUP_SUFFIX_FORMAT);
        String backupFilename = stem + SEPARATOR + dateFormat.format(dateForBackupName) + suffix;

        return backupFilename;
    }

    /**
     * To support multiple users on the same machine, the block chain is
     * installed into the program installation directory and is then copied to
     * the user's application data directory when MultiBit is first used.
     * 
     * Thus each user has their own copy of the blockchain.
     */
    public void copyBlockChainFromInstallationDirectory(MultiBitService multiBitService, String destinationBlockChainFilename) {
        if (destinationBlockChainFilename == null) {
            return;
        }

        // See if the block chain in the user's application data directory exists.
        // It is never overwritten.
        File destinationBlockchain = new File(destinationBlockChainFilename);
        if (!destinationBlockchain.exists()) {
            // Work out the source blockchain (put into the program installation
            // directory by the installer).
            File directory = new File(".");
            try {
                String currentWorkingDirectory = directory.getCanonicalPath();

                String filePrefix = multiBitService.getFilePrefix();
                String blockchainFilename = filePrefix + MultiBitService.BLOCKCHAIN_SUFFIX;
                String sourceBlockchainFilename = currentWorkingDirectory + File.separator + blockchainFilename;
                File sourceBlockchain = new File(sourceBlockchainFilename);
                if (sourceBlockchain.exists()) {
                    // It should exist since installer puts them in.
                    log.info("Copying blockchain from '" + sourceBlockchainFilename + "' to '" + destinationBlockChainFilename
                            + "'");
                    long startTime = (DateUtils.nowUtc()).getMillis();
                    copyFile(sourceBlockchain, destinationBlockchain);
                    long stopTime = (DateUtils.nowUtc()).getMillis();
                    log.info("Time taken to copy blockchain was " + (stopTime - startTime) + " ms.");

                    // Check all the data was copied.
                    long sourceLength = sourceBlockchain.length();
                    long destinationLength = destinationBlockchain.length();
                    if (sourceLength != destinationLength) {
                        log.error("Blockchain was not copied to user's application data directory correctly.\nThe source blockchain '"
                                + sourceBlockchainFilename
                                + "' is of length "
                                + sourceLength
                                + "\nbut the destination blockchain '"
                                + destinationBlockChainFilename
                                + "' is of length "
                                + destinationLength);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void copyFile(File sourceFile, File destinationFile) throws IOException {
        if (!destinationFile.exists()) {
            destinationFile.createNewFile();
        }
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        FileChannel source = null;
        FileChannel destination = null;
        try {
            fileInputStream = new FileInputStream(sourceFile);
            source = fileInputStream.getChannel();
            fileOutputStream = new FileOutputStream(destinationFile);
            destination = fileOutputStream.getChannel();
            long transfered = 0;
            long bytes = source.size();
            while (transfered < bytes) {
                transfered += destination.transferFrom(source, 0, source.size());
                destination.position(transfered);
            }
        } finally {
            if (source != null) {
                source.close();
            } else if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (destination != null) {
                destination.close();
            } else if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }
    
    public static File createTempDirectory(String filePrefix) throws IOException {
        final File temp;

        temp = File.createTempFile(filePrefix, Long.toString(System.currentTimeMillis()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        temp.deleteOnExit();

        return temp;
    }
    
    public static void secureDelete(File file) throws IOException {
        if (file.exists()) {
            long length = file.length();
            SecureRandom random = new SecureRandom();
            RandomAccessFile raf = new RandomAccessFile(file, "rws");
            raf.seek(0);
            raf.getFilePointer();
            byte[] data = new byte[64];
            int pos = 0;
            while (pos < length) {
                random.nextBytes(data);
                raf.write(data);
                pos += data.length;
            }
            raf.close();
            file.delete();
        }
    }
}
