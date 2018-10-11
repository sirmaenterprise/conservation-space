package com.sirma.itt.emf.rest;

import java.net.URI;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.rest.Activator;

import io.swagger.jaxrs.config.BeanConfig;

/**
 * Initializer for the swagger jar-rs documentation generator.
 *
 * @author yasko
 */
@WebServlet(loadOnStartup = 2)
public class SwaggerInitializationServlet extends HttpServlet {
	private static final long serialVersionUID = -2489420464820092308L;

	@Inject
	private SystemConfiguration systemConfiguration;

	@Override
	public void init() throws javax.servlet.ServletException {
		URI accessUrl = systemConfiguration.getSystemAccessUrl().get();
		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setVersion(Activator.API_VERSION);
		beanConfig.setSchemes(new String[] { accessUrl.getScheme() });
		beanConfig.setHost(accessUrl.getHost() + ":" + accessUrl.getPort());
		beanConfig.setBasePath(getServletContext().getContextPath() + Activator.ROOT_PATH);
		beanConfig.setResourcePackage("com.sirma");
		beanConfig.setScan(true);
		beanConfig.setPrettyPrint(String.valueOf(systemConfiguration.getApplicationMode().get()));
	}
}
