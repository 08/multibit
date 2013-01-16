package org.multibit.viewsystem.swing.action;

import java.io.File;
import java.security.SecureRandom;
import java.util.Locale;

import org.multibit.Localiser;
import org.multibit.controller.MultiBitController;
import com.google.bitcoin.crypto.KeyCrypter;
import com.google.bitcoin.crypto.KeyCrypterScrypt;
import com.google.bitcoin.crypto.ScryptParameters;

import org.multibit.exchange.CurrencyConverter;
import org.multibit.file.FileHandler;
import org.multibit.model.MultiBitModel;
import org.multibit.model.PerWalletModelData;
import org.multibit.model.WalletInfo;
import com.google.bitcoin.core.WalletVersion;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Wallet;

/**
 * Class containing utility methods for action tests.
 * @author jim
 *
 */
public class ActionTestUtils {
    
    private static SecureRandom secureRandom;

    public static MultiBitController createController() {
        MultiBitController controller = new MultiBitController();
         
         Localiser localiser = new Localiser(Locale.ENGLISH);
         MultiBitModel model = new MultiBitModel(controller);
         
         controller.setLocaliser(localiser);
         controller.setModel(model);
         
         CurrencyConverter.INSTANCE.initialise(controller);
         
         return controller;
     }
     
     public static void createNewActiveWallet(MultiBitController controller, String descriptor, boolean encrypt, char[] walletPassword) throws Exception {
         if (secureRandom == null) {
             secureRandom = new SecureRandom();
         }
         
         byte[] salt = new byte[ScryptParameters.SALT_LENGTH];
         secureRandom.nextBytes(salt);
         ScryptParameters scryptParameters = new ScryptParameters(salt);
         KeyCrypter keyCrypter = new KeyCrypterScrypt(scryptParameters);

         Wallet wallet = new Wallet(NetworkParameters.prodNet(), keyCrypter);
         wallet.getKeychain().add(new ECKey());
  
         PerWalletModelData perWalletModelData = new PerWalletModelData();
         perWalletModelData.setWallet(wallet);
  
         // Save the wallet to a temporary directory.
         File multiBitDirectory = FileHandler.createTempDirectory("CreateAndDeleteWalletsTest");
         String multiBitDirectoryPath = multiBitDirectory.getAbsolutePath();
         String walletFile = multiBitDirectoryPath + File.separator + descriptor + ".wallet";
         
         // Put the wallet in the model as the active wallet.
         perWalletModelData.setWalletInfo(new WalletInfo(walletFile, WalletVersion.PROTOBUF_ENCRYPTED));
         perWalletModelData.setWalletFilename(walletFile);
         perWalletModelData.setWalletDescription(descriptor);
         
         // Save the wallet and load it up again, making it the active wallet.
         // This also sets the timestamp fields used in file change detection.
         FileHandler fileHandler = new FileHandler(controller);
         fileHandler.savePerWalletModelData(perWalletModelData, true);
         PerWalletModelData loadedPerWalletModelData = fileHandler.loadFromFile(new File(walletFile));
             
         if (encrypt) {
             loadedPerWalletModelData.getWallet().encrypt(keyCrypter.deriveKey(walletPassword));
         }
         
         controller.getModel().setActiveWalletByFilename(loadedPerWalletModelData.getWalletFilename());
         
     }
}
