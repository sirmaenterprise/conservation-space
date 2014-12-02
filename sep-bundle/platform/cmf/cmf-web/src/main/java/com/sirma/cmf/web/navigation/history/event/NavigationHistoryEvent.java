package com.sirma.cmf.web.navigation.history.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * NavigationHistoryEvent fired from NavigationHistory api when user tries to go back to previous
 * visited page. This event allows application to be prepared for this case. Observers for this
 * event are selected by using the navigation string returned by the NavigationHistory api.
 * 
 * @author svelikov
 */
@Documentation("NavigationHistoryEvent fired from NavigationHistory api when user tries to go back to previous visited page. This event allows application to be prepared for this case. Observers for this event are selected by using the navigation string returned by the NavigationHistory api.")
public class NavigationHistoryEvent implements EmfEvent {

}
