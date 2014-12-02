package com.sirma.itt.idoc.web.widget;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.sirma.itt.emf.plugin.ExtensionPoint;

/**
 * Parses all subdirectories of /widgets/ for locating widgets scripts that will be included in the
 * idoc page. A directory is said to contain a widget if it has a file named widget.js. I.e.
 * /widgets/objectData/widget.js <br/>
 * The servlet context is not scanned in startup due the following bug - Bug in jboss 7.1.1
 * https://issues.jboss.org/browse/AS7-4722
 * 
 * @author Adrian Mitev
 */
@Named
@ApplicationScoped
public class WidgetManager {

	private static final String COMPONENT_SCRIPT_NAME = "component.js";

	private static final String COMPONENT_CSS_NAME = "component.css";

	private List<Widget> widgets;
	private List<Widget> components;

	@Inject
	@ExtensionPoint(value = IdocExtension.EXTENSION_POINT)
	private Iterable<IdocExtension> extensions;

	private ServletContext servletContext;

	/**
	 * Called when the app is started. Captures the servletContext for later processing.
	 * 
	 * @param event
	 *            ServletContextEvent
	 */
	public void onApplicationStartup(@Observes ServletContextEvent event) {
		servletContext = event.getServletContext();
	}

	/**
	 * Collect registered widgets and captures them.
	 * 
	 * @return captured widgets.
	 * @throws MalformedURLException
	 *             shoud never be thrown.
	 */
	public List<Widget> getWidgets() throws MalformedURLException {
		if (widgets == null) {
			widgets = new ArrayList<>();

			for (IdocExtension extension : extensions) {
				Boolean hasStyle = extension.hasStylesheet();
				widgets.add(new Widget(extension.getPath(), hasStyle));
			}

		}
		return widgets;
	}

	/**
	 * Looks for directories with components and captures them.
	 * 
	 * @return captured components.
	 * @throws MalformedURLException
	 *             should never be thrown.
	 */
	public List<Widget> getComponents() throws MalformedURLException {
		if (components == null) {
			components = new ArrayList<>();
			Set<String> resourcePaths = servletContext.getResourcePaths("/components/");

			if (resourcePaths != null) {
				for (String path : resourcePaths) {
					if (servletContext.getResource(path + COMPONENT_SCRIPT_NAME) != null) {
						Boolean hasStyle = servletContext.getResource(path + COMPONENT_CSS_NAME) != null;

						components.add(new Widget(path, hasStyle));
					}
				}
			}
		}
		return components;
	}

	/**
	 * Setter method for components.
	 * 
	 * @param components
	 *            the components to set
	 */
	public void setComponents(List<Widget> components) {
		this.components = components;
	}
}
