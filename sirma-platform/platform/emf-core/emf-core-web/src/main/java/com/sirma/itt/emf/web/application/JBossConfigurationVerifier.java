package com.sirma.itt.emf.web.application;

import java.io.File;
import java.io.IOException;

import javax.enterprise.event.Observes;
import javax.servlet.ServletContextEvent;

import org.apache.log4j.Logger;

import com.sirma.itt.commons.utils.file.FileUtils;

/**
 * WARNING: JBoss dependent! Verifies the correction of jboss datasource. Checks if jta="false" is set as this breaks
 * the application.
 *
 * @author Adrian Mitev
 */
public class JBossConfigurationVerifier {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(JBossConfigurationVerifier.class);

	/** The Constant JBOSS_CONFIG_DIR. */
	private static final String JBOSS_CONFIG_DIR = "jboss.server.config.dir";

	/**
	 * Called on startup. Checks if the datasource configuration contains jta="false".
	 *
	 * @param event
	 *            ServletContextEvent
	 */
	public void onApplicationStarted(@Observes ServletContextEvent event) {
		try {
			String jbossConfigDir = System.getProperty(JBOSS_CONFIG_DIR);
			if (jbossConfigDir != null) {
				File configFile = new File(System.getProperty(JBOSS_CONFIG_DIR) + "/standalone.xml");
				if (configFile.exists()) {
					checkForJtaFalse(configFile);
				}
			}
		} catch (IOException e) {
			LOGGER.warn("Failed to read the jboss configuration file", e);
		}
	}

	/**
	 * Checks if the jboss config file contains jta=false string in it.
	 *
	 * @param configFile
	 *            config file to read.
	 * @throws IOException
	 *             if reading of the config file fails.
	 */
	private void checkForJtaFalse(File configFile) throws IOException {
		String content = FileUtils.getFileAsUTF8String(configFile);
		if (content.contains("jta=\"false\"")) {
			throw new IllegalStateException("JBoss configuration file " + configFile.getAbsolutePath()
					+ " contains jta=\"false\" in its datasource and that breaks the application");
		}
	}
}
