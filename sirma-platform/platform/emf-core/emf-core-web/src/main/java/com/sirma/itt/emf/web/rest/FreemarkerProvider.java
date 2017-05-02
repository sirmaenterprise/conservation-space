package com.sirma.itt.emf.web.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import freemarker.cache.StrongCacheStorage;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

/**
 * The Class FreemarkerProvider.
 *
 * @author hlungov
 */
@Named
@ApplicationScoped
public class FreemarkerProvider {

	private static final String BASE_PATH_CONFIG = "/META-INF/resources/templates/";
	private static String TEMP_FILE = "-template.ftl";
	private static String DEFAULT_ENCODING = "UTF-8";

	private Configuration config;

	/**
	 * Initialise FreeMarker Configuration.
	 */
	@PostConstruct
	public void init() {
		Version version = Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS;
		config = new Configuration(version);

		// setup template cache
		config.setCacheStorage(new StrongCacheStorage());
		config.setTemplateUpdateDelay(0);

		// setup template loaders
		ClassPathTemplateLoader loader = new ClassPathTemplateLoader();
		config.setTemplateLoader(loader);

		// use our custom object wrapper that can deal with QNameMap objects
		// directly
		config.setObjectWrapper(new BeansWrapperBuilder(version).build());

		// rethrow any exception so we can deal with them
		config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		// turn off locale sensitive lookup - to save numerous wasted calls
		// to nodeservice.exists()
		config.setLocalizedLookup(false);

		// set template encoding
		if (DEFAULT_ENCODING != null) {
			config.setDefaultEncoding(DEFAULT_ENCODING);
		}
		// set output encoding
		config.setOutputEncoding(DEFAULT_ENCODING);
	}

	/**
	 * Process template.
	 *
	 * @param viewModel
	 *            the view model
	 * @param path
	 *            the full path to template
	 * @return the byte array output stream
	 * @throws TemplateException
	 *             if can't process template
	 */
	public String processTemplateByFullPath(Map<?, ?> viewModel, String path) throws TemplateException {
		try (Writer out = new StringWriter()) {
			Template temp = config.getTemplate(BASE_PATH_CONFIG + path + TEMP_FILE, DEFAULT_ENCODING);
			temp.process(viewModel, out);
			out.flush();
			return out.toString();
		} catch (IOException e) {
			throw new TemplateException(e, null);
		}
	}
}