package com.sirma.cmf.web.tab;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.cmf.web.EntityAction;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.web.tab.SelectedTabBinding;
import com.sirma.itt.emf.web.tab.TabSelectedEvent;

/**
 * Action class backing the case dashboard tabs.
 * 
 * @author svelikov
 */
@Named
public class CaseDashboardTabAction extends EntityAction {

	/** The tab selected event. */
	@Inject
	private Event<TabSelectedEvent> tabSelectedEvent;

	/**
	 * Switch the tab and fire an event to allow some initialization to be done before the tab is
	 * loaded.
	 * 
	 * @param selectedTab
	 *            Selected tab.
	 * @return Navigation string to selected tab.
	 */
	public String switchTab(String selectedTab) {

		getDocumentContext().setSelectedTab(selectedTab);

		log.debug("CMFWeb: selected tab [" + selectedTab + "]");

		fireTabSelectedEvent(selectedTab);

		return selectedTab;
	}

	/**
	 * Fire tab selected event.
	 * 
	 * @param selectedTab
	 *            the selected tab
	 */
	protected void fireTabSelectedEvent(String selectedTab) {
		SelectedTabBinding tabBinding = new SelectedTabBinding(selectedTab);

		TabSelectedEvent event = new TabSelectedEvent(getDocumentContext().getInstance(
				CaseInstance.class));

		tabSelectedEvent.select(tabBinding).fire(event);
	}

}
