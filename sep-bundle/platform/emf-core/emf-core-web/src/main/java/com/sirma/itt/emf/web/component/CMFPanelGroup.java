package com.sirma.itt.emf.web.component;

import javax.faces.component.FacesComponent;
import javax.faces.component.NamingContainer;
import javax.faces.component.html.HtmlPanelGroup;

/**
 * Extension of the h:panelGroup that implements the NamingContainer in order to
 * allow its children to have generated unique ids.
 * 
 * @author svelikov
 */
@FacesComponent("CMFPanelGroup")
public class CMFPanelGroup extends HtmlPanelGroup implements NamingContainer {

}
