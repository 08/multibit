package org.multibit.action;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.multibit.MultiBit;
import org.multibit.controller.MultiBitController;
import org.multibit.model.AddressBook;
import org.multibit.model.DataProvider;



/**
 * exit the application
 * @author jim
 *
 */
public class ExitAction implements Action{   
    private MultiBitController controller;
    
    public ExitAction(MultiBitController controller) { 
        this.controller = controller;
    }
    
    public void execute(DataProvider dataProvider) {
        // save the wallet file
        controller.getModel().saveWallet();

        // write the user properties
        Properties userPreferences = controller.getModel().getAllUserPreferences();
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(MultiBit.PROPERTIES_FILE_NAME);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, "UTF8");
            userPreferences.store(outputStreamWriter, MultiBit.PROPERTIES_HEADER_TEXT);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // write the address book
        AddressBook addressBook = controller.getModel().getAddressBook();
        addressBook.writeToFile();
        
        // write the wallet file
        
        System.exit(0);     
    }
    
    public String getDisplayText() {
        // TODO localise
        return "exit";
    }
}


