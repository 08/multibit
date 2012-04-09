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
package org.multibit.functionaltests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.multibit.ApplicationDataDirectoryLocator;
import org.multibit.controller.MultiBitController;
import org.multibit.file.FileHandler;
import org.multibit.model.MultiBitModel;
import org.multibit.network.MultiBitService;
import org.multibit.viewsystem.commandline.Message;
import org.multibit.viewsystem.commandline.MessageFormatter;
import org.multibit.viewsystem.commandline.MultiBitShell;
import org.multibit.viewsystem.simple.SimpleViewSystem;
import org.multibit.viewsystem.swing.action.CreateWalletSubmitAction;
import org.multibit.viewsystem.swing.action.DeleteWalletSubmitAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * functional test to check that wallets can be created and deleted ok

 * @author jim
 * 
 */
public class CreateAndDeleteWalletsTest extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(CreateAndDeleteWalletsTest.class);

    private static File multiBitDirectory;
    
    private static MultiBitController controller;
    
    private static SimpleViewSystem simpleViewSystem ;
    
    @Before
    public void setUp() throws IOException {  
        multiBitDirectory = createMultiBitRuntime();

        // set the application data directory to be the one we just created
        ApplicationDataDirectoryLocator applicationDataDirectoryLocator = new ApplicationDataDirectoryLocator(multiBitDirectory);

        // create the controller
        controller = new MultiBitController(applicationDataDirectoryLocator);

        // create the model - gets hooked up to controller automatically
        MultiBitModel model = new MultiBitModel(controller);

        log.debug("Creating Bitcoin service");
        // create the MultiBitService that connects to the bitcoin network
        MultiBitService multiBitService = new MultiBitService(false, controller);
        controller.setMultiBitService(multiBitService);

        // add the simple view system (no Swing)
        simpleViewSystem = new SimpleViewSystem();
        controller.registerViewSystem(simpleViewSystem);

        // MultiBit runtime is now setup and running
    }
    
    @Test
    public void testCreateAndDeleteWalletsWithActions() throws Exception {
        String test1WalletPath = multiBitDirectory.getAbsolutePath() + File.separator + "actionTest1.wallet";
        String test2WalletPath = multiBitDirectory.getAbsolutePath() + File.separator + "actionTest2.wallet";

        // initially there is a blank PerWalletModelData
        assertEquals(1, controller.getModel().getPerWalletModelDataList().size());
       
        // create test1 wallet
        CreateWalletSubmitAction createNewWalletAction = new CreateWalletSubmitAction(controller, null, null);     
        createNewWalletAction.createNewWallet(test1WalletPath);
        assertEquals(1, controller.getModel().getPerWalletModelDataList().size());

        // create test2 wallet
        createNewWalletAction.createNewWallet(test2WalletPath);
        assertEquals(2, controller.getModel().getPerWalletModelDataList().size());

        // delete the test1wallet
        DeleteWalletSubmitAction deleteWalletSubmitAction = new DeleteWalletSubmitAction(controller, null, null, null);
        deleteWalletSubmitAction.deleteWallet(test1WalletPath);
        assertEquals(1, controller.getModel().getPerWalletModelDataList().size());

        // delete the test2wallet - a default one should then be created
        deleteWalletSubmitAction.deleteWallet(test2WalletPath);
        assertEquals(1, controller.getModel().getPerWalletModelDataList().size());
    }

    @Test
    public void testCreateAndDeleteWalletsWithShell() throws Exception {
        String test1WalletPath = multiBitDirectory.getAbsolutePath() + File.separator + "shellTest1.wallet";
        String test2WalletPath = multiBitDirectory.getAbsolutePath() + File.separator + "shellTest2.wallet";

        // initially there is a blank PerWalletModelData
        assertEquals(1, controller.getModel().getPerWalletModelDataList().size());
       
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        
        // create shell 
        MultiBitShell multiBitShell = new MultiBitShell(controller, printStream);
        
        // create test1 wallet
        runShellCommand("create -wallet -filename=" + test1WalletPath, 
                multiBitShell, byteArrayOutputStream, Message.WALLET_CREATED);
        assertEquals(1, controller.getModel().getPerWalletModelDataList().size());

        // create test2 wallet
        runShellCommand("create -wallet -filename=" + test2WalletPath, 
                multiBitShell, byteArrayOutputStream, Message.WALLET_CREATED);
        assertEquals(2, controller.getModel().getPerWalletModelDataList().size());
        
        // delete the test1wallet
        runShellCommand("delete -wallet -filename=" + test1WalletPath, 
                multiBitShell, byteArrayOutputStream, Message.WALLET_DELETED);
        assertEquals(1, controller.getModel().getPerWalletModelDataList().size());
       
        // delete the test2wallet - a default one should then be created
        runShellCommand("delete -wallet -filename=" + test2WalletPath, 
                multiBitShell, byteArrayOutputStream, Message.WALLET_DELETED);
        assertEquals(1, controller.getModel().getPerWalletModelDataList().size());
    }
    
    /**
     * Run the shell command on the MultiBitShell and then check the byteArrayOutputStream saw the expected message 
     * @param shellCommand The shell command to run.
     * @param shell The multiBitShell to use.
     * @param result The byteArrayOutputStream backing the output.
     * @param expectedMessage The expected message that should be returned.
     * @throws IOException 
     */
    private void runShellCommand(String shellCommand, MultiBitShell shell, ByteArrayOutputStream result, Message expectedMessage) throws IOException {
        result.reset();
        shell.processLine(shellCommand);
        assertTrue(result.toString().startsWith(MessageFormatter.expectedStartOfOutput(expectedMessage)));
    }

    /**
     * create a working, portable runtime of MultiBit in a temporary directory
     * 
     * @return the temporary directory the multibit runtime has been created in
     */
    private File createMultiBitRuntime() throws IOException {
        File multiBitDirectory = FileHandler.createTempDirectory("CreateAndDeleteWalletsTest");
        String multiBitDirectoryPath = multiBitDirectory.getAbsolutePath();

        System.out.println("Building MultiBit runtime in : " + multiBitDirectory.getAbsolutePath());

        // create an empty multibit.properties
        File multibitProperties = new File(multiBitDirectoryPath + File.separator + "multibit.properties");
        multibitProperties.createNewFile();
        multibitProperties.deleteOnExit();

        // copy in the blockchain stored in git - this is in source/main/resources/
        File multibitBlockchain = new File(multiBitDirectoryPath + File.separator + "multibit.blockchain");
        FileHandler.copyFile(new File("./src/main/resources/multibit.blockchain"), multibitBlockchain);
        multibitBlockchain.deleteOnExit();

        return multiBitDirectory;
    }
}
