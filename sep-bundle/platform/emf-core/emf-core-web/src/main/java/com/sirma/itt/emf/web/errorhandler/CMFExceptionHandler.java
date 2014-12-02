package com.sirma.itt.emf.web.errorhandler;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import org.apache.log4j.Logger;

/**
 * The Class CMFExceptionHandler.
 * 
 * @author y.yordanov
 */
public class CMFExceptionHandler extends ExceptionHandlerWrapper {

	private static final Logger LOGGER = Logger.getLogger(CMFExceptionHandler.class);
	/** The exception handler. */
	private final ExceptionHandler exceptionHandler;

	/**
	 * Construct the exception handler.
	 * 
	 * @param exceptionHandler
	 *            - {@link ExceptionHandler}
	 */
	public CMFExceptionHandler(ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExceptionHandler getWrapped() {
		return exceptionHandler;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle() throws FacesException {
		final Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents()
				.iterator();
		while (i.hasNext()) {
			ExceptionQueuedEvent event = i.next();
			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event
					.getSource();
			Throwable t = context.getException();
			LOGGER.error("Occurred unexpected error in web: ", t);
			final FacesContext fc = FacesContext.getCurrentInstance();
			final Map<String, Object> requestMap = fc.getExternalContext()
					.getRequestMap();
			final NavigationHandler nav = fc.getApplication()
					.getNavigationHandler();
			try {

				if (t instanceof ViewExpiredException) {
					requestMap.put("exceptionMessage", "Session expired");
					requestMap.put("exceptionDetails", t.getMessage());
				} else if (t instanceof FileNotFoundException) {
					requestMap.put("exceptionMessage", "Page not found");
					requestMap.put("exceptionDetails", t.getMessage());
				} else {
					requestMap.put("exceptionMessage", "Server error");
					requestMap.put("exceptionDetails", t.getMessage());
				}
				nav.handleNavigation(fc, null, "/common/error.xhtml");
				fc.renderResponse();
			} finally {
				i.remove();
			}
		}
		getWrapped().handle();
	}
}
