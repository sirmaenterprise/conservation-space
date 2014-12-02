package com.sirma.itt.emf.web.application;

import java.io.File;
import java.io.IOException;

import javax.enterprise.event.Observes;
import javax.servlet.ServletContextEvent;

import org.apache.log4j.Logger;

import com.sirma.itt.commons.utils.file.FileUtils;

/**
 * WARNING: JBoss dependent! Verifies the correction of jboss datasource. Checks if jta="false" is
 * set as this breaks the application.
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
		File configFile = new File(".");// to prevent some NPE
		try {
			String jbossConfigDir = System.getProperty(JBOSS_CONFIG_DIR);
			if (jbossConfigDir != null) {
				configFile = new File(System.getProperty(JBOSS_CONFIG_DIR) + "/standalone.xml");
				if (configFile.exists()) {
					String content = FileUtils.getFileAsUTF8String(configFile);
					if (content.contains("jta=\"false\"")) {
						throw new IllegalStateException(
								"JBoss configuration file "
										+ configFile.getAbsolutePath()
										+ " contains jta=\"false\" in its datasource and that breaks the application");
					}
				}
			}
		} catch (IOException e) {
			LOGGER.warn("Failed to read the configurationb file: " + configFile.getAbsolutePath(),
					e);
		}
	}
}
