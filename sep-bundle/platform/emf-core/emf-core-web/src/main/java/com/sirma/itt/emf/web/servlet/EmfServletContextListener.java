package com.sirma.itt.emf.web.servlet;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.sirma.itt.emf.web.application.EmfApplication;

/**
 * The listener interface for receiving emfServletContext events. The class that is interested in
 * processing a emfServletContext event implements this interface, and the object created with that
 * class is registered with a component using the component's
 * <code>addEmfServletContextListener</code> method. When the emfServletContext event occurs, that
 * object's appropriate method is invoked.
 * 
 * @see EmfServletContextEvent
 */
@WebListener
public class EmfServletContextListener implements ServletContextListener {

	@Inject
	private EmfApplication emfApplication;

	@Inject
	private Event<ServletContextEvent> event;

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		String contextPath = servletContextEvent.getServletContext().getContextPath();
		emfApplication.setContextPath(contextPath);
		event.fire(servletContextEvent);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Do nothing here
	}

}
