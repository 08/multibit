package org.multibit.action;

import org.multibit.controller.ActionForward;
import org.multibit.controller.MultiBitController;
import org.multibit.model.DataProvider;

/**
 * an action to show the preferences view
 * 
 * @author jim
 *
 */
public class ShowPreferencesAction implements Action {

    private MultiBitController controller;
    
    public ShowPreferencesAction(MultiBitController controller) {
        this.controller = controller;    
    }
    
    public void execute(DataProvider dataProvider) {
        // no changes required to model
        
        controller.setActionForwardToChild(ActionForward.FORWARD_TO_PREFERENCES);       
    }
}
