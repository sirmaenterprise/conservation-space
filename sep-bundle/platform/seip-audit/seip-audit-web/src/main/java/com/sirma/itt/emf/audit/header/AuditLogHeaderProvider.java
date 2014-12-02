package com.sirma.itt.emf.audit.header;

import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 * Provides conditions concerning the header in the audit log page.
 */
@Named
public class AuditLogHeaderProvider {
	
	/**
	 * Gets the current page name.
	 *
	 * @return the current page name
	 */
	public String getCurrentPageName() {
		String currentPage = FacesContext.getCurrentInstance().getViewRoot()
				.getViewId();
		String[] pageSections = currentPage.split("/");
		return pageSections[pageSections.length - 1];
	}
}
