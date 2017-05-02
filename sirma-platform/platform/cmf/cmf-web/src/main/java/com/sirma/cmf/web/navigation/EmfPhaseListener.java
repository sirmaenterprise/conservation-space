package com.sirma.cmf.web.navigation;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.cmf.web.DocumentContext;

/**
 * Handles some requests triggered from the user trough the browser like: refresh, browser back button and click on
 * simple links. In all those cases the {@link EmfNavigationHandler} is manually invoked to handle navigation with
 * specific arguments.
 *
 * @author svelikov
 */
public class EmfPhaseListener implements PhaseListener {

	private static final long serialVersionUID = -3455601862177474049L;

	private static final Logger LOG = LoggerFactory.getLogger(EmfPhaseListener.class);

	@Inject
	private DocumentContext documentContext;

	@Override
	public void afterPhase(PhaseEvent event) {
		FacesContext facesContext = event.getFacesContext();
		HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
		String queryString = request.getQueryString();
		if (queryString != null) {
			NavigationHandler navigationHandler = facesContext.getApplication().getNavigationHandler();
			// handle browser's back button
			boolean isHistory = queryString.contains(NavigationHandlerConstants.HISTORY_PAGE);
			if (isHistory) {
				LOG.debug("History page was requested!");
				navigationHandler.handleNavigation(facesContext, null, NavigationHandlerConstants.BACKWARD);
			}
			// handle page refresh
			boolean isCurrent = queryString.contains(NavigationHandlerConstants.CURRENT_PAGE);
			if (isCurrent) {
				LOG.debug("Current page was requested/page-refresh");
				navigationHandler.handleNavigation(facesContext, null, NavigationHandlerConstants.REFRESH);
			}
			// when request comes from a simple link, we manually clear the DocumentContext map
			boolean isSimpleLink = queryString.contains(NavigationHandlerConstants.SIMPLE_LINK);
			if (isSimpleLink) {
				documentContext.clear();
				String requestServletPath = facesContext.getExternalContext().getRequestServletPath();
				requestServletPath = requestServletPath.substring(requestServletPath.lastIndexOf("/") + 1,
						requestServletPath.lastIndexOf(".jsf"));
				navigationHandler.handleNavigation(facesContext, NavigationHandlerConstants.SIMPLE_LINK_OUTCOME,
						requestServletPath);
			}
		}
	}

	@Override
	public void beforePhase(PhaseEvent event) {
		//
	}

	@Override
	public PhaseId getPhaseId() {
		return PhaseId.RESTORE_VIEW;
	}

}