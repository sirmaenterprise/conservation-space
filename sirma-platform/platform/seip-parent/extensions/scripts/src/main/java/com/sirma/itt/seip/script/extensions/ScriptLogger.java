package com.sirma.itt.seip.script.extensions;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;

/**
 * Adds logging to server side scripts. This is done via added a global binding with id log and methods that accept
 * single string.
 *
 * @author BBonev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 1.002)
public class ScriptLogger implements GlobalBindingsExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptLogger.class);

	/**
	 * Gets the bindings.
	 *
	 * @return the bindings
	 */
	@Override
	public Map<String, Object> getBindings() {
		return Collections.<String, Object> singletonMap("log", this);
	}

	/**
	 * Gets the scripts.
	 *
	 * @return the scripts
	 */
	@Override
	public Collection<String> getScripts() {
		return Collections.emptyList();
	}

	/**
	 * Info.
	 *
	 * @param message
	 *            the message
	 */
	public void info(Object message) {
		if (message != null) {
			LOGGER.info("{}", message);
		}
	}

	/**
	 * Debug.
	 *
	 * @param message
	 *            the message
	 */
	public void debug(Object message) {
		if (message != null) {
			LOGGER.debug("{}", message);
		}
	}

	/**
	 * Warn.
	 *
	 * @param message
	 *            the message
	 */
	public void warn(Object message) {
		if (message != null) {
			LOGGER.warn("{}", message);
		}
	}

	/**
	 * Error.
	 *
	 * @param message
	 *            the message
	 */
	public void error(Object message) {
		if (message != null) {
			LOGGER.error("{}", message);
		}
	}

}
