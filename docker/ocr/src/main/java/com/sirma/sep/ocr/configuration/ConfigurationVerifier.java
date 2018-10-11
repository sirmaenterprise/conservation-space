package com.sirma.sep.ocr.configuration;

import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;

import com.sirma.sep.ocr.service.TesseractOCRProperties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Component used to verify the necessary configuration at application startup. If vital configuration is missing a
 * custom exception will be thrown and the application will be stopped.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 20/10/2017
 */
@Component
@Profile("service")
public class ConfigurationVerifier {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final TesseractOCRProperties ocrProperties;
	private final ApplicationContext applicationContext;

	private static final String START_ERROR = "Application failed to start because of missing [%s] configuration.";

	/**
	 * Injects the beans for the service.
	 *
	 * @param ocrProperties the configuration properties.
	 * @param applicationContext context used to shutdown the application.
	 */
	@Autowired
	public ConfigurationVerifier(TesseractOCRProperties ocrProperties, ApplicationContext applicationContext) {
		this.ocrProperties = ocrProperties;
		this.applicationContext = applicationContext;
	}

	/**
	 * Verifies explicitly the tesseract configurations from application.properties.
	 */
	@PostConstruct
	public void verify() {
		if (StringUtils.isBlank(ocrProperties.getDatapath())) {
			throwExceptionAndExit("tesseract.datapath");
		}
		if (StringUtils.isBlank(ocrProperties.getLanguage())) {
			LOGGER.warn("Language is not set in the application.properties file. Will use eng language by default.");
			ocrProperties.setLanguage("eng");
		}
		if (ocrProperties.getMimetype() == null) {
			throwExceptionAndExit("tesseract.language");
		}
	}

	private void throwExceptionAndExit(String missingPropertyName) {
		// every code different than 0 is considered and 'error' exit so we set 42.
		SpringApplication.exit(applicationContext, () -> 42);
		throw new ConfigurationException(String.format(START_ERROR, missingPropertyName));
	}

}
