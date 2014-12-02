package com.sirma.cmf.web.navigation;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.cmf.web.DocumentContext;

/**
 * The listener interface for receiving emfPhase events.
 * The class that is interested in processing a emfPhase
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addEmfPhaseListener<code> method. When
 * the emfPhase event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see EmfPhaseEvent
 */
public class EmfPhaseListener implements PhaseListener {

	private static final long serialVersionUID = -3455601862177474049L;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void afterPhase(PhaseEvent event) {
		FacesContext facesContext = event.getFacesContext();
		NavigationHandler navigationHandler = facesContext.getApplication().getNavigationHandler();
		HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext()
				.getRequest();
		String queryString = request.getQueryString();
		if (queryString != null) {
			// handle browser's back button
			boolean isHistory = queryString.contains(NavigationHandlerConstants.HISTORY_PAGE);
			if (isHistory) {
				log.debug("History page was requested!");
				navigationHandler.handleNavigation(facesContext, null,
						NavigationHandlerConstants.BACKWARD);
			}
			// handle page refresh
			boolean isCurrent = queryString.contains(NavigationHandlerConstants.CURRENT_PAGE);
			if (isCurrent) {
				log.debug("Current page was requested/page-refresh");
				navigationHandler.handleNavigation(facesContext, null,
						NavigationHandlerConstants.REFRESH);
			}
			// when request comes from a simple link, we manually clear the DocumentContext map
			boolean isSimpleLink = queryString.contains(NavigationHandlerConstants.SIMPLE_LINK);
			if (isSimpleLink) {
				DocumentContext documentContext = getDocumentContext();
				documentContext.clear();
			}
		}
	}

	/**
	 * Gets the document context by resolving EL expression #{documentContext}.
	 * 
	 * @return the document context
	 */
	private DocumentContext getDocumentContext() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Application app = facesContext.getApplication();
		ExpressionFactory elFactory = app.getExpressionFactory();
		ELContext elContext = facesContext.getELContext();
		ValueExpression valueExp = elFactory.createValueExpression(elContext, "#{documentContext}",
				Object.class);
		return (DocumentContext) valueExp.getValue(elContext);
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