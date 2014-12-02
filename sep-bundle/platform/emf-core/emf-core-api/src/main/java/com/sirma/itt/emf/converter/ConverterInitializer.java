package com.sirma.itt.emf.converter;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.event.ApplicationInitializationEvent;

/**
 * Initializer to load all type converter extensions to the type converter implementation
 *
 * @author BBonev
 */
public class ConverterInitializer {

	/** Default Type Converter. */
	@Inject
	private TypeConverter converter;

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConverterInitializer.class);

	@Inject
	@Any
	private Instance<TypeConverterProvider> converters;

	/**
	 * Initialize default set of Converters
	 */
	@PostConstruct
	public void initConverters() {
		LOGGER.info("Initializing type converters");

		int count = 0;
		// register all available providers
		for (TypeConverterProvider provider : converters) {
			provider.register(converter);
			count++;
		}

		// initialize the utility class for static access
		TypeConverterUtil.setTypeConverter(converter);

		LOGGER.info("Initializing type converters: COMPLETE. Processed " + count + " providers");
	}

	/**
	 * On application initialization.
	 * 
	 * @param event
	 *            the event
	 */
	public void onApplicationInitialization(@Observes ApplicationInitializationEvent event) {
		// forces creation of type converter
	}
}
