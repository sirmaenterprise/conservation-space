package com.sirma.itt.emf.web.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import freemarker.cache.StrongCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * The Class FreemarkerProvider.
 * 
 * @author hlungov
 */
@Named
@ApplicationScoped
public class FreemarkerProvider {

	/** The Constant BASE_PATH_CONFIG. */
	private static final String BASE_PATH_CONFIG = "/META-INF/resources/templates/";

	/** The temp type. */
	private static String tempType = "-template.ftl";

	/** The config. */
	private Configuration config;

	/** The default encoding. */
	private static String defaultEncoding = "UTF-8";

	/**
	 * Initialise FreeMarker Configuration.
	 */
	@PostConstruct
	public void init() {
		config = new Configuration();

		// setup template cache
		config.setCacheStorage(new StrongCacheStorage());
		config.setTemplateUpdateDelay(0);

		// setup template loaders
		ClassPathTemplateLoader loader = new ClassPathTemplateLoader();
		config.setTemplateLoader(loader);

		// use our custom object wrapper that can deal with QNameMap objects
		// directly
		config.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);

		// rethrow any exception so we can deal with them
		config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		// turn off locale sensitive lookup - to save numerous wasted calls
		// to nodeservice.exists()
		config.setLocalizedLookup(false);

		// set template encoding
		if (defaultEncoding != null) {
			config.setDefaultEncoding(defaultEncoding);
		}
		// set output encoding
		config.setOutputEncoding("UTF-8");
	}

	/**
	 * Process template.
	 * 
	 * @param viewModel
	 *            the view model
	 * @param path
	 *            the full path to template
	 * @return the byte array output stream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TemplateException
	 *             if can't process template
	 */
	public String processTemplateByFullPath(Map<?, ?> viewModel, String path) throws IOException,
			TemplateException {
		Writer out = new StringWriter();
		Template temp = config.getTemplate(BASE_PATH_CONFIG + path + tempType, "UTF-8");
		try {
			temp.process(viewModel, out);
		} catch (TemplateException e) {
			throw e;
		} finally {
			out.flush();
		}
		return out.toString();
	}
}