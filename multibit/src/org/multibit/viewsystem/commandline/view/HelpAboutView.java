package org.multibit.viewsystem.commandline.view;

import java.io.InputStream;
import java.io.PrintStream;

import org.multibit.Localiser;
import org.multibit.viewsystem.commandline.CommandLineViewSystem;

/**
 * the help about command line view 
 * 
 * @author jim
 * 
 */
public class HelpAboutView extends AbstractView{
    public HelpAboutView(Localiser localiser, String viewDescription, InputStream inputStream,
            PrintStream printStream) {
        super(localiser, viewDescription, inputStream, printStream);
        
    }

    /**
     * display the view to the user
     */
    @Override
    public void displayView() {
        String versionNumber = localiser.getVersionNumber();

        String helpAboutMessage = localiser.getString("helpAboutAction.messageText",
                new Object[] { versionNumber });

        printStream
                .println(CommandLineViewSystem.TEXT_VIEW_OUTPUT_PREFIX
                        + CommandLineViewSystem.DISPLAY_VIEW_PREFIX + description + "\n"
                        + helpAboutMessage);

        // if there was a form it would go here

        // show menu of actions and process response
        displayActionsAndProcessResponse();
    }
}
